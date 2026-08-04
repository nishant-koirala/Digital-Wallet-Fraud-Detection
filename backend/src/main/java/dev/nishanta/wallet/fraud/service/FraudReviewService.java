package dev.nishanta.wallet.fraud.service;

import dev.nishanta.wallet.common.exception.FlagAlreadyReviewedException;
import dev.nishanta.wallet.common.exception.InsufficientBalanceException;
import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.fraud.domain.FraudFlag;
import dev.nishanta.wallet.fraud.domain.ReviewDecision;
import dev.nishanta.wallet.fraud.dto.FraudFlagResponse;
import dev.nishanta.wallet.fraud.dto.ReviewResponse;
import dev.nishanta.wallet.fraud.repository.FraudFlagRepository;
import dev.nishanta.wallet.transaction.domain.EntryType;
import dev.nishanta.wallet.transaction.domain.LedgerEntry;
import dev.nishanta.wallet.transaction.domain.Transaction;
import dev.nishanta.wallet.transaction.ledger.WalletBalanceCalculator;
import dev.nishanta.wallet.transaction.repository.LedgerEntryRepository;
import dev.nishanta.wallet.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.user.domain.User;
import dev.nishanta.wallet.user.repository.UserRepository;
import dev.nishanta.wallet.wallet.domain.Wallet;
import dev.nishanta.wallet.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FraudReviewService {

    private final FraudFlagRepository fraudFlagRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    private final WalletBalanceCalculator balanceCalculator;

    public FraudReviewService(FraudFlagRepository fraudFlagRepository,
                              TransactionRepository transactionRepository,
                              WalletRepository walletRepository,
                              LedgerEntryRepository ledgerEntryRepository,
                              UserRepository userRepository,
                              WalletBalanceCalculator balanceCalculator) {
        this.fraudFlagRepository = fraudFlagRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.userRepository = userRepository;
        this.balanceCalculator = balanceCalculator;
    }

    public List<FraudFlagResponse> listPending() {
        return fraudFlagRepository.findByReviewed(false).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse approve(UUID transactionId, UUID adminUserId) {
        FraudFlag flag = fraudFlagRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new NotFoundException("No fraud flag for transaction: " + transactionId));

        if (flag.isReviewed()) {
            throw new FlagAlreadyReviewedException("This flag has already been reviewed");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("Admin user not found: " + adminUserId));

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
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + firstLockId));
        Wallet secondLocked = walletRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + secondLockId));

        Wallet fromWallet = firstLockId.equals(fromId) ? firstLocked : secondLocked;
        Wallet toWallet = firstLockId.equals(fromId) ? secondLocked : firstLocked;

        BigDecimal senderBalance = balanceCalculator.calculateBalance(fromWallet.getId());
        if (senderBalance.compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientBalanceException(
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

        return toReviewResponse(transaction, "APPROVED");
    }

    @Transactional
    public ReviewResponse reject(UUID transactionId, UUID adminUserId) {
        FraudFlag flag = fraudFlagRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new NotFoundException("No fraud flag for transaction: " + transactionId));

        if (flag.isReviewed()) {
            throw new FlagAlreadyReviewedException("This flag has already been reviewed");
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("Admin user not found: " + adminUserId));

        Transaction transaction = flag.getTransaction();
        transaction.markFailed();
        transactionRepository.save(transaction);

        flag.review(admin, ReviewDecision.REJECTED);
        fraudFlagRepository.save(flag);

        // No ledger entries ever get created — the money never moves.
        return toReviewResponse(transaction, "REJECTED");
    }

    private FraudFlagResponse toResponse(FraudFlag flag) {
        return new FraudFlagResponse(
                flag.getTransaction().getId(),
                flag.getRuleTriggered(),
                flag.getRiskScore(),
                flag.getTransaction().getStatus().name(),
                flag.getCreatedAt()
        );
    }

    private ReviewResponse toReviewResponse(Transaction transaction, String decision) {
        return new ReviewResponse(transaction.getId(), decision, transaction.getStatus().name());
    }
}
