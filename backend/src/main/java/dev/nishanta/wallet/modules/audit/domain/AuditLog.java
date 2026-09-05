package dev.nishanta.wallet.modules.audit.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    private String entityType;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID entityId;

    private String action;

    private String performedBy;

    @Column(columnDefinition = "json")
    private String oldValue;

    @Column(columnDefinition = "json")
    private String newValue;

    private LocalDateTime createdAt;

    protected AuditLog() {
    }

    public AuditLog(String entityType, UUID entityId, String action, String performedBy,
                    String oldValue, String newValue) {
        this.id = UUID.randomUUID();
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.performedBy = performedBy;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public String getAction() { return action; }
    public String getPerformedBy() { return performedBy; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
