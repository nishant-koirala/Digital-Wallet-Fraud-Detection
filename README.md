## Backend — Tech Stack

- Java 26, Spring Boot 4.1.0, Maven
- MySQL (local instance, no Docker per project constraints)
- Flyway for schema migrations
- Spring Data JPA / Hibernate
- Spring Security (JWT auth)

## Core Features

- **Double-entry ledger** — every transfer writes a linked debit/credit pair; balance is always derived from ledger entries, never stored directly
- **Idempotent transfers** — client-supplied idempotency keys prevent duplicate processing on retried requests
- **Deadlock-safe concurrency** — pessimistic row locking with a consistent lock ordering across wallets
- **Fraud detection** — rule-based checks (amount threshold relative to wallet history, transaction velocity), with flagged transactions held for admin review rather than auto-completed
- **Admin review flow** — approve/reject flagged transactions; money only moves on approval
- **Merchant profiles** — merchant onboarding and profile management
- **Audit logging** — audit logging for critical actions
- **QR code payments** — QR code payments integration

## Running locally

1. Create a local MySQL database and user matching `application.properties`
2. `./mvnw spring-boot:run` from `backend/` — Flyway applies migrations automatically
3. API available at `http://localhost:8080`

## Status

Actively in development. See commit history for progress.