package dev.nishanta.wallet.modules.user.repository;

import dev.nishanta.wallet.modules.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
