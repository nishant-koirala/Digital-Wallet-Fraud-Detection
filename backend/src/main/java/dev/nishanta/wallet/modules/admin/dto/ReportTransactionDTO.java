package dev.nishanta.wallet.modules.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReportTransactionDTO {
    private String id;
    private String fromWalletId;
    private String toWalletId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private boolean isFraudulent;
    private LocalDateTime createdAt;

    public ReportTransactionDTO(String id, String fromWalletId, String toWalletId, BigDecimal amount, String currency, String status, boolean isFraudulent, LocalDateTime createdAt) {
        this.id = id;
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.isFraudulent = isFraudulent;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getFromWalletId() {
        return fromWalletId;
    }

    public String getToWalletId() {
        return toWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public boolean isFraudulent() {
        return isFraudulent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
