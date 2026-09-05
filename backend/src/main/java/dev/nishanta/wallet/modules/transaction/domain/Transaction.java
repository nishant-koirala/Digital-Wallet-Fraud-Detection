package dev.nishanta.wallet.modules.transaction.domain;

import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id", nullable = false)
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id", nullable = false)
    private Wallet toWallet;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private BigDecimal latitude;
    private BigDecimal longitude;


    protected Transaction() {
    }

    public Transaction(String idempotencyKey, Wallet fromWallet, Wallet toWallet,
                       BigDecimal amount, String currency, BigDecimal latitude, BigDecimal longitude) {
        if (fromWallet.getId().equals(toWallet.getId())) {
            throw new IllegalArgumentException("A transaction cannot transfer to the same wallet");
        }
        this.id = UUID.randomUUID();
        this.idempotencyKey = idempotencyKey;
        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
        this.amount = amount;
        this.currency = currency;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Wallet getFromWallet() { return fromWallet; }
    public Wallet getToWallet() { return toWallet; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransactionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }

    public void markCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = TransactionStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFlagged() {
        this.status = TransactionStatus.FLAGGED;
        this.updatedAt = LocalDateTime.now();
    }
}
