package dev.nishanta.wallet.dev;

import dev.nishanta.wallet.transaction.domain.EntryType;
import dev.nishanta.wallet.transaction.domain.LedgerEntry;
import dev.nishanta.wallet.transaction.domain.Transaction;
import dev.nishanta.wallet.transaction.repository.LedgerEntryRepository;
import dev.nishanta.wallet.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.user.domain.Role;
import dev.nishanta.wallet.user.domain.User;
import dev.nishanta.wallet.user.repository.UserRepository;
import dev.nishanta.wallet.wallet.domain.Wallet;
import dev.nishanta.wallet.wallet.domain.WalletType;
import dev.nishanta.wallet.wallet.repository.WalletRepository;
import dev.nishanta.wallet.wallet.service.MintWalletProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SeedService {

    private final MintWalletProvider mintWalletProvider;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public SeedService(MintWalletProvider mintWalletProvider,
                       UserRepository userRepository,
                       WalletRepository walletRepository,
                       TransactionRepository transactionRepository,
                       LedgerEntryRepository ledgerEntryRepository) {
        this.mintWalletProvider = mintWalletProvider;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public SeedResponse seed() {
        Wallet mintWallet = mintWalletProvider.findOrCreateMintWallet();

        User alice = findOrCreateUser("Alice Test", "alice@test.com", Role.USER, "9800000001");
        Wallet aliceWallet = findOrCreateWallet(alice);

        User bob = findOrCreateUser("Bob Test", "bob@test.com", Role.USER, "9800000002");
        Wallet bobWallet = findOrCreateWallet(bob);

        // Seed alice with some starting balance, so transfers can be tested.
        Transaction seedTx = new Transaction(
                "seed-" + System.currentTimeMillis(), mintWallet, aliceWallet,
                new BigDecimal("1000.0000"), "NPR", null, null);
        transactionRepository.save(seedTx);
        ledgerEntryRepository.save(new LedgerEntry(
                seedTx, mintWallet, new BigDecimal("-1000.0000"), EntryType.DEBIT, "NPR"));
        ledgerEntryRepository.save(new LedgerEntry(
                seedTx, aliceWallet, new BigDecimal("1000.0000"), EntryType.CREDIT, "NPR"));
        seedTx.markCompleted();
        transactionRepository.save(seedTx);

        return new SeedResponse(aliceWallet.getId().toString(), bobWallet.getId().toString());
    }

    private User findOrCreateUser(String name, String email, Role role, String phone) {
        return userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseGet(() -> userRepository.save(new User(name, email, "placeholder", role, phone)));
    }

    private Wallet findOrCreateWallet(User user) {
        return walletRepository.findAll().stream()
                .filter(w -> w.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseGet(() -> walletRepository.save(new Wallet(user, WalletType.PERSONAL, "NPR")));
    }
}
