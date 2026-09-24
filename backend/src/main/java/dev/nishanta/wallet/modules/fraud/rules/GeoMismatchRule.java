package dev.nishanta.wallet.modules.fraud.rules;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.fraud.domain.FraudSeverity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class GeoMismatchRule implements FraudRule {

    // Faster than any commercial flight — a real person cannot cover
    // this distance in this time, no matter how they traveled.
    private static final double MAX_PLAUSIBLE_SPEED_KMH = 900.0;

    private final TransactionRepository transactionRepository;

    public GeoMismatchRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public FraudSeverity evaluate(Transaction transaction) {
        // No location on THIS transaction — nothing to check. Per the
        // earlier decision: skip silently, don't penalize missing
        // permission.
        if (transaction.getLatitude() == null || transaction.getLongitude() == null) {
            return FraudSeverity.NONE;
        }

        var previous = transactionRepository.findFirstByFromWalletIdAndIdNotAndLatitudeIsNotNullOrderByCreatedAtDesc(
                transaction.getFromWallet().getId(), transaction.getId());

        // No prior located transaction for this wallet — nothing to
        // compare against yet (cold start).
        if (previous.isEmpty()) {
            return FraudSeverity.NONE;
        }

        Transaction prev = previous.get();

        double distanceKm = dev.nishanta.wallet.modules.fraud.util.GeoUtils.haversineDistanceKm(
                prev.getLatitude().doubleValue(), prev.getLongitude().doubleValue(),
                transaction.getLatitude().doubleValue(), transaction.getLongitude().doubleValue());

        double hoursElapsed = Duration.between(prev.getCreatedAt(), transaction.getCreatedAt()).toSeconds() / 3600.0;

        // Guard against divide-by-zero / near-zero time (two transactions
        // essentially simultaneous) — treat as maximally suspicious if
        // there's real distance but ~no time elapsed.
        if (hoursElapsed <= 0.001) {
            return (distanceKm > 1.0) ? FraudSeverity.MAJOR : FraudSeverity.NONE;
        }

        double impliedSpeedKmh = distanceKm / hoursElapsed;

        return (impliedSpeedKmh > MAX_PLAUSIBLE_SPEED_KMH) ? FraudSeverity.MAJOR : FraudSeverity.NONE;
    }



    @Override
    public String ruleName() {
        return "GEO_MISMATCH";
    }
}
