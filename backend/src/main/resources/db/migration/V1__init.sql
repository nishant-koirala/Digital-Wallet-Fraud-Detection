-- =====================================================================
-- Digital Wallet System — Database Schema
-- Engine: MySQL 8.0+ (InnoDB, utf8mb4)
-- Designed in SQLyog — apply as-is, or via Flyway migration V1__init.sql
--
-- Design decisions (see project documentation for full rationale):
--   - Primary keys: UUIDv4, generated in the application layer (Java),
--     stored as CHAR(36). Never auto-increment (avoids exposing
--     sequential, guessable IDs on financial records).
--   - Money: DECIMAL(19,4) everywhere, mapped to BigDecimal in Java.
--     Never FLOAT/DOUBLE.
--   - Every money column carries its own `currency` (ISO 4217, e.g. NPR)
--     even though only one currency is used today.
--   - snake_case, plural table names.
--   - Timestamps stored in UTC. created_at on every table; updated_at
--     added only where a row can legitimately change after creation.
--   - Status/type fields are VARCHAR, validated by a Java enum at the
--     application layer — not native MySQL ENUM — so new values don't
--     require a schema migration.
--   - Transactions and ledger_entries are treated as immutable/append-
--     only: no application code should ever DELETE from these tables.
--     A correction is a new, reversing transaction — never an edit.
--   - Concurrency: wallet balance safety is enforced in the application
--     layer via pessimistic row locking (SELECT ... FOR UPDATE) inside
--     a single @Transactional method — not expressed in this DDL.
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS fraud_flags;
DROP TABLE IF EXISTS ledger_entries;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS merchant_profiles;
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id              CHAR(36)      NOT NULL,
    name            VARCHAR(150)  NOT NULL,
    email           VARCHAR(255)  NOT NULL,
    password_hash   VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL,          -- USER, MERCHANT, ADMIN
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- wallets
-- ---------------------------------------------------------------------
CREATE TABLE wallets (
    id              CHAR(36)      NOT NULL,
    user_id         CHAR(36)      NOT NULL,
    type            VARCHAR(20)   NOT NULL,          -- PERSONAL, MERCHANT
    currency        CHAR(3)       NOT NULL DEFAULT 'NPR',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_wallets_user_id (user_id),
    CONSTRAINT fk_wallets_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- merchant_profiles   (wallet 1 --- 0..1 merchant_profile)
-- ---------------------------------------------------------------------
CREATE TABLE merchant_profiles (
    id                  CHAR(36)      NOT NULL,
    wallet_id           CHAR(36)      NOT NULL,
    business_name       VARCHAR(200)  NOT NULL,
    category            VARCHAR(100)  NULL,
    settlement_account  VARCHAR(100)  NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, SUSPENDED
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_merchant_profiles_wallet_id (wallet_id),
    CONSTRAINT fk_merchant_profiles_wallet
        FOREIGN KEY (wallet_id) REFERENCES wallets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- transactions
-- ---------------------------------------------------------------------
CREATE TABLE transactions (
    id                CHAR(36)        NOT NULL,
    idempotency_key   VARCHAR(100)    NOT NULL,
    from_wallet_id    CHAR(36)        NOT NULL,
    to_wallet_id      CHAR(36)        NOT NULL,
    amount            DECIMAL(19,4)   NOT NULL,
    currency          CHAR(3)         NOT NULL DEFAULT 'NPR',
    status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED, FLAGGED
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_transactions_idempotency_key (idempotency_key),
    KEY idx_transactions_from_wallet (from_wallet_id),
    KEY idx_transactions_to_wallet (to_wallet_id),
    KEY idx_transactions_status (status),
    CONSTRAINT fk_transactions_from_wallet
        FOREIGN KEY (from_wallet_id) REFERENCES wallets (id),
    CONSTRAINT fk_transactions_to_wallet
        FOREIGN KEY (to_wallet_id) REFERENCES wallets (id),
    CONSTRAINT chk_transactions_amount_positive
        CHECK (amount > 0),
    CONSTRAINT chk_transactions_distinct_wallets
        CHECK (from_wallet_id <> to_wallet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- ledger_entries   (append-only; exactly 2 rows per transaction)
-- ---------------------------------------------------------------------
CREATE TABLE ledger_entries (
    id                CHAR(36)        NOT NULL,
    transaction_id    CHAR(36)        NOT NULL,
    wallet_id         CHAR(36)        NOT NULL,
    amount            DECIMAL(19,4)   NOT NULL,        -- signed: negative = debit, positive = credit
    entry_type        VARCHAR(10)     NOT NULL,        -- DEBIT, CREDIT
    currency          CHAR(3)         NOT NULL DEFAULT 'NPR',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ledger_wallet_created (wallet_id, created_at),  -- primary access pattern: wallet history/balance
    KEY idx_ledger_transaction_id (transaction_id),
    CONSTRAINT fk_ledger_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id),
    CONSTRAINT fk_ledger_wallet
        FOREIGN KEY (wallet_id) REFERENCES wallets (id),
    CONSTRAINT chk_ledger_amount_nonzero
        CHECK (amount <> 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- fraud_flags   (transaction 1 --- 0..1 fraud_flag)
-- ---------------------------------------------------------------------
CREATE TABLE fraud_flags (
    id                CHAR(36)        NOT NULL,
    transaction_id    CHAR(36)        NOT NULL,
    rule_triggered    VARCHAR(50)     NOT NULL,        -- VELOCITY, AMOUNT_THRESHOLD, GEO_MISMATCH
    risk_score        INT             NOT NULL,
    reviewed          TINYINT(1)      NOT NULL DEFAULT 0,
    review_decision   VARCHAR(20)     NULL,            -- APPROVED, REJECTED
    reviewed_by       CHAR(36)        NULL,             -- references users.id (an admin); NULL until reviewed
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at       DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_fraud_flags_transaction_id (transaction_id),
    KEY idx_fraud_flags_reviewed (reviewed),
    CONSTRAINT fk_fraud_flags_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id),
    CONSTRAINT fk_fraud_flags_reviewer
        FOREIGN KEY (reviewed_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- audit_log   (append-only, polymorphic — no strict FK by design)
-- ---------------------------------------------------------------------
CREATE TABLE audit_log (
    id              CHAR(36)      NOT NULL,
    entity_type     VARCHAR(50)   NOT NULL,   -- e.g. 'Transaction', 'Wallet', 'FraudFlag'
    entity_id       CHAR(36)      NOT NULL,
    action          VARCHAR(50)   NOT NULL,   -- CREATED, STATUS_CHANGED, FLAGGED, REVIEWED
    performed_by    VARCHAR(40)   NOT NULL,   -- a users.id UUID, or the literal string 'SYSTEM'
    old_value       JSON          NULL,
    new_value       JSON          NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_audit_log_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
