# Database Architecture

The Digital Wallet Fraud Detection system uses a relational database model (MySQL) to maintain ACID compliance for financial transfers while supporting complex fraud detection queries.

## Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS {
        UUID id PK
        VARCHAR email
        VARCHAR password_hash
        VARCHAR phone_number
        VARCHAR kyc_status "UNVERIFIED, PENDING, APPROVED"
        DATETIME created_at
    }

    WALLETS {
        UUID id PK
        UUID user_id FK
        VARCHAR type "PERSONAL, BUSINESS"
        VARCHAR currency
        DATETIME created_at
    }

    TRANSACTIONS {
        UUID id PK
        VARCHAR idempotency_key
        UUID from_wallet_id FK
        UUID to_wallet_id FK
        DECIMAL amount
        VARCHAR currency
        VARCHAR status "PENDING, COMPLETED, FAILED, FLAGGED"
        DECIMAL latitude
        DECIMAL longitude
        DATETIME created_at
    }

    LEDGERS {
        UUID id PK
        UUID transaction_id FK
        UUID wallet_id FK
        VARCHAR type "CREDIT, DEBIT"
        DECIMAL amount
        DATETIME created_at
    }

    FRAUD_CONFIG {
        INT id PK
        DECIMAL cold_start_threshold
        INT min_history_for_baseline
        DECIMAL average_multiplier
        DECIMAL max_geo_distance_km
        INT velocity_window_minutes
        INT velocity_lookback_windows
        INT velocity_cold_start_max
        DECIMAL velocity_multiplier
    }

    FRAUD_FLAGS {
        UUID id PK
        UUID transaction_id FK
        VARCHAR rule_triggered
        INT risk_score
        BOOLEAN reviewed
        VARCHAR review_decision "APPROVED, REJECTED"
        UUID reviewed_by FK
        DATETIME created_at
        DATETIME reviewed_at
    }

    USERS ||--o{ WALLETS : owns
    WALLETS ||--o{ TRANSACTIONS : sends
    WALLETS ||--o{ TRANSACTIONS : receives
    TRANSACTIONS ||--|{ LEDGERS : creates
    TRANSACTIONS ||--o| FRAUD_FLAGS : triggers
    USERS ||--o{ FRAUD_FLAGS : reviews
```

## Core Tables Breakdown

### `wallets` & `ledgers`
To prevent race conditions, the system uses pessimistic locking on the `wallets` rows when a transfer occurs. Rather than storing a mutable "balance" column, the true balance is calculated dynamically by summing the `ledgers` table, ensuring strict double-entry bookkeeping.

### `transactions`
Acts as the central intent of a transfer. It is created first with a `PENDING` status. Depending on the fraud engine, it either moves to `COMPLETED` (and ledgers are written), `FAILED`, or `FLAGGED`.

### `fraud_config`
A singleton table (always `id = 1`) that acts as the real-time configuration store for the fraud engine. The Angular Admin Dashboard updates this row to tweak thresholds without requiring a backend redeployment.

### `fraud_flags`
If a transaction triggers a `MAJOR` fraud rule, a flag is written here. The transaction remains frozen until an admin reviews it and updates the `review_decision`.
