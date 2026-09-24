package dev.nishanta.wallet.modules.transaction.service;

import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.transaction.domain.EntryType;
import dev.nishanta.wallet.modules.transaction.domain.LedgerEntry;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.ledger.BalanceCalculator;
import dev.nishanta.wallet.modules.transaction.repository.LedgerEntryRepository;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.dto.BalanceResponse;
import dev.nishanta.wallet.modules.wallet.dto.WithdrawRequest;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import dev.nishanta.wallet.modules.wallet.service.MintWalletProvider;
import dev.nishanta.wallet.common.exception.BusinessRuleException;
import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.audit.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import dev.nishanta.wallet.modules.fraud.domain.FraudDetectionResult;
import dev.nishanta.wallet.modules.fraud.service.FraudDetectionService;
import dev.nishanta.wallet.common.exception.OtpRequiredException;
import dev.nishanta.wallet.modules.auth.service.OtpService;
import dev.nishanta.wallet.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class WithdrawService {

    private final MintWalletProvider mintWalletProvider;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BalanceCalculator balanceCalculator;
    private final AuditService auditService;
    private final TransactionLimitValidator transactionLimitValidator;
    private final FraudDetectionService fraudDetectionService;
    private final OtpService otpService;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    public WithdrawService(MintWalletProvider mintWalletProvider,
                           WalletRepository walletRepository,
                           TransactionRepository transactionRepository,
                           LedgerEntryRepository ledgerEntryRepository,
                           BalanceCalculator balanceCalculator,
                           AuditService auditService,
                           TransactionLimitValidator transactionLimitValidator,
                           FraudDetectionService fraudDetectionService,
                           OtpService otpService,
                           SecurityUtils securityUtils,
                           PasswordEncoder passwordEncoder) {
        this.mintWalletProvider = mintWalletProvider;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.balanceCalculator = balanceCalculator;
        this.auditService = auditService;
        this.transactionLimitValidator = transactionLimitValidator;
        this.fraudDetectionService = fraudDetectionService;
        this.otpService = otpService;
        this.securityUtils = securityUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public BalanceResponse withdraw(UUID targetWalletId, WithdrawRequest request) {
        String idempotencyKey = request.idempotencyKey();
        BigDecimal amount = request.amount();

        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(targetWalletId, existing.get().getCurrency());
        }

        Wallet mintWallet = mintWalletProvider.findOrCreateMintWallet();

        UUID mintId = mintWallet.getId();
        UUID firstLockId = mintId.compareTo(targetWalletId) < 0 ? mintId : targetWalletId;
        UUID secondLockId = mintId.compareTo(targetWalletId) < 0 ? targetWalletId : mintId;

        Wallet firstLocked = walletRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + firstLockId));
        Wallet secondLocked = walletRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + secondLockId));

        Wallet lockedMint = firstLockId.equals(mintId) ? firstLocked : secondLocked;
        Wallet targetWallet = firstLockId.equals(mintId) ? secondLocked : firstLocked;

        securityUtils.verifyWalletOwnership(targetWallet);

        if (request.pin() == null || targetWallet.getUser().getPinHash() == null || !passwordEncoder.matches(request.pin(), targetWallet.getUser().getPinHash())) {
            throw new BusinessRuleException("Invalid Transaction PIN");
        }

        BigDecimal currentBalance = balanceCalculator.calculateBalance(targetWallet.getId());
        if (currentBalance.compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient funds for withdrawal");
        }

        // --- KYC LOGIC & DAILY LIMITS ---
        transactionLimitValidator.validateDailyLimit(targetWallet, amount);

        Transaction transaction = new Transaction(
                idempotencyKey, targetWallet, lockedMint, amount, targetWallet.getCurrency(), null, null, null, null);
        transactionRepository.save(transaction);

        // Fraud check happens BEFORE any money actually moves.
        FraudDetectionResult result = fraudDetectionService.evaluateFraud(transaction);
        if (result == FraudDetectionResult.FLAGGED) {
            return toResponse(targetWalletId, targetWallet.getCurrency()); // return early without processing
        } else if (result == FraudDetectionResult.MINOR_FRAUD) {
            String userEmail = targetWallet.getUser().getEmail();
            if (request.otp() == null || request.otp().isEmpty()) {
                otpService.generateAndSendOtp(userEmail);
                throw new OtpRequiredException("Unusual activity detected. OTP sent to " + userEmail + " for step-up verification.");
            } else {
                otpService.validateOtp(userEmail, request.otp());
            }
        }

        LedgerEntry debit = new LedgerEntry(
                transaction, targetWallet, amount.negate(), EntryType.DEBIT, targetWallet.getCurrency());
        LedgerEntry credit = new LedgerEntry(
                transaction, lockedMint, amount, EntryType.CREDIT, lockedMint.getCurrency());
        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        transaction.markCompleted();
        transactionRepository.save(transaction);

        auditService.logAction("TRANSACTION", transaction.getId(), "WITHDRAW", "USER", null, request);

        return toResponse(targetWalletId, targetWallet.getCurrency());
    }

    private BalanceResponse toResponse(UUID walletId, String currency) {
        return new BalanceResponse(walletId, balanceCalculator.calculateBalance(walletId), currency);
    }
}
