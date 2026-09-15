package dev.nishanta.wallet.modules.fraud.repository;

import dev.nishanta.wallet.modules.fraud.domain.FraudConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudConfigRepository extends JpaRepository<FraudConfig, Integer> {
}
