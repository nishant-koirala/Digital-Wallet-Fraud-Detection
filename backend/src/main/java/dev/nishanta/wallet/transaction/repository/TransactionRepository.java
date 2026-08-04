package dev.nishanta.wallet.transaction.repository;

import dev.nishanta.wallet.transaction.domain.Transaction;
import dev.nishanta.wallet.transaction.domain.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    long countByFromWalletIdAndCreatedAtAfter(UUID fromWalletId, LocalDateTime after);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromWallet.id = :walletId " +
            "AND t.createdAt BETWEEN :start AND :end")
    long countByFromWalletIdAndCreatedAtBetween(
            @Param("walletId") UUID walletId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    long countByFromWalletIdAndStatus(UUID fromWalletId, TransactionStatus status);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.fromWallet.id = :walletId AND t.status = 'COMPLETED'")
    Optional<BigDecimal> findAverageAmountByFromWalletId(@Param("walletId") UUID walletId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findFirstByFromWalletIdAndIdNotAndLatitudeIsNotNullOrderByCreatedAtDesc(
            UUID fromWalletId, UUID excludeId);
}
