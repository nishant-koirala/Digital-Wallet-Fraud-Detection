package dev.nishanta.wallet.service;

import dev.nishanta.wallet.domain.*;
import dev.nishanta.wallet.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DepositService {

    // Well-known marker for the system mint account — same pattern
    // used while manually seeding earlier, just formalized here.
    private static final String MINT_EMAIL = "mint@internal";

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public DepositService(UserRepository userRepository, WalletRepository walletRepository,
                          TransactionRepository transactionRepository,
                          LedgerEntryRepository ledgerEntryRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public Transaction deposit(String idempotencyKey, UUID targetWalletId, BigDecimal amount) {

        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        Wallet mintWallet = findOrCreateMintWallet();

        // Same ordered-locking pattern as TransferService, for the same
        // reason — the mint wallet gets touched by EVERY deposit, so
        // without a consistent lock order, concurrent deposits into
        // different wallets could deadlock against each other on mint.
        UUID mintId = mintWallet.getId();
        UUID firstLockId = mintId.compareTo(targetWalletId) < 0 ? mintId : targetWalletId;
        UUID secondLockId = mintId.compareTo(targetWalletId) < 0 ? targetWalletId : mintId;

        Wallet firstLocked = walletRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + firstLockId));
        Wallet secondLocked = walletRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + secondLockId));

        Wallet lockedMint = firstLockId.equals(mintId) ? firstLocked : secondLocked;
        Wallet targetWallet = firstLockId.equals(mintId) ? secondLocked : firstLocked;

        // NO balance check here — this is the whole point of a mint
        // account. It's allowed to go negative; that negative number IS
        // the running total of real money the system has ever injected.

        Transaction transaction = new Transaction(
                idempotencyKey, lockedMint, targetWallet, amount, targetWallet.getCurrency());
        transactionRepository.save(transaction);

        LedgerEntry debit = new LedgerEntry(
                transaction, lockedMint, amount.negate(), EntryType.DEBIT, lockedMint.getCurrency());
        LedgerEntry credit = new LedgerEntry(
                transaction, targetWallet, amount, EntryType.CREDIT, targetWallet.getCurrency());
        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        // Deliberately no fraud check here — your existing rules
        // (AmountThresholdRule, VelocityRule) work by looking at a
        // WALLET's own history. The mint wallet's "history" isn't a
        // meaningful signal the same way a real user's is, so running
        // those rules against every deposit wouldn't produce anything
        // useful right now. Worth revisiting once you have real deposit
        // volume patterns to actually calibrate against.
        transaction.markCompleted();
        transactionRepository.save(transaction);

        return transaction;
    }

    // Finds the mint wallet if it already exists, or creates it the
    // first time it's needed. This means you no longer depend on
    // DevSeedController having run first — deposits work standalone.
    private Wallet findOrCreateMintWallet() {
        var existingUser = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(MINT_EMAIL))
                .findFirst();

        User mintUser = existingUser.orElseGet(() -> {
            User newUser = new User("System Mint", MINT_EMAIL, "n/a", Role.ADMIN);
            userRepository.save(newUser);
            return newUser;
        });

        var existingWallet = walletRepository.findAll().stream()
                .filter(w -> w.getUser().getId().equals(mintUser.getId()))
                .findFirst();

        return existingWallet.orElseGet(() -> {
            Wallet newWallet = new Wallet(mintUser, WalletType.PERSONAL, "NPR");
            walletRepository.save(newWallet);
            return newWallet;
        });
    }
}