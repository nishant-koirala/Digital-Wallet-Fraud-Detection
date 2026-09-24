package dev.nishanta.wallet.modules.transaction.ledger;

import dev.nishanta.wallet.modules.transaction.domain.EntryType;
import dev.nishanta.wallet.modules.transaction.domain.LedgerEntry;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.LedgerEntryRepository;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;

// Single responsibility: write the linked debit/credit pair for a
// completed transfer and mark the transaction COMPLETED. The caller owns
// the surrounding transaction and balance checks.
@Service
public class LedgerPostingService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public LedgerPostingService(LedgerEntryRepository ledgerEntryRepository,
                                TransactionRepository transactionRepository,
                                WalletRepository walletRepository,
                                SimpMessagingTemplate messagingTemplate) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void postAndComplete(Transaction transaction, Wallet fromWallet, Wallet toWallet) {
        if (!fromWallet.getCurrency().equals(transaction.getCurrency()) || 
            !toWallet.getCurrency().equals(transaction.getCurrency())) {
            throw new dev.nishanta.wallet.common.exception.BusinessRuleException("Currency mismatch between wallets and transaction");
        }
        
        LedgerEntry debit = new LedgerEntry(
                transaction, fromWallet, transaction.getAmount().negate(), EntryType.DEBIT, fromWallet.getCurrency());
        LedgerEntry credit = new LedgerEntry(
                transaction, toWallet, transaction.getAmount(), EntryType.CREDIT, toWallet.getCurrency());
        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        fromWallet.setBalance(fromWallet.getBalance().subtract(transaction.getAmount()));
        toWallet.setBalance(toWallet.getBalance().add(transaction.getAmount()));
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        transaction.markCompleted();
        transactionRepository.save(transaction);

        // Send real-time notification to the receiver's wallet ID
        Map<String, String> notification = new HashMap<>();
        notification.put("message", "You received Rs. " + transaction.getAmount() + "!");
        notification.put("transactionId", transaction.getId().toString());
        notification.put("amount", transaction.getAmount().toString());
        
        messagingTemplate.convertAndSend("/topic/notifications/" + toWallet.getId(), notification);
    }
}
