package dev.nishanta.wallet.modules.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nishanta.wallet.modules.audit.domain.AuditLog;
import dev.nishanta.wallet.modules.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
    }

    public void logAction(String entityType, UUID entityId, String action, String performedBy, Object oldValue, Object newValue) {
        String oldJson = null;
        String newJson = null;
        try {
            if (oldValue != null) {
                oldJson = objectMapper.writeValueAsString(oldValue);
            }
            if (newValue != null) {
                newJson = objectMapper.writeValueAsString(newValue);
            }
        } catch (JsonProcessingException e) {
            // In a real application, you might want to log this using a standard logger.
            System.err.println("Failed to serialize audit log values: " + e.getMessage());
        }

        AuditLog log = new AuditLog(entityType, entityId, action, performedBy, oldJson, newJson);
        auditLogRepository.save(log);
    }
}
