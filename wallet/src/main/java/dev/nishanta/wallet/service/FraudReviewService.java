package dev.nishanta.wallet.service;

import dev.nishanta.wallet.domain.*;
import dev.nishanta.wallet.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class FraudReviewService {

    private final FraudFlagRepository fraudFlagRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    private final dev.nishanta.wallet.ledger.WalletBalanceCalculator balanceCalculator;

    public FraudReviewService(FraudFlagRepository fraudFlagRepository,
                              TransactionRepository transactionRepository,
                              WalletRepository walletRepository,
                              LedgerEntryRepository ledgerEntryRepository,
                              UserRepository userRepository,
                              dev.nishanta.wallet.ledger.WalletBalanceCalculator balanceCalculator) {
        this.fraudFlagRepository = fraudFlagRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.userRepository = userRepository;
        this.balanceCalculator = balanceCalculator;
    }

    public List<FraudFlag> listPending() {
        return fraudFlagRepository.findByReviewed(false);
    }

    @Transactional
    public Transaction approve(UUID transactionId, UUID adminUserId) {
        FraudFlag flag = fraudFlagRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("No fraud flag for transaction: " + transactionId));

        if (flag.isReviewed()) {
            throw new IllegalStateException("This flag has already been reviewed");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found: " + adminUserId));

        Transaction transaction = flag.getTransaction();

        // Lock both wallets, same ordered pattern as TransferService —
        // time has passed since this was flagged, so we re-verify the
        // sender still has sufficient balance NOW, not just at the
        // original moment of the transfer attempt.
        UUID fromId = transaction.getFromWallet().getId();
        UUID toId = transaction.getToWallet().getId();
        UUID firstLockId = fromId.compareTo(toId) < 0 ? fromId : toId;
        UUID secondLockId = fromId.compareTo(toId) < 0 ? toId : fromId;

        Wallet firstLocked = walletRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + firstLockId));
        Wallet secondLocked = walletRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + secondLockId));

        Wallet fromWallet = firstLockId.equals(fromId) ? firstLocked : secondLocked;
        Wallet toWallet = firstLockId.equals(fromId) ? secondLocked : firstLocked;

        BigDecimal senderBalance = balanceCalculator.calculateBalance(fromWallet.getId());
        if (senderBalance.compareTo(transaction.getAmount()) < 0) {
            throw new IllegalStateException(
                    "Sender's balance is no longer sufficient to approve this transaction");
        }

        LedgerEntry debit = new LedgerEntry(
                transaction, fromWallet, transaction.getAmount().negate(), EntryType.DEBIT, fromWallet.getCurrency());
        LedgerEntry credit = new LedgerEntry(
                transaction, toWallet, transaction.getAmount(), EntryType.CREDIT, toWallet.getCurrency());
        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        transaction.markCompleted();
        transactionRepository.save(transaction);

        flag.review(admin, ReviewDecision.APPROVED);
        fraudFlagRepository.save(flag);

        return transaction;
    }

    @Transactional
    public Transaction reject(UUID transactionId, UUID adminUserId) {
        FraudFlag flag = fraudFlagRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("No fraud flag for transaction: " + transactionId));

        if (flag.isReviewed()) {
            throw new IllegalStateException("This flag has already been reviewed");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found: " + adminUserId));

        Transaction transaction = flag.getTransaction();
        transaction.markFailed();
        transactionRepository.save(transaction);

        flag.review(admin, ReviewDecision.REJECTED);
        fraudFlagRepository.save(flag);

        // No ledger entries ever get created — the money never moves.
        return transaction;
    }
}