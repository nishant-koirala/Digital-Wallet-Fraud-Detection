package dev.nishanta.wallet.modules.fraud.rules;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class LocationAnomalyRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    public LocationAnomalyRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public boolean isSuspicious(Transaction transaction) {
        if (transaction.getLatitude() == null || transaction.getLongitude() == null) {
            return false;
        }

        Optional<Transaction> lastTxOpt = transactionRepository
                .findFirstByFromWalletIdAndIdNotAndLatitudeIsNotNullOrderByCreatedAtDesc(
                        transaction.getFromWallet().getId(), transaction.getId());

        if (lastTxOpt.isEmpty()) {
            return false;
        }

        Transaction lastTx = lastTxOpt.get();

        double distance = calculateDistance(
                transaction.getLatitude().doubleValue(), transaction.getLongitude().doubleValue(),
                lastTx.getLatitude().doubleValue(), lastTx.getLongitude().doubleValue()
        );

        // If distance is greater than 500km, flag as suspicious
        return distance > 500;
    }

    @Override
    public String ruleName() {
        return "GEO_MISMATCH";
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
