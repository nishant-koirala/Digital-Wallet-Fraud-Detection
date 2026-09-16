package dev.nishanta.wallet.modules.fraud.rules;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import dev.nishanta.wallet.modules.fraud.domain.FraudConfig;
import dev.nishanta.wallet.modules.fraud.repository.FraudConfigRepository;
import dev.nishanta.wallet.modules.fraud.domain.FraudSeverity;

import java.time.LocalDateTime;
import java.util.UUID;

// Relative to the wallet's OWN typical activity, same principle as
// AmountThresholdRule — a busy grocery store's normal pace and a quiet
// personal wallet's normal pace are both "normal FOR THEM."
@Component
public class VelocityRule implements FraudRule {

    private final TransactionRepository transactionRepository;
    private final FraudConfigRepository fraudConfigRepository;

    public VelocityRule(TransactionRepository transactionRepository, FraudConfigRepository fraudConfigRepository) {
        this.transactionRepository = transactionRepository;
        this.fraudConfigRepository = fraudConfigRepository;
    }

    @Override
    public FraudSeverity evaluate(Transaction transaction) {
        FraudConfig config = fraudConfigRepository.findById(1)
                .orElseGet(() -> new FraudConfig(new java.math.BigDecimal("50000"), 5, new java.math.BigDecimal("5"), 500.0, 10, 6, 5, 3.0));

        UUID walletId = transaction.getFromWallet().getId();
        LocalDateTime now = LocalDateTime.now();

        long currentWindowCount = transactionRepository.countByFromWalletIdAndCreatedAtBetween(
                walletId, now.minusMinutes(config.getVelocityWindowMinutes()), now);

        // Build a baseline: average transactions-per-window over the last hour.
        long historicalTotal = transactionRepository.countByFromWalletIdAndCreatedAtBetween(
                walletId, now.minusMinutes((long) config.getVelocityWindowMinutes() * config.getVelocityLookbackWindows()), now.minusMinutes(config.getVelocityWindowMinutes()));

        if (historicalTotal == 0) {
            // No baseline yet for this wallet — fall back to a flat cap.
            if (currentWindowCount > config.getVelocityColdStartMax() + 2) {
                return FraudSeverity.MAJOR;
            }
            if (currentWindowCount > config.getVelocityColdStartMax()) {
                return FraudSeverity.MINOR;
            }
            return FraudSeverity.NONE;
        }

        double averagePerWindow = (double) historicalTotal / config.getVelocityLookbackWindows();
        double minorThreshold = averagePerWindow * config.getVelocityMultiplier();
        double majorThreshold = averagePerWindow * (config.getVelocityMultiplier() + 2.0);

        if (currentWindowCount > majorThreshold) {
            return FraudSeverity.MAJOR;
        }
        if (currentWindowCount > minorThreshold) {
            return FraudSeverity.MINOR;
        }
        return FraudSeverity.NONE;
    }

    @Override
    public String ruleName() {
        return "VELOCITY";
    }
}
