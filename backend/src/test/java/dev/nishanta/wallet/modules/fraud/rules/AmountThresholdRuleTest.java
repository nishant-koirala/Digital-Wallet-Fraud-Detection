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

    @Mock
    private FraudConfigRepository fraudConfigRepository;

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

        when(fraudConfigRepository.findById(1)).thenReturn(Optional.of(config));
    }

    @Test
    public void evaluate_coldStartMajorFraud() {
        when(transactionRepository.countByFromWalletIdAndStatus(fromWallet.getId(), TransactionStatus.COMPLETED))
                .thenReturn(1L); // < minHistoryForBaseline (5)

        // config.getColdStartThreshold() is 50000.
        // > 100000 -> MAJOR
        when(transaction.getAmount()).thenReturn(new BigDecimal("100001"));

        assertEquals(FraudSeverity.MAJOR, rule.evaluate(transaction));
    }

    @Test
    public void evaluate_coldStartMinorFraud() {
        when(transactionRepository.countByFromWalletIdAndStatus(fromWallet.getId(), TransactionStatus.COMPLETED))
                .thenReturn(1L);

        // > 50000 and <= 100000 -> MINOR
        when(transaction.getAmount()).thenReturn(new BigDecimal("60000"));

        assertEquals(FraudSeverity.MINOR, rule.evaluate(transaction));
    }

    @Test
    public void evaluate_historyBaselineMajorFraud() {
        when(transactionRepository.countByFromWalletIdAndStatus(fromWallet.getId(), TransactionStatus.COMPLETED))
                .thenReturn(10L); // >= minHistoryForBaseline

        // Average is 1000. Multiplier is 5.
        // Minor = 1000 * 5 = 5000
        // Major = 1000 * (5 + 2) = 7000
        when(transactionRepository.findAverageAmountByFromWalletId(fromWallet.getId()))
                .thenReturn(Optional.of(new BigDecimal("1000")));
        
        when(transaction.getAmount()).thenReturn(new BigDecimal("7001"));

        assertEquals(FraudSeverity.MAJOR, rule.evaluate(transaction));
    }

    @Test
    public void evaluate_historyBaselineClean() {
        when(transactionRepository.countByFromWalletIdAndStatus(fromWallet.getId(), TransactionStatus.COMPLETED))
                .thenReturn(10L);

        when(transactionRepository.findAverageAmountByFromWalletId(fromWallet.getId()))
                .thenReturn(Optional.of(new BigDecimal("1000")));
        
        when(transaction.getAmount()).thenReturn(new BigDecimal("4999"));

        assertEquals(FraudSeverity.NONE, rule.evaluate(transaction));
    }
}
