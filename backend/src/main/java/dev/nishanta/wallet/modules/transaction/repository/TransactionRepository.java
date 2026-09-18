package dev.nishanta.wallet.modules.transaction.repository;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.domain.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    org.springframework.data.domain.Page<Transaction> findByStatus(TransactionStatus status, org.springframework.data.domain.Pageable pageable);
    List<Transaction> findByStatus(TransactionStatus status);

    long countByFromWalletIdAndCreatedAtAfter(UUID fromWalletId, LocalDateTime after);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromWallet.id = :walletId " +
            "AND t.createdAt BETWEEN :start AND :end")
    long countByFromWalletIdAndCreatedAtBetween(
            @Param("walletId") UUID walletId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromWallet.id = :walletId " +
            "AND t.createdAt BETWEEN :start AND :end AND t.status = 'COMPLETED'")
    BigDecimal sumAmountByFromWalletIdAndCreatedAtBetween(
            @Param("walletId") UUID walletId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    long countByFromWalletIdAndStatus(UUID fromWalletId, TransactionStatus status);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.fromWallet.id = :walletId AND t.status = 'COMPLETED'")
    Optional<BigDecimal> findAverageAmountByFromWalletId(@Param("walletId") UUID walletId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findFirstByFromWalletIdAndIdNotAndLatitudeIsNotNullOrderByCreatedAtDesc(
            UUID fromWalletId, UUID excludeId);

    @Query("SELECT t FROM Transaction t WHERE t.fromWallet.id = :walletId OR t.toWallet.id = :walletId ORDER BY t.createdAt DESC")
    List<Transaction> findRecentByWalletId(@Param("walletId") UUID walletId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED'")
    Optional<BigDecimal> findTotalVolume();

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'COMPLETED'")
    long countSafeTransactions();

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status IN ('FLAGGED', 'REJECTED')")
    long countFlaggedTransactions();

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.createdAt >= :startDate AND t.createdAt < :endDate")
    Optional<BigDecimal> findVolumeBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
