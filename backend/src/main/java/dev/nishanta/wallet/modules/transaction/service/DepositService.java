package dev.nishanta.wallet.modules.transaction.service;

import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.transaction.domain.EntryType;
import dev.nishanta.wallet.modules.transaction.domain.LedgerEntry;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.ledger.BalanceCalculator;
import dev.nishanta.wallet.modules.transaction.repository.LedgerEntryRepository;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.dto.BalanceResponse;
import dev.nishanta.wallet.modules.wallet.dto.DepositRequest;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import dev.nishanta.wallet.modules.wallet.service.MintWalletProvider;
import dev.nishanta.wallet.modules.audit.service.AuditService;
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
    private final BalanceCalculator balanceCalculator;
    private final AuditService auditService;

    public DepositService(MintWalletProvider mintWalletProvider,
                          WalletRepository walletRepository,
                          TransactionRepository transactionRepository,
                          LedgerEntryRepository ledgerEntryRepository,
                          BalanceCalculator balanceCalculator,
                          AuditService auditService) {
        this.mintWalletProvider = mintWalletProvider;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.balanceCalculator = balanceCalculator;
        this.auditService = auditService;
    }

    private void verifyWalletOwnership(Wallet wallet) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !wallet.getUser().getEmail().equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to use this wallet");
        }
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

        verifyWalletOwnership(targetWallet);

        Transaction transaction = new Transaction(
                idempotencyKey, lockedMint, targetWallet, amount, targetWallet.getCurrency(), null, null, null, null);
        transactionRepository.save(transaction);

        LedgerEntry debit = new LedgerEntry(
                transaction, lockedMint, amount.negate(), EntryType.DEBIT, lockedMint.getCurrency());
        LedgerEntry credit = new LedgerEntry(
                transaction, targetWallet, amount, EntryType.CREDIT, targetWallet.getCurrency());
        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        transaction.markCompleted();
        transactionRepository.save(transaction);

        auditService.logAction("TRANSACTION", transaction.getId(), "DEPOSIT", "SYSTEM", null, request);

        return toResponse(targetWalletId, targetWallet.getCurrency());
    }

    private BalanceResponse toResponse(UUID walletId, String currency) {
        return new BalanceResponse(walletId, balanceCalculator.calculateBalance(walletId), currency);
    }
}
