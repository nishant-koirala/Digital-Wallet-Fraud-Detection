# System Overview

The **Digital Wallet Fraud Detection System** is a distributed platform designed to simulate a modern mobile wallet application while providing advanced, real-time transaction monitoring and fraud detection capabilities.

## Tech Stack
- **Backend:** Java 26, Spring Boot 4.1.0
- **Frontend:** Angular 18, TypeScript, TailwindCSS
- **Database:** MySQL 9.1 with Flyway for migrations
- **Rate Limiting:** Bucket4j
- **Build Tool:** Maven

## Core Modules

The backend architecture is highly modularized to separate core wallet domains from the security and auditing concerns.

### 1. User & Auth Module
Handles user registration, authentication, and session management. It interfaces with the `OtpService` to provide multi-factor step-up authentication.

### 2. KYC Module
Maintains the "Know Your Customer" tier of a user (e.g., `UNVERIFIED`, `PENDING`, `APPROVED`). The transaction engine utilizes this status to enforce dynamic daily transfer limits.

### 3. Wallet Module
Manages the digital wallets tied to a user. Each user can have a `PERSONAL` wallet and a `BUSINESS` wallet. This module uses pessimistic locking (`WalletLockingService`) to ensure safe concurrent transfers and prevent race conditions.

### 4. Transaction Module
The heart of the application. The `TransferService` is the orchestrator that guarantees ACID compliance when moving funds. It communicates with:
- `TransactionLimitValidator` to check daily limits.
- `LedgerPostingService` for double-entry bookkeeping.
- `FraudDetectionService` to analyze the transaction payload before committing the transfer.

### 5. Fraud Module
A dynamic, rule-based engine that evaluates `Transaction` payloads against historical baselines. It can emit severity markers (`MINOR`, `MAJOR`) which trigger different defensive actions like OTP step-up authentication or manual admin review. Admins can configure the rules dynamically via the `FraudConfig` database entity.

### 6. Audit Module
An asynchronous logging system that immutably records high-value actions (e.g., `TRANSFER`, `LOGIN`, `ADMIN_CONFIG_CHANGE`) for compliance reporting.
