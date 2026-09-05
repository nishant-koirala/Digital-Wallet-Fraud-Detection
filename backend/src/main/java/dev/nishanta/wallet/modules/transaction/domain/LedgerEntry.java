package dev.nishanta.wallet.modules.transaction.domain;

import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    private String currency;

    private LocalDateTime createdAt;

    protected LedgerEntry() {
    }

    public LedgerEntry(Transaction transaction, Wallet wallet, BigDecimal amount,
                       EntryType entryType, String currency) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("A ledger entry amount cannot be zero");
        }
        this.id = UUID.randomUUID();
        this.transaction = transaction;
        this.wallet = wallet;
        this.amount = amount;
        this.entryType = entryType;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Transaction getTransaction() { return transaction; }
    public Wallet getWallet() { return wallet; }
    public BigDecimal getAmount() { return amount; }
    public EntryType getEntryType() { return entryType; }
    public String getCurrency() { return currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
