package dev.nishanta.wallet.transaction.ledger;

import dev.nishanta.wallet.transaction.domain.EntryType;
import dev.nishanta.wallet.transaction.domain.LedgerEntry;
import dev.nishanta.wallet.transaction.domain.Transaction;
import dev.nishanta.wallet.transaction.repository.LedgerEntryRepository;
import dev.nishanta.wallet.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.wallet.domain.Wallet;
import org.springframework.stereotype.Service;

// Single responsibility: write the linked debit/credit pair for a
// completed transfer and mark the transaction COMPLETED. The caller owns
// the surrounding transaction and balance checks.
@Service
public class LedgerPostingService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionRepository transactionRepository;

    public LedgerPostingService(LedgerEntryRepository ledgerEntryRepository,
                                TransactionRepository transactionRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionRepository = transactionRepository;
    }

    public void postAndComplete(Transaction transaction, Wallet fromWallet, Wallet toWallet) {
        LedgerEntry debit = new LedgerEntry(
                transaction, fromWallet, transaction.getAmount().negate(), EntryType.DEBIT, fromWallet.getCurrency());
        LedgerEntry credit = new LedgerEntry(
                transaction, toWallet, transaction.getAmount(), EntryType.CREDIT, toWallet.getCurrency());
        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        transaction.markCompleted();
        transactionRepository.save(transaction);
    }
}
