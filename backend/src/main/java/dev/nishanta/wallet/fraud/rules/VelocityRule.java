package dev.nishanta.wallet.fraud.rules;

import dev.nishanta.wallet.transaction.domain.Transaction;
import dev.nishanta.wallet.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

// Relative to the wallet's OWN typical activity, same principle as
// AmountThresholdRule — a busy grocery store's normal pace and a quiet
// personal wallet's normal pace are both "normal FOR THEM."
@Component
public class VelocityRule implements FraudRule {

    private static final int WINDOW_MINUTES = 10;
    private static final int LOOKBACK_WINDOWS = 6; // last 6 windows = last hour, to build a baseline
    private static final int COLD_START_MAX = 5;   // fallback for wallets with no history yet
    private static final double MULTIPLIER = 3.0;

    private final TransactionRepository transactionRepository;

    public VelocityRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public boolean isSuspicious(Transaction transaction) {
        UUID walletId = transaction.getFromWallet().getId();
        LocalDateTime now = LocalDateTime.now();

        long currentWindowCount = transactionRepository.countByFromWalletIdAndCreatedAtBetween(
                walletId, now.minusMinutes(WINDOW_MINUTES), now);

        // Build a baseline: average transactions-per-window over the last hour.
        long historicalTotal = transactionRepository.countByFromWalletIdAndCreatedAtBetween(
                walletId, now.minusMinutes((long) WINDOW_MINUTES * LOOKBACK_WINDOWS), now.minusMinutes(WINDOW_MINUTES));

        if (historicalTotal == 0) {
            // No baseline yet for this wallet — fall back to a flat cap.
            return currentWindowCount > COLD_START_MAX;
        }

        double averagePerWindow = (double) historicalTotal / LOOKBACK_WINDOWS;
        double relativeThreshold = averagePerWindow * MULTIPLIER;

        return currentWindowCount > relativeThreshold;
    }

    @Override
    public String ruleName() {
        return "VELOCITY";
    }
}
