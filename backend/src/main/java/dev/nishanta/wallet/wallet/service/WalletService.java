package dev.nishanta.wallet.wallet.service;

import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.transaction.ledger.WalletBalanceCalculator;
import dev.nishanta.wallet.wallet.domain.Wallet;
import dev.nishanta.wallet.wallet.dto.BalanceResponse;
import dev.nishanta.wallet.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletBalanceCalculator balanceCalculator;

    public WalletService(WalletRepository walletRepository, WalletBalanceCalculator balanceCalculator) {
        this.walletRepository = walletRepository;
        this.balanceCalculator = balanceCalculator;
    }

    public BalanceResponse getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + walletId));

        var balance = balanceCalculator.calculateBalance(walletId);
        return new BalanceResponse(walletId, balance, wallet.getCurrency());
    }
}
