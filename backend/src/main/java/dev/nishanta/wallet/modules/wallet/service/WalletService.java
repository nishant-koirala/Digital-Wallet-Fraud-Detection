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

    private void verifyWalletOwnership(Wallet wallet) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !wallet.getUser().getEmail().equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to access this wallet");
        }
    }

    public BalanceResponse getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + walletId));
        verifyWalletOwnership(wallet);

        var balance = balanceCalculator.calculateBalance(walletId);
        return new BalanceResponse(walletId, balance, wallet.getCurrency());
    }

    public List<Transaction> getTransactions(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + walletId));
        verifyWalletOwnership(wallet);
        return transactionRepository.findRecentByWalletId(walletId);
    }
}
