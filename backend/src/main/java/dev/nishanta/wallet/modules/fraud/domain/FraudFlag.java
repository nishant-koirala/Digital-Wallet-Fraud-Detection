package dev.nishanta.wallet.modules.fraud.domain;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.user.domain.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_flags")
public class FraudFlag {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    private String ruleTriggered;

    private int riskScore;

    private boolean reviewed;

    @Enumerated(EnumType.STRING)
    private ReviewDecision reviewDecision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    protected FraudFlag() {
    }

    public FraudFlag(Transaction transaction, String ruleTriggered, int riskScore) {
        this.id = UUID.randomUUID();
        this.transaction = transaction;
        this.ruleTriggered = ruleTriggered;
        this.riskScore = riskScore;
        this.reviewed = false;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Transaction getTransaction() { return transaction; }
    public String getRuleTriggered() { return ruleTriggered; }
    public int getRiskScore() { return riskScore; }
    public boolean isReviewed() { return reviewed; }
    public ReviewDecision getReviewDecision() { return reviewDecision; }
    public User getReviewedBy() { return reviewedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }

    public void review(User admin, ReviewDecision decision) {
        this.reviewedBy = admin;
        this.reviewDecision = decision;
        this.reviewed = true;
        this.reviewedAt = LocalDateTime.now();
    }
}
