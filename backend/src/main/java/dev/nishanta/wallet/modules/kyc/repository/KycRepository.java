package dev.nishanta.wallet.modules.kyc.repository;

import dev.nishanta.wallet.modules.kyc.domain.KycDocument;
import dev.nishanta.wallet.modules.kyc.domain.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KycRepository extends JpaRepository<KycDocument, UUID> {
    Optional<KycDocument> findByUserId(UUID userId);
    List<KycDocument> findAllByStatus(KycStatus status);
}
