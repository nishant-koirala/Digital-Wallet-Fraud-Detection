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
    private static final double EARTH_RADIUS_KM = 6371.0;

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

        double distanceKm = haversineDistanceKm(
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

    // Standard great-circle distance formula between two lat/long points.
    private double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public String ruleName() {
        return "GEO_MISMATCH";
    }
}
