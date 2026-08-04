package dev.nishanta.wallet.fraud.repository;

import dev.nishanta.wallet.fraud.domain.FraudFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudFlagRepository extends JpaRepository<FraudFlag, UUID> {

    Optional<FraudFlag> findByTransactionId(UUID transactionId);

    List<FraudFlag> findByReviewed(boolean reviewed);
}
