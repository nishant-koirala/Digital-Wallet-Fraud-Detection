package dev.nishanta.wallet.modules.wallet.domain;

import dev.nishanta.wallet.modules.user.domain.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private WalletType type;

    private String currency;

    private LocalDateTime createdAt;

    protected Wallet() {
    }

    public Wallet(User user, WalletType type, String currency) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.type = type;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public WalletType getType() { return type; }
    public String getCurrency() { return currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
