package dev.nishanta.wallet.modules.user.repository;

import dev.nishanta.wallet.modules.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    
    org.springframework.data.domain.Page<User> findByRole(dev.nishanta.wallet.modules.user.domain.Role role, org.springframework.data.domain.Pageable pageable);
    java.util.List<User> findByRole(dev.nishanta.wallet.modules.user.domain.Role role);
}
