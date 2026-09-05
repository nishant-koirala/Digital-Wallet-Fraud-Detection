package dev.nishanta.wallet.modules.fraud.rules;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.domain.TransactionStatus;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
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

    // Safety net ONLY for brand-new wallets with no transaction history
    // yet — there's no "normal" to compare against, so fall back to a
    // flat threshold until enough history exists.
    private static final BigDecimal COLD_START_THRESHOLD = new BigDecimal("50000.0000");
    private static final int MIN_HISTORY_FOR_BASELINE = 5;
    private static final BigDecimal MULTIPLIER = new BigDecimal("5");

    private final TransactionRepository transactionRepository;

    public AmountThresholdRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public boolean isSuspicious(Transaction transaction) {
        UUID walletId = transaction.getFromWallet().getId();
        long historyCount = transactionRepository.countByFromWalletIdAndStatus(
                walletId, TransactionStatus.COMPLETED);

        if (historyCount < MIN_HISTORY_FOR_BASELINE) {
            return transaction.getAmount().compareTo(COLD_START_THRESHOLD) > 0;
        }

        BigDecimal average = transactionRepository.findAverageAmountByFromWalletId(walletId)
                .orElse(BigDecimal.ZERO);

        BigDecimal relativeThreshold = average.multiply(MULTIPLIER);
        return transaction.getAmount().compareTo(relativeThreshold) > 0;
    }

    @Override
    public String ruleName() {
        return "AMOUNT_THRESHOLD";
    }
}
