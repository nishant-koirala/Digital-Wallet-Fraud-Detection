package dev.nishanta.wallet.modules.audit.repository;

import dev.nishanta.wallet.modules.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);
    Page<AuditLog> findByActionContainingIgnoreCaseOrPerformedByContainingIgnoreCase(String action, String performedBy, Pageable pageable);
}
