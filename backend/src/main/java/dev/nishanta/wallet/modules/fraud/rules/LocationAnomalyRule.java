package dev.nishanta.wallet.modules.fraud.rules;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.fraud.domain.FraudSeverity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class LocationAnomalyRule implements FraudRule {

    private final TransactionRepository transactionRepository;
    private final dev.nishanta.wallet.modules.fraud.repository.FraudConfigRepository fraudConfigRepository;

    public LocationAnomalyRule(TransactionRepository transactionRepository,
                               dev.nishanta.wallet.modules.fraud.repository.FraudConfigRepository fraudConfigRepository) {
        this.transactionRepository = transactionRepository;
        this.fraudConfigRepository = fraudConfigRepository;
    }

    @Override
    public FraudSeverity evaluate(Transaction transaction) {
        if (transaction.getLatitude() == null || transaction.getLongitude() == null) {
            return FraudSeverity.NONE;
        }

        Optional<Transaction> lastTxOpt = transactionRepository
                .findFirstByFromWalletIdAndIdNotAndLatitudeIsNotNullOrderByCreatedAtDesc(
                        transaction.getFromWallet().getId(), transaction.getId());

        if (lastTxOpt.isEmpty()) {
            return FraudSeverity.NONE;
        }

        Transaction lastTx = lastTxOpt.get();

        double distance = calculateDistance(
                transaction.getLatitude().doubleValue(), transaction.getLongitude().doubleValue(),
                lastTx.getLatitude().doubleValue(), lastTx.getLongitude().doubleValue()
        );

        dev.nishanta.wallet.modules.fraud.domain.FraudConfig config = fraudConfigRepository.findById(1).get();

        if (distance > config.getMaxGeoDistanceKm() * 3) {
            return FraudSeverity.MAJOR;
        }
        if (distance > config.getMaxGeoDistanceKm()) {
            return FraudSeverity.MINOR;
        }
        return FraudSeverity.NONE;
    }

    @Override
    public String ruleName() {
        return "LOCATION_ANOMALY";
    }

    // Haversine formula to calculate distance between two lat/lon points in km
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
