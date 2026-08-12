package dev.nishanta.wallet.wallet.controller;

import dev.nishanta.wallet.constant.ApiConstants;
import dev.nishanta.wallet.transaction.service.DepositService;
import dev.nishanta.wallet.wallet.dto.BalanceResponse;
import dev.nishanta.wallet.wallet.dto.DepositRequest;
import dev.nishanta.wallet.wallet.service.WalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.Wallet.BASE)
public class WalletController {

    private final WalletService walletService;
    private final DepositService depositService;

    public WalletController(WalletService walletService, DepositService depositService) {
        this.walletService = walletService;
        this.depositService = depositService;
    }

    @GetMapping("/{walletId}/balance")
    public BalanceResponse getBalance(@PathVariable UUID walletId) {
        return walletService.getBalance(walletId);
    }

    @PostMapping("/{walletId}/deposit")
    public BalanceResponse deposit(@PathVariable UUID walletId, @RequestBody DepositRequest request) {
        return depositService.deposit(walletId, request);
    }
}
