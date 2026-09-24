package dev.nishanta.wallet.modules.wallet.controller;

import dev.nishanta.wallet.modules.transaction.service.DepositService;
import dev.nishanta.wallet.modules.transaction.service.WithdrawService;
import dev.nishanta.wallet.modules.wallet.dto.BalanceResponse;
import dev.nishanta.wallet.modules.wallet.dto.DepositRequest;
import dev.nishanta.wallet.modules.wallet.dto.WithdrawRequest;
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
    private final WithdrawService withdrawService;

    public WalletController(WalletService walletService, DepositService depositService, WithdrawService withdrawService) {
        this.walletService = walletService;
        this.depositService = depositService;
        this.withdrawService = withdrawService;
    }

    @GetMapping(ApiRoutes.WALLET_BALANCE)
    public BalanceResponse getBalance(@PathVariable UUID walletId) {
        return walletService.getBalance(walletId);
    }

    @PostMapping(ApiRoutes.WALLET_DEPOSIT)
    public BalanceResponse deposit(@PathVariable UUID walletId, @RequestBody DepositRequest request) {
        return depositService.deposit(walletId, request);
    }

    @PostMapping(ApiRoutes.WALLET_WITHDRAW)
    public BalanceResponse withdraw(@PathVariable UUID walletId, @RequestBody WithdrawRequest request) {
        return withdrawService.withdraw(walletId, request);
    }

    @GetMapping("/{walletId}/transactions")
    public org.springframework.data.domain.Page<Transaction> getTransactions(
            @PathVariable UUID walletId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        return walletService.getTransactions(walletId, org.springframework.data.domain.PageRequest.of(page, size));
    }
}
