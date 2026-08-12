package dev.nishanta.wallet.transaction.ledger;

import java.math.BigDecimal;
import java.util.UUID;

// Contract for deriving a wallet's current balance. Services depend on
// this interface rather than the concrete summation implementation, so
// the balance strategy can evolve without touching callers.
public interface BalanceCalculator {

    BigDecimal calculateBalance(UUID walletId);
}
