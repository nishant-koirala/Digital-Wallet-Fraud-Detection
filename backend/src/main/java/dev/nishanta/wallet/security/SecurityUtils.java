package dev.nishanta.wallet.security;

import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public void verifyWalletOwnership(Wallet wallet) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !wallet.getUser().getEmail().equals(auth.getName())) {
            throw new AccessDeniedException("You do not have permission to access this wallet");
        }
    }
}
