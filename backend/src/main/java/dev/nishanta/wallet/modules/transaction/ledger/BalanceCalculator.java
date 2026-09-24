package dev.nishanta.wallet.modules.transaction.ledger;

import java.math.BigDecimal;
import java.util.UUID;

public interface BalanceCalculator {
    BigDecimal calculateBalance(UUID walletId);
}
