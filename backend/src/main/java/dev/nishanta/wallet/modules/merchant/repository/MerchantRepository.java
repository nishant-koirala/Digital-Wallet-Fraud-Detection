package dev.nishanta.wallet.modules.merchant.repository;

import dev.nishanta.wallet.modules.merchant.domain.MerchantProfile;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantProfile, UUID> {
    Optional<MerchantProfile> findByWallet(Wallet wallet);
    Optional<MerchantProfile> findByWallet_User_Id(UUID userId);
}
