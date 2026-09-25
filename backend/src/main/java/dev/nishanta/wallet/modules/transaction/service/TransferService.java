package dev.nishanta.wallet.modules.transaction.service;

import dev.nishanta.wallet.common.exception.InsufficientBalanceException;
import dev.nishanta.wallet.modules.fraud.service.FraudDetectionService;
import dev.nishanta.wallet.modules.fraud.domain.FraudDetectionResult;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.dto.TransferRequest;
import dev.nishanta.wallet.modules.transaction.dto.TransferResponse;
import dev.nishanta.wallet.modules.transaction.ledger.BalanceCalculator;
import dev.nishanta.wallet.modules.transaction.ledger.LedgerPostingService;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.service.WalletLockingService;
import dev.nishanta.wallet.modules.wallet.service.WalletLockingService.WalletPair;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import dev.nishanta.wallet.modules.wallet.domain.WalletType;
import dev.nishanta.wallet.common.exception.BusinessRuleException;
import dev.nishanta.wallet.common.exception.OtpRequiredException;
import dev.nishanta.wallet.modules.audit.service.AuditService;
import dev.nishanta.wallet.modules.auth.service.OtpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

// Single responsibility: orchestrate a transfer. Wallet locking, balance
// enforcement, fraud detection and ledger posting are each delegated to
// their own service.
import dev.nishanta.wallet.security.SecurityUtils;

@Service
public class TransferService {

    private final TransactionRepository transactionRepository;
    private final WalletLockingService walletLockingService;
    private final FraudDetectionService fraudDetectionService;
    private final LedgerPostingService ledgerPostingService;
    private final BalanceCalculator balanceCalculator;
    private final AuditService auditService;
    private final WalletRepository walletRepository;
    private final OtpService otpService;
    private final TransactionLimitValidator transactionLimitValidator;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    public TransferService(TransactionRepository transactionRepository,
                           WalletLockingService walletLockingService,
                           FraudDetectionService fraudDetectionService,
                           LedgerPostingService ledgerPostingService,
                           BalanceCalculator balanceCalculator,
                           AuditService auditService,
                           WalletRepository walletRepository,
                           OtpService otpService,
                           TransactionLimitValidator transactionLimitValidator,
                           SecurityUtils securityUtils,
                           PasswordEncoder passwordEncoder) {
        this.transactionRepository = transactionRepository;
        this.walletLockingService = walletLockingService;
        this.fraudDetectionService = fraudDetectionService;
        this.ledgerPostingService = ledgerPostingService;
        this.balanceCalculator = balanceCalculator;
        this.auditService = auditService;
        this.walletRepository = walletRepository;
        this.otpService = otpService;
        this.transactionLimitValidator = transactionLimitValidator;
        this.securityUtils = securityUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(noRollbackFor = OtpRequiredException.class)
    public TransferResponse transfer(TransferRequest request, String deviceId, String ipAddress) {
        String idempotencyKey = request.idempotencyKey();
        UUID fromWalletId = request.fromWalletId();
        
        UUID toWalletId = request.toWalletId();
        if (toWalletId == null && request.toPhoneNumber() != null) {
            Wallet toWallet = walletRepository.findByUser_PhoneNumberAndType(request.toPhoneNumber(), WalletType.PERSONAL)
                 .orElseThrow(() -> new BusinessRuleException("No wallet found for phone number " + request.toPhoneNumber()));
            toWalletId = toWallet.getId();
        } else if (toWalletId == null) {
            throw new BusinessRuleException("Either toWalletId or toPhoneNumber must be provided");
        }

        BigDecimal amount = request.amount();

        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Wallet fromWalletCheck = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> new BusinessRuleException("Wallet not found"));
        securityUtils.verifyWalletOwnership(fromWalletCheck);
        
        if (request.pin() == null || fromWalletCheck.getUser().getPinHash() == null || !passwordEncoder.matches(request.pin(), fromWalletCheck.getUser().getPinHash())) {
            throw new BusinessRuleException("Invalid Transaction PIN");
        }

        // Fraud check happens BEFORE any money actually moves. A flagged
        // transaction must not have already changed either wallet's
        // balance, or "held for review" would be a lie. 
        // We use the basic fromWalletCheck for fraud, no locks yet.

        // Transaction is created and saved FIRST, as PENDING — this
        // exists regardless of outcome, so there's always a record of
        // the attempt, and fraud rules that query transaction history
        // have something to reason about.
        Transaction transaction = new Transaction(
                idempotencyKey, fromWalletCheck, walletRepository.findById(toWalletId).get(), amount, fromWalletCheck.getCurrency(),
                request.latitude(), request.longitude(), deviceId, ipAddress);
        transactionRepository.save(transaction);

        // Fraud check happens BEFORE any money actually moves. A flagged
        // transaction must not have already changed either wallet's
        // balance, or "held for review" would be a lie. The detection
        // service writes no ledger entries when it flags.
        FraudDetectionResult result = fraudDetectionService.evaluateFraud(transaction);
        
        if (result == FraudDetectionResult.FLAGGED) {
            return toResponse(transaction);
        }

        // --- 2FA OTP LOGIC ---
        // Require OTP for transfers >= Rs 1000 OR if MINOR_FRAUD was detected
        boolean needsOtp = amount.compareTo(new BigDecimal("1000")) >= 0 || result == FraudDetectionResult.MINOR_FRAUD;

        if (needsOtp) {
            String userEmail = fromWalletCheck.getUser().getEmail();
            if (request.otp() == null || request.otp().isEmpty()) {
                otpService.generateAndSendOtp(userEmail);
                transaction.markFailed();
                transactionRepository.save(transaction);
                throw new OtpRequiredException("Verification required. OTP sent to " + userEmail);
            } else {
                otpService.validateOtp(userEmail, request.otp());
            }
        }

        // Only a genuinely clean transaction reaches this point.
        // NOW we acquire locks.
        WalletPair wallets = walletLockingService.lockForTransfer(fromWalletId, toWalletId);
        Wallet fromWalletLocked = wallets.fromWallet();
        Wallet toWalletLocked = wallets.toWallet();

        BigDecimal senderBalance = balanceCalculator.calculateBalance(fromWalletLocked.getId());
        if (senderBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in wallet " + fromWalletLocked.getId());
        }

        // --- KYC LOGIC & DAILY LIMITS ---
        transactionLimitValidator.validateDailyLimit(fromWalletLocked, amount);

        // Update the transaction's detached wallet references if necessary, or pass the fresh locked ones.
        ledgerPostingService.postAndComplete(transaction, fromWalletLocked, toWalletLocked);

        auditService.logAction("TRANSACTION", transaction.getId(), "TRANSFER", fromWalletLocked.getUser().getEmail(), null, request);

        return toResponse(transaction);
    }

    private TransferResponse toResponse(Transaction tx) {
        return new TransferResponse(
                tx.getId(),
                tx.getStatus().name(),
                tx.getFromWallet().getId(),
                tx.getToWallet().getId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getCreatedAt()
        );
    }
}
