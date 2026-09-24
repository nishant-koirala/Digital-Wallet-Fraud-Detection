package dev.nishanta.wallet.modules.wallet.service;

import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletLockingServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletLockingService walletLockingService;

    @Test
    public void lockForTransfer_alwaysLocksInIdOrder() {
        UUID id1 = new UUID(0, 1);
        UUID id2 = new UUID(0, 2); // id1 < id2

        Wallet wallet1 = mock(Wallet.class);
        Wallet wallet2 = mock(Wallet.class);

        when(walletRepository.findByIdForUpdate(id1)).thenReturn(Optional.of(wallet1));
        when(walletRepository.findByIdForUpdate(id2)).thenReturn(Optional.of(wallet2));

        // Test with id1 -> id2
        WalletLockingService.WalletPair pair1 = walletLockingService.lockForTransfer(id1, id2);
        assertEquals(wallet1, pair1.fromWallet());
        assertEquals(wallet2, pair1.toWallet());

        InOrder inOrder1 = inOrder(walletRepository);
        inOrder1.verify(walletRepository).findByIdForUpdate(id1);
        inOrder1.verify(walletRepository).findByIdForUpdate(id2);

        // Test with id2 -> id1
        WalletLockingService.WalletPair pair2 = walletLockingService.lockForTransfer(id2, id1);
        assertEquals(wallet2, pair2.fromWallet());
        assertEquals(wallet1, pair2.toWallet());

        InOrder inOrder2 = inOrder(walletRepository);
        // It should still lock id1 first, then id2
        inOrder2.verify(walletRepository).findByIdForUpdate(id1);
        inOrder2.verify(walletRepository).findByIdForUpdate(id2);
    }
}
