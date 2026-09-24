package dev.nishanta.wallet.modules.fraud.rules;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.domain.TransactionStatus;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.fraud.domain.FraudSeverity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

// Relative, not fixed — compares this transaction against THIS wallet's
// own historical average, not a number applied to everyone equally.
// A business that regularly sends 50,000 won't get flagged for sending
// 50,000 again; a wallet that's never sent more than 500 suddenly
// sending 40,000 will, even though 40,000 is a smaller absolute number.
@Component
public class AmountThresholdRule implements FraudRule {

    private final TransactionRepository transactionRepository;
    private final dev.nishanta.wallet.modules.fraud.repository.FraudConfigRepository fraudConfigRepository;

    public AmountThresholdRule(TransactionRepository transactionRepository,
                               dev.nishanta.wallet.modules.fraud.repository.FraudConfigRepository fraudConfigRepository) {
        this.transactionRepository = transactionRepository;
        this.fraudConfigRepository = fraudConfigRepository;
    }

    @Override
    public FraudSeverity evaluate(Transaction transaction) {
        UUID walletId = transaction.getFromWallet().getId();
        long historyCount = transactionRepository.countByFromWalletIdAndStatus(
                walletId, TransactionStatus.COMPLETED);

        dev.nishanta.wallet.modules.fraud.domain.FraudConfig config = fraudConfigRepository.findById(1).orElseGet(dev.nishanta.wallet.modules.fraud.domain.FraudConfig::createDefault);

        if (historyCount < config.getMinHistoryForBaseline()) {
            if (transaction.getAmount().compareTo(config.getColdStartThreshold().multiply(new BigDecimal("2"))) > 0) {
                return FraudSeverity.MAJOR;
            }
            if (transaction.getAmount().compareTo(config.getColdStartThreshold()) > 0) {
                return FraudSeverity.MINOR;
            }
            return FraudSeverity.NONE;
        }

        BigDecimal average = transactionRepository.findAverageAmountByFromWalletId(walletId)
                .orElse(BigDecimal.ZERO);

        BigDecimal minorThreshold = average.multiply(config.getAverageMultiplier());
        BigDecimal majorThreshold = average.multiply(config.getAverageMultiplier().add(new BigDecimal("2")));

        if (transaction.getAmount().compareTo(majorThreshold) > 0) {
            return FraudSeverity.MAJOR;
        }
        if (transaction.getAmount().compareTo(minorThreshold) > 0) {
            return FraudSeverity.MINOR;
        }
        return FraudSeverity.NONE;
    }

    @Override
    public String ruleName() {
        return "AMOUNT_THRESHOLD";
    }
}
