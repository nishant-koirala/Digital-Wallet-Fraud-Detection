package dev.nishanta.wallet.modules.transaction.service;

import dev.nishanta.wallet.common.exception.InsufficientBalanceException;
import dev.nishanta.wallet.modules.fraud.service.FraudDetectionService;
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

// Single responsibility: orchestrate a transfer. Wallet locking, balance
// enforcement, fraud detection and ledger posting are each delegated to
// their own service.
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

    public TransferService(TransactionRepository transactionRepository,
                           WalletLockingService walletLockingService,
                           FraudDetectionService fraudDetectionService,
                           LedgerPostingService ledgerPostingService,
                           BalanceCalculator balanceCalculator,
                           AuditService auditService,
                           WalletRepository walletRepository,
                           OtpService otpService) {
        this.transactionRepository = transactionRepository;
        this.walletLockingService = walletLockingService;
        this.fraudDetectionService = fraudDetectionService;
        this.ledgerPostingService = ledgerPostingService;
        this.balanceCalculator = balanceCalculator;
        this.auditService = auditService;
        this.walletRepository = walletRepository;
        this.otpService = otpService;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
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

        WalletPair wallets = walletLockingService.lockForTransfer(fromWalletId, toWalletId);
        Wallet fromWallet = wallets.fromWallet();
        Wallet toWallet = wallets.toWallet();

        BigDecimal senderBalance = balanceCalculator.calculateBalance(fromWallet.getId());
        if (senderBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in wallet " + fromWallet.getId());
        }

        // --- 2FA OTP LOGIC ---
        // Require OTP for transfers >= Rs 10,000 (we can use 1000 for easier demoing)
        if (amount.compareTo(new BigDecimal("1000")) >= 0) {
            String userEmail = fromWallet.getUser().getEmail();
            if (request.otp() == null || request.otp().isEmpty()) {
                otpService.generateAndSendOtp(userEmail);
                throw new OtpRequiredException("OTP sent to " + userEmail);
            } else {
                otpService.validateOtp(userEmail, request.otp());
            }
        }

        // Transaction is created and saved FIRST, as PENDING — this
        // exists regardless of outcome, so there's always a record of
        // the attempt, and fraud rules that query transaction history
        // have something to reason about.
        Transaction transaction = new Transaction(
                idempotencyKey, fromWallet, toWallet, amount, fromWallet.getCurrency(),
                request.latitude(), request.longitude());
        transactionRepository.save(transaction);

        // Fraud check happens BEFORE any money actually moves. A flagged
        // transaction must not have already changed either wallet's
        // balance, or "held for review" would be a lie. The detection
        // service writes no ledger entries when it flags.
        if (fraudDetectionService.flagIfSuspicious(transaction)) {
            return toResponse(transaction);
        }

        // Only a genuinely clean transaction reaches this point, where
        // the double-entry ledger pair actually gets written.
        ledgerPostingService.postAndComplete(transaction, fromWallet, toWallet);

        auditService.logAction("TRANSACTION", transaction.getId(), "TRANSFER", fromWallet.getUser().getEmail(), null, request);

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
