package dev.nishanta.wallet.controller;

import dev.nishanta.wallet.domain.Transaction;
import dev.nishanta.wallet.domain.Wallet;
import dev.nishanta.wallet.dto.BalanceResponse;
import dev.nishanta.wallet.dto.DepositRequest;
import dev.nishanta.wallet.ledger.WalletBalanceCalculator;
import dev.nishanta.wallet.repository.WalletRepository;
import dev.nishanta.wallet.service.DepositService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletRepository walletRepository;
    private final WalletBalanceCalculator balanceCalculator;
    private final DepositService depositService;

    public WalletController(WalletRepository walletRepository, WalletBalanceCalculator balanceCalculator,
                            DepositService depositService) {
        this.walletRepository = walletRepository;
        this.balanceCalculator = balanceCalculator;
        this.depositService = depositService;
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

        var balance = balanceCalculator.calculateBalance(walletId);

        return ResponseEntity.ok(new BalanceResponse(walletId, balance, wallet.getCurrency()));
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<BalanceResponse> deposit(@PathVariable UUID walletId,
                                                   @RequestBody DepositRequest request) {
        Transaction result = depositService.deposit(request.idempotencyKey(), walletId, request.amount());
        Wallet wallet = result.getToWallet();
        var newBalance = balanceCalculator.calculateBalance(walletId);
        return ResponseEntity.ok(new BalanceResponse(walletId, newBalance, wallet.getCurrency()));
    }
}