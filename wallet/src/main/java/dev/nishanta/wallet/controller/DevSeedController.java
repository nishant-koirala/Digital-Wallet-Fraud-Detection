package dev.nishanta.wallet.controller;

import dev.nishanta.wallet.domain.*;
import dev.nishanta.wallet.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

// TEMPORARY, dev-only convenience — creates two funded test wallets in
// one call instead of hand-writing SQL inserts. Delete this whole file
// once a real registration endpoint exists.
@RestController
@RequestMapping("/api/v1/dev")
public class DevSeedController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public DevSeedController(UserRepository userRepository, WalletRepository walletRepository,
                             TransactionRepository transactionRepository,
                             LedgerEntryRepository ledgerEntryRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, String>> seed() {
        // Mint "wallet" representing money entering the system from outside.
        User mintUser = new User("System Mint", "mint@internal", "n/a", Role.ADMIN);
        userRepository.save(mintUser);
        Wallet mintWallet = new Wallet(mintUser, WalletType.PERSONAL, "NPR");
        walletRepository.save(mintWallet);

        User alice = new User("Alice Test", "alice@test.com", "placeholder", Role.USER);
        userRepository.save(alice);
        Wallet aliceWallet = new Wallet(alice, WalletType.PERSONAL, "NPR");
        walletRepository.save(aliceWallet);

        User bob = new User("Bob Test", "bob@test.com", "placeholder", Role.USER);
        userRepository.save(bob);
        Wallet bobWallet = new Wallet(bob, WalletType.PERSONAL, "NPR");
        walletRepository.save(bobWallet);

        // Fund Alice with 1000 via a real transaction, same as any transfer would be.
        Transaction seedTx = new Transaction(
                "seed-" + System.currentTimeMillis(), mintWallet, aliceWallet,
                new BigDecimal("1000.0000"), "NPR");
        transactionRepository.save(seedTx);
        ledgerEntryRepository.save(new LedgerEntry(
                seedTx, mintWallet, new BigDecimal("-1000.0000"), EntryType.DEBIT, "NPR"));
        ledgerEntryRepository.save(new LedgerEntry(
                seedTx, aliceWallet, new BigDecimal("1000.0000"), EntryType.CREDIT, "NPR"));
        seedTx.markCompleted();
        transactionRepository.save(seedTx);

        return ResponseEntity.ok(Map.of(
                "aliceWalletId", aliceWallet.getId().toString(),
                "bobWalletId", bobWallet.getId().toString()
        ));
    }
}