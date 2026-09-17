# Backend Component Architecture

This C4 Component diagram zooms into the Spring Boot API (`Wallet & Fraud API`) container, mapping the internal structure, dependency injection lines, and boundaries between domains.

## Component Diagram (Level 3)

```mermaid
C4Component
    title Component Diagram for Wallet & Fraud API

    Container_Boundary(api, "Wallet & Fraud API (Spring Boot)") {
        
        Component(transferController, "TransferController", "REST Controller", "Handles incoming HTTP transfer requests.")
        Component(adminController, "FraudConfigController", "REST Controller", "Handles Admin updates to live configurations.")
        
        Component(transferSvc, "TransferService", "Service", "Orchestrates locking, limits, fraud checks, and ledgers. (@Transactional)")
        Component(lockSvc, "WalletLockingService", "Service", "Pessimistically locks wallets for thread safety.")
        Component(fraudSvc, "FraudDetectionService", "Service", "Iterates rules to calculate severity.")
        Component(kycSvc, "TransactionLimitValidator", "Service", "Validates daily limits based on KYC tier.")
        Component(ledgerSvc, "LedgerPostingService", "Service", "Performs double-entry bookkeeping.")
        
        Component(rules, "FraudRules (List)", "Interfaces", "AmountThresholdRule, VelocityRule, LocationAnomalyRule, GeoMismatchRule.")
        
        Component(txRepo, "TransactionRepository", "JPA Repository", "Database access for Transactions.")
        Component(fraudRepo, "FraudConfigRepository", "JPA Repository", "Database access for live configurations.")
        Component(walletRepo, "WalletRepository", "JPA Repository", "Database access for Wallets (with @Lock).")
        
        Rel(transferController, transferSvc, "Calls transfer()")
        Rel(adminController, fraudRepo, "Reads/Updates config")
        
        Rel(transferSvc, lockSvc, "Acquires locks via")
        Rel(transferSvc, kycSvc, "Validates limits via")
        Rel(transferSvc, fraudSvc, "Evaluates fraud via")
        Rel(transferSvc, ledgerSvc, "Posts ledgers via")
        
        Rel(lockSvc, walletRepo, "Queries with FOR UPDATE")
        Rel(fraudSvc, rules, "Evaluates")
        
        Rel(rules, txRepo, "Queries historical baselines")
        Rel(rules, fraudRepo, "Fetches dynamic thresholds")
        
    }
    
    ContainerDb(mysqlDb, "Relational Database", "MySQL 9.1", "Stores data.")
    System_Ext(otpExt, "OtpService", "External or internal service sending 2FA.")

    Rel(transferSvc, txRepo, "Saves PENDING tx")
    Rel(ledgerSvc, mysqlDb, "Inserts ledgers")
    Rel(walletRepo, mysqlDb, "JDBC")
    Rel(txRepo, mysqlDb, "JDBC")
    Rel(fraudRepo, mysqlDb, "JDBC")
    
    Rel(transferSvc, otpExt, "Triggers OTP on MINOR fraud")
```
