package dev.nishanta.wallet.transaction.service;

import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.transaction.domain.EntryType;
import dev.nishanta.wallet.transaction.domain.LedgerEntry;
import dev.nishanta.wallet.transaction.domain.Transaction;
import dev.nishanta.wallet.transaction.ledger.WalletBalanceCalculator;
import dev.nishanta.wallet.transaction.repository.LedgerEntryRepository;
import dev.nishanta.wallet.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.wallet.domain.Wallet;
import dev.nishanta.wallet.wallet.dto.BalanceResponse;
import dev.nishanta.wallet.wallet.dto.DepositRequest;
import dev.nishanta.wallet.wallet.repository.WalletRepository;
import dev.nishanta.wallet.wallet.service.MintWalletProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DepositService {

    private final MintWalletProvider mintWalletProvider;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletBalanceCalculator balanceCalculator;

    public DepositService(MintWalletProvider mintWalletProvider,
                          WalletRepository walletRepository,
                          TransactionRepository transactionRepository,
                          LedgerEntryRepository ledgerEntryRepository,
                          WalletBalanceCalculator balanceCalculator) {
        this.mintWalletProvider = mintWalletProvider;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.balanceCalculator = balanceCalculator;
    }

    @Transactional
    public BalanceResponse deposit(UUID targetWalletId, DepositRequest request) {
        String idempotencyKey = request.idempotencyKey();
        BigDecimal amount = request.amount();

        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(targetWalletId, existing.get().getCurrency());
        }

        Wallet mintWallet = mintWalletProvider.findOrCreateMintWallet();

        UUID mintId = mintWallet.getId();
        UUID firstLockId = mintId.compareTo(targetWalletId) < 0 ? mintId : targetWalletId;
        UUID secondLockId = mintId.compareTo(targetWalletId) < 0 ? targetWalletId : mintId;

        Wallet firstLocked = walletRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + firstLockId));
        Wallet secondLocked = walletRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + secondLockId));

        Wallet lockedMint = firstLockId.equals(mintId) ? firstLocked : secondLocked;
        Wallet targetWallet = firstLockId.equals(mintId) ? secondLocked : firstLocked;

        Transaction transaction = new Transaction(
                idempotencyKey, lockedMint, targetWallet, amount, targetWallet.getCurrency(), null, null);
        transactionRepository.save(transaction);

        LedgerEntry debit = new LedgerEntry(
                transaction, lockedMint, amount.negate(), EntryType.DEBIT, lockedMint.getCurrency());
        LedgerEntry credit = new LedgerEntry(
                transaction, targetWallet, amount, EntryType.CREDIT, targetWallet.getCurrency());
        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        transaction.markCompleted();
        transactionRepository.save(transaction);

        return toResponse(targetWalletId, targetWallet.getCurrency());
    }

    private BalanceResponse toResponse(UUID walletId, String currency) {
        return new BalanceResponse(walletId, balanceCalculator.calculateBalance(walletId), currency);
    }
}
