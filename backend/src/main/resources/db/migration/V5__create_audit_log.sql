DROP TABLE IF EXISTS audit_log;

CREATE TABLE audit_log (
    id CHAR(36) PRIMARY KEY,
    entity_type VARCHAR(255) NOT NULL,
    entity_id CHAR(36) NOT NULL,
    action VARCHAR(255) NOT NULL,
    performed_by VARCHAR(255) NOT NULL,
    old_value JSON,
    new_value JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
