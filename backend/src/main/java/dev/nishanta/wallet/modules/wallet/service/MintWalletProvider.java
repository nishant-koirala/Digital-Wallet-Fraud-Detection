package dev.nishanta.wallet.modules.wallet.service;

import dev.nishanta.wallet.modules.user.domain.Role;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.domain.WalletType;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import org.springframework.stereotype.Component;

@Component
public class MintWalletProvider {

    private static final String MINT_EMAIL = "mint@internal";

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public MintWalletProvider(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public Wallet findOrCreateMintWallet() {
        var existingUser = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(MINT_EMAIL))
                .findFirst();

        User mintUser = existingUser.orElseGet(() -> {
            User newUser = new User("System Mint", MINT_EMAIL, "n/a", Role.ADMIN, "0000000000");
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
