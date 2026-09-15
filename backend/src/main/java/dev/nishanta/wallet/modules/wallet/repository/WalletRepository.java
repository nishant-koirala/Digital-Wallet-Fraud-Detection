package dev.nishanta.wallet.modules.wallet.repository;

import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import dev.nishanta.wallet.modules.wallet.domain.WalletType;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUser_PhoneNumberAndType(String phoneNumber, WalletType type);
    
    Optional<Wallet> findFirstByType(WalletType type);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") UUID id);
}
