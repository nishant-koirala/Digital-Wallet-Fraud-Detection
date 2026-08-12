package dev.nishanta.wallet.transaction.service;

import dev.nishanta.wallet.common.exception.InsufficientBalanceException;
import dev.nishanta.wallet.fraud.service.FraudDetectionService;
import dev.nishanta.wallet.transaction.domain.Transaction;
import dev.nishanta.wallet.transaction.dto.TransferRequest;
import dev.nishanta.wallet.transaction.dto.TransferResponse;
import dev.nishanta.wallet.transaction.ledger.BalanceCalculator;
import dev.nishanta.wallet.transaction.ledger.LedgerPostingService;
import dev.nishanta.wallet.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.wallet.domain.Wallet;
import dev.nishanta.wallet.wallet.service.WalletLockingService;
import dev.nishanta.wallet.wallet.service.WalletLockingService.WalletPair;
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

    public TransferService(TransactionRepository transactionRepository,
                           WalletLockingService walletLockingService,
                           FraudDetectionService fraudDetectionService,
                           LedgerPostingService ledgerPostingService,
                           BalanceCalculator balanceCalculator) {
        this.transactionRepository = transactionRepository;
        this.walletLockingService = walletLockingService;
        this.fraudDetectionService = fraudDetectionService;
        this.ledgerPostingService = ledgerPostingService;
        this.balanceCalculator = balanceCalculator;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        String idempotencyKey = request.idempotencyKey();
        UUID fromWalletId = request.fromWalletId();
        UUID toWalletId = request.toWalletId();
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
