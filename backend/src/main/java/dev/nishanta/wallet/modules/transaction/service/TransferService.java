package dev.nishanta.wallet.modules.transaction.service;

import dev.nishanta.wallet.common.exception.InsufficientBalanceException;
import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.fraud.domain.FraudFlag;
import dev.nishanta.wallet.modules.fraud.repository.FraudFlagRepository;
import dev.nishanta.wallet.modules.fraud.rules.FraudRule;
import dev.nishanta.wallet.modules.transaction.domain.EntryType;
import dev.nishanta.wallet.modules.transaction.domain.LedgerEntry;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.dto.TransferRequest;
import dev.nishanta.wallet.modules.transaction.dto.TransferResponse;
import dev.nishanta.wallet.modules.transaction.ledger.WalletBalanceCalculator;
import dev.nishanta.wallet.modules.transaction.repository.LedgerEntryRepository;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransferService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletBalanceCalculator balanceCalculator;
    private final FraudFlagRepository fraudFlagRepository;
    private final List<FraudRule> fraudRules;

    public TransferService(TransactionRepository transactionRepository,
                           WalletRepository walletRepository,
                           LedgerEntryRepository ledgerEntryRepository,
                           WalletBalanceCalculator balanceCalculator,
                           FraudFlagRepository fraudFlagRepository,
                           List<FraudRule> fraudRules) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.balanceCalculator = balanceCalculator;
        this.fraudFlagRepository = fraudFlagRepository;
        this.fraudRules = fraudRules;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        String idempotencyKey = request.idempotencyKey();
        UUID fromWalletId = request.fromWalletId();
        UUID toWalletId = request.toWalletId();
        BigDecimal amount = request.amount();

        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        UUID firstLockId = fromWalletId.compareTo(toWalletId) < 0 ? fromWalletId : toWalletId;
        UUID secondLockId = fromWalletId.compareTo(toWalletId) < 0 ? toWalletId : fromWalletId;

        Wallet firstLocked = walletRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + firstLockId));
        Wallet secondLocked = walletRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + secondLockId));

        Wallet fromWallet = firstLockId.equals(fromWalletId) ? firstLocked : secondLocked;
        Wallet toWallet = firstLockId.equals(fromWalletId) ? secondLocked : firstLocked;

        BigDecimal senderBalance = balanceCalculator.calculateBalance(fromWallet.getId());
        if (senderBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in wallet " + fromWallet.getId());
        }

        // Transaction is created and saved FIRST, as PENDING — this
        // exists regardless of outcome, so there's always a record of
        // the attempt, and fraud rules that query transaction history
        // have something to reason about.
        Transaction transaction = new Transaction(
                idempotencyKey, fromWallet, toWallet, amount, fromWallet.getCurrency(),
                request.latitude(), request.longitude());
        transactionRepository.save(transaction);

        // FRAUD CHECK NOW HAPPENS HERE — before any money actually
        // moves. This is the fix: a flagged transaction must not have
        // already changed either wallet's balance, or "held for review"
        // would be a lie.
        List<FraudRule> firedRules = fraudRules.stream()
                .filter(rule -> rule.isSuspicious(transaction))
                .collect(Collectors.toList());

        if (!firedRules.isEmpty()) {
            String combinedRuleNames = firedRules.stream()
                    .map(FraudRule::ruleName)
                    .collect(Collectors.joining(","));
            int riskScore = firedRules.size() * 10;

            FraudFlag flag = new FraudFlag(transaction, combinedRuleNames, riskScore);
            fraudFlagRepository.save(flag);

            transaction.markFlagged();
            transactionRepository.save(transaction);

            // Deliberately stop here — NO LedgerEntry rows get created.
            // The money hasn't moved. It only moves once an admin
            // approves this transaction.
            return toResponse(transaction);
        }

        // Only a genuinely clean transaction reaches this point, where
        // the ledger entries actually get written.
        LedgerEntry debit = new LedgerEntry(
                transaction, fromWallet, amount.negate(), EntryType.DEBIT, fromWallet.getCurrency());
        LedgerEntry credit = new LedgerEntry(
                transaction, toWallet, amount, EntryType.CREDIT, toWallet.getCurrency());
        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        transaction.markCompleted();
        transactionRepository.save(transaction);

        return toResponse(transaction);
    }

    private TransferResponse toResponse(Transaction tx) {
        return new TransferResponse(
                tx.getId(),
                tx.getStatus().name(),
                tx.getFromWallet().getId(),
                tx.getToWallet().getId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getCreatedAt()
        );
    }
}
