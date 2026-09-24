package dev.nishanta.wallet.modules.transaction.ledger;

import dev.nishanta.wallet.modules.transaction.domain.EntryType;
import dev.nishanta.wallet.modules.transaction.domain.LedgerEntry;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.LedgerEntryRepository;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LedgerPostingServiceTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private LedgerPostingService ledgerPostingService;

    @Test
    public void postAndComplete_updatesBalancesAndSavesLedger() {
        Wallet fromWallet = mock(Wallet.class);
        Wallet toWallet = mock(Wallet.class);
        Transaction transaction = mock(Transaction.class);

        when(fromWallet.getCurrency()).thenReturn("NPR");
        when(toWallet.getCurrency()).thenReturn("NPR");
        when(transaction.getCurrency()).thenReturn("NPR");
        
        when(transaction.getAmount()).thenReturn(new BigDecimal("500"));
        when(fromWallet.getBalance()).thenReturn(new BigDecimal("1000"));
        when(toWallet.getBalance()).thenReturn(new BigDecimal("200"));
        
        when(transaction.getId()).thenReturn(UUID.randomUUID());
        when(toWallet.getId()).thenReturn(UUID.randomUUID());

        ledgerPostingService.postAndComplete(transaction, fromWallet, toWallet);

        verify(fromWallet).setBalance(new BigDecimal("500"));
        verify(toWallet).setBalance(new BigDecimal("700"));
        
        verify(walletRepository).save(fromWallet);
        verify(walletRepository).save(toWallet);

        ArgumentCaptor<LedgerEntry> entryCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository, times(2)).save(entryCaptor.capture());

        LedgerEntry firstEntry = entryCaptor.getAllValues().get(0); // Debit
        LedgerEntry secondEntry = entryCaptor.getAllValues().get(1); // Credit

        assertEquals(new BigDecimal("-500"), firstEntry.getAmount());
        assertEquals(EntryType.DEBIT, firstEntry.getEntryType());
        
        assertEquals(new BigDecimal("500"), secondEntry.getAmount());
        assertEquals(EntryType.CREDIT, secondEntry.getEntryType());

        verify(transaction).markCompleted();
        verify(transactionRepository).save(transaction);
    }
}
