package dev.nishanta.wallet.modules.fraud.rules;

import dev.nishanta.wallet.modules.fraud.domain.FraudConfig;
import dev.nishanta.wallet.modules.fraud.domain.FraudSeverity;
import dev.nishanta.wallet.modules.fraud.repository.FraudConfigRepository;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.domain.TransactionStatus;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AmountThresholdRuleTest {

    @Mock
    private TransactionRepository transactionRepository;



    @InjectMocks
    private AmountThresholdRule rule;

    private FraudConfig config;
    private Wallet fromWallet;
    private Transaction transaction;

    @BeforeEach
    public void setup() {
        config = FraudConfig.createDefault();
        fromWallet = mock(Wallet.class);
        when(fromWallet.getId()).thenReturn(UUID.randomUUID());

        transaction = mock(Transaction.class);
        when(transaction.getFromWallet()).thenReturn(fromWallet);


    }

    @Test
    public void evaluate_coldStartMajorFraud() {
        when(fromWallet.getTransactionCount()).thenReturn(1L);

        // config.getColdStartThreshold() is 50000.
        // > 100000 -> MAJOR
        when(transaction.getAmount()).thenReturn(new BigDecimal("100001"));

        assertEquals(FraudSeverity.MAJOR, rule.evaluate(transaction, config));
    }

    @Test
    public void evaluate_coldStartMinorFraud() {
        when(fromWallet.getTransactionCount()).thenReturn(1L);

        // > 50000 and <= 100000 -> MINOR
        when(transaction.getAmount()).thenReturn(new BigDecimal("60000"));

        assertEquals(FraudSeverity.MINOR, rule.evaluate(transaction, config));
    }

    @Test
    public void evaluate_historyBaselineMajorFraud() {
        when(fromWallet.getTransactionCount()).thenReturn(10L); // >= minHistoryForBaseline

        // Average is 1000. Multiplier is 5.
        // Minor = 1000 * 5 = 5000
        // Major = 1000 * (5 + 2) = 7000
        when(fromWallet.getTotalTransactionVolume())
                .thenReturn(new BigDecimal("10000")); // 1000 * 10
        
        when(transaction.getAmount()).thenReturn(new BigDecimal("7001"));

        assertEquals(FraudSeverity.MAJOR, rule.evaluate(transaction, config));
    }

    @Test
    public void evaluate_historyBaselineClean() {
        when(fromWallet.getTransactionCount()).thenReturn(10L);

        when(fromWallet.getTotalTransactionVolume())
                .thenReturn(new BigDecimal("10000")); // 1000 * 10
        
        when(transaction.getAmount()).thenReturn(new BigDecimal("4999"));

        assertEquals(FraudSeverity.NONE, rule.evaluate(transaction, config));
    }
}
