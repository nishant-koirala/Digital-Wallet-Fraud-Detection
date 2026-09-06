package dev.nishanta.wallet.modules.wallet.service;

import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.transaction.ledger.WalletBalanceCalculator;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.dto.BalanceResponse;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletBalanceCalculator balanceCalculator;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, WalletBalanceCalculator balanceCalculator, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.balanceCalculator = balanceCalculator;
        this.transactionRepository = transactionRepository;
    }

    public BalanceResponse getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + walletId));

        var balance = balanceCalculator.calculateBalance(walletId);
        return new BalanceResponse(walletId, balance, wallet.getCurrency());
    }

    public List<Transaction> getTransactions(UUID walletId) {
        walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + walletId));
        return transactionRepository.findRecentByWalletId(walletId);
    }
}
