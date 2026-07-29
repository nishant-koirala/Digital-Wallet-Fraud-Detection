package dev.nishanta.wallet.repository;

import dev.nishanta.wallet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}