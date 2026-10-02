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

    public LocationAnomalyRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public FraudSeverity evaluate(Transaction transaction, dev.nishanta.wallet.modules.fraud.domain.FraudConfig config) {
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

        double distance = dev.nishanta.wallet.modules.fraud.util.GeoUtils.haversineDistanceKm(
                transaction.getLatitude().doubleValue(), transaction.getLongitude().doubleValue(),
                lastTx.getLatitude().doubleValue(), lastTx.getLongitude().doubleValue()
        );



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


}
