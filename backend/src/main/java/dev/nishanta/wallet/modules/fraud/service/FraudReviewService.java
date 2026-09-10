package dev.nishanta.wallet.modules.fraud.service;

import dev.nishanta.wallet.common.exception.FlagAlreadyReviewedException;
import dev.nishanta.wallet.common.exception.InsufficientBalanceException;
import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.fraud.domain.FraudFlag;
import dev.nishanta.wallet.modules.fraud.domain.ReviewDecision;
import dev.nishanta.wallet.modules.fraud.dto.FraudFlagResponse;
import dev.nishanta.wallet.modules.fraud.dto.ReviewResponse;
import dev.nishanta.wallet.modules.fraud.repository.FraudFlagRepository;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.ledger.BalanceCalculator;
import dev.nishanta.wallet.modules.transaction.ledger.LedgerPostingService;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.service.WalletLockingService;
import dev.nishanta.wallet.modules.wallet.service.WalletLockingService.WalletPair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Single responsibility: make admin review decisions on flagged
// transactions. Wallet locking, balance re-verification and ledger
// posting are delegated to their own services.
@Service
public class FraudReviewService {

    private final FraudFlagRepository fraudFlagRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletLockingService walletLockingService;
    private final LedgerPostingService ledgerPostingService;
    private final BalanceCalculator balanceCalculator;

    public FraudReviewService(FraudFlagRepository fraudFlagRepository,
                              TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              WalletLockingService walletLockingService,
                              LedgerPostingService ledgerPostingService,
                              BalanceCalculator balanceCalculator) {
        this.fraudFlagRepository = fraudFlagRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.walletLockingService = walletLockingService;
        this.ledgerPostingService = ledgerPostingService;
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

        // Time has passed since this was flagged, so we re-verify the
        // sender still has sufficient balance NOW, not just at the
        // original moment of the transfer attempt.
        WalletPair wallets = walletLockingService.lockForTransfer(
                transaction.getFromWallet().getId(), transaction.getToWallet().getId());
        Wallet fromWallet = wallets.fromWallet();
        Wallet toWallet = wallets.toWallet();

        BigDecimal senderBalance = balanceCalculator.calculateBalance(fromWallet.getId());
        if (senderBalance.compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Sender's balance is no longer sufficient to approve this transaction");
        }

        ledgerPostingService.postAndComplete(transaction, fromWallet, toWallet);

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
