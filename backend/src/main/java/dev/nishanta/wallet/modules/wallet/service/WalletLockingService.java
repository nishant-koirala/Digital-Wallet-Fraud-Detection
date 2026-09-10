package dev.nishanta.wallet.modules.wallet.service;

import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

// Single responsibility: acquire pessimistic row locks on both wallets
// of a transfer in a deterministic ID order, so concurrent transfers
// between the same two wallets can never deadlock.
@Service
public class WalletLockingService {

    private final WalletRepository walletRepository;

    public WalletLockingService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletPair lockForTransfer(UUID fromWalletId, UUID toWalletId) {
        UUID firstLockId = fromWalletId.compareTo(toWalletId) < 0 ? fromWalletId : toWalletId;
        UUID secondLockId = fromWalletId.compareTo(toWalletId) < 0 ? toWalletId : fromWalletId;

        Wallet firstLocked = walletRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + firstLockId));
        Wallet secondLocked = walletRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + secondLockId));

        Wallet fromWallet = firstLockId.equals(fromWalletId) ? firstLocked : secondLocked;
        Wallet toWallet = firstLockId.equals(fromWalletId) ? secondLocked : firstLocked;

        return new WalletPair(fromWallet, toWallet);
    }

    public record WalletPair(Wallet fromWallet, Wallet toWallet) {
    }
}
