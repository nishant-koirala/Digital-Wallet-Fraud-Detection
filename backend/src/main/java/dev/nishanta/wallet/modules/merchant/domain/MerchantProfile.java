package dev.nishanta.wallet.modules.merchant.domain;

import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "merchant_profiles")
public class MerchantProfile {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, unique = true)
    private Wallet wallet;

    private String businessName;

    private String category;

    private String settlementAccount;

    @Enumerated(EnumType.STRING)
    private MerchantStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected MerchantProfile() {
    }

    public MerchantProfile(Wallet wallet, String businessName, String category, String settlementAccount) {
        this.id = UUID.randomUUID();
        this.wallet = wallet;
        this.businessName = businessName;
        this.category = category;
        this.settlementAccount = settlementAccount;
        this.status = MerchantStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Wallet getWallet() { return wallet; }
    public String getBusinessName() { return businessName; }
    public String getCategory() { return category; }
    public String getSettlementAccount() { return settlementAccount; }
    public MerchantStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void suspend() {
        this.status = MerchantStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.status = MerchantStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = MerchantStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = MerchantStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }
}
