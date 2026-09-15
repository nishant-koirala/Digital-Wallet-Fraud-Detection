package dev.nishanta.wallet.modules.kyc.domain;

import dev.nishanta.wallet.modules.user.domain.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kyc_documents")
public class KycDocument {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String documentType;
    private String documentNumber;
    private String frontImageUrl;
    private String backImageUrl;

    @Enumerated(EnumType.STRING)
    private KycStatus status = KycStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    protected KycDocument() {}

    public KycDocument(User user, String documentType, String documentNumber, String frontImageUrl, String backImageUrl) {
        this.user = user;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.frontImageUrl = frontImageUrl;
        this.backImageUrl = backImageUrl;
        this.status = KycStatus.PENDING;
    }

    public void approve() {
        this.status = KycStatus.APPROVED;
    }

    public void reject() {
        this.status = KycStatus.REJECTED;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getFrontImageUrl() {
        return frontImageUrl;
    }

    public String getBackImageUrl() {
        return backImageUrl;
    }

    public KycStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
