package dev.nishanta.wallet.transaction.ledger;

import dev.nishanta.wallet.transaction.repository.LedgerEntryRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

// Single job: given a wallet's id, compute its current balance.
// Balance is NEVER stored — it's always derived by summing every
// ledger entry for that wallet (debits are negative, credits are
// positive, so a plain sum gives the correct running balance).
@Component
public class WalletBalanceCalculator {

    private final LedgerEntryRepository ledgerEntryRepository;

    // Spring injects the repository automatically — we don't construct it ourselves.
    public WalletBalanceCalculator(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public BigDecimal calculateBalance(UUID walletId) {
        return ledgerEntryRepository.findByWalletId(walletId).stream()
                .map(entry -> entry.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
