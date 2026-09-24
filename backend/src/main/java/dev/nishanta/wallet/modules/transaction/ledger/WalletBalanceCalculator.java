package dev.nishanta.wallet.modules.transaction.ledger;

import dev.nishanta.wallet.modules.transaction.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletBalanceCalculator implements BalanceCalculator {

    private final WalletRepository walletRepository;

    public WalletBalanceCalculator(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public BigDecimal calculateBalance(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(dev.nishanta.wallet.modules.wallet.domain.Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }
}
