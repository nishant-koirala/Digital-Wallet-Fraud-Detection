package dev.nishanta.wallet.repository;

import dev.nishanta.wallet.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    long countByFromWalletIdAndCreatedAtAfter(java.util.UUID fromWalletId, java.time.LocalDateTime after);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(t) FROM Transaction t WHERE t.fromWallet.id = :walletId " +
                    "AND t.createdAt BETWEEN :start AND :end")
    long countByFromWalletIdAndCreatedAtBetween(
            @org.springframework.data.repository.query.Param("walletId") java.util.UUID walletId,
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    long countByFromWalletIdAndStatus(UUID fromWalletId, dev.nishanta.wallet.domain.TransactionStatus status);

    @org.springframework.data.jpa.repository.Query(
            "SELECT AVG(t.amount) FROM Transaction t WHERE t.fromWallet.id = :walletId AND t.status = 'COMPLETED'")
    Optional<java.math.BigDecimal> findAverageAmountByFromWalletId(
            @org.springframework.data.repository.query.Param("walletId") UUID walletId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}