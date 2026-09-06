package dev.nishanta.wallet.modules.wallet.controller;

import dev.nishanta.wallet.modules.transaction.service.DepositService;
import dev.nishanta.wallet.modules.wallet.dto.BalanceResponse;
import dev.nishanta.wallet.modules.wallet.dto.DepositRequest;
import dev.nishanta.wallet.modules.wallet.service.WalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.nishanta.wallet.common.constant.ApiRoutes;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiRoutes.WALLET_BASE)
public class WalletController {

    private final WalletService walletService;
    private final DepositService depositService;

    public WalletController(WalletService walletService, DepositService depositService) {
        this.walletService = walletService;
        this.depositService = depositService;
    }

    @GetMapping(ApiRoutes.WALLET_BALANCE)
    public BalanceResponse getBalance(@PathVariable UUID walletId) {
        return walletService.getBalance(walletId);
    }

    @PostMapping(ApiRoutes.WALLET_DEPOSIT)
    public BalanceResponse deposit(@PathVariable UUID walletId, @RequestBody DepositRequest request) {
        return depositService.deposit(walletId, request);
    }

    @GetMapping("/api/v1/wallets/{walletId}/transactions")
    public List<Transaction> getTransactions(@PathVariable UUID walletId) {
        return walletService.getTransactions(walletId);
    }
}
