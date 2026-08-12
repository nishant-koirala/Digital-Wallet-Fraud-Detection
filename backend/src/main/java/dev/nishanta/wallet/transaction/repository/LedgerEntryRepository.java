package dev.nishanta.wallet.transaction.repository;

import dev.nishanta.wallet.transaction.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    // Balance is derived by the DB, not loaded row-by-row into the JVM.
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntry l WHERE l.wallet.id = :walletId")
    BigDecimal sumAmountByWalletId(@Param("walletId") UUID walletId);
}
