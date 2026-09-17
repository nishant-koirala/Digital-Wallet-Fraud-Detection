# Concurrency & Locking Workflow

This sequence diagram demonstrates how the `WalletLockingService` handles concurrent transfer requests using pessimistic database locking (`SELECT ... FOR UPDATE`). This prevents a race condition where a user with a $100 balance rapidly submits two $100 transfer requests, potentially double-spending.

## Sequence Diagram

```mermaid
sequenceDiagram
    autonumber

    actor User1 as Request A
    actor User2 as Request B (Concurrent)
    participant TS as TransferService
    participant Lock as WalletLockingService
    participant DB as MySQL Database

    User1->>TS: transfer(amount: 100)
    User2->>TS: transfer(amount: 100)
    
    note over User1, User2: Both requests arrive at the exact same millisecond
    
    TS->>Lock: lockForTransfer(fromWallet) [Thread A]
    TS->>Lock: lockForTransfer(fromWallet) [Thread B]
    
    Lock->>DB: SELECT * FROM wallets WHERE id=? FOR UPDATE [Thread A]
    DB-->>Lock: Wallet Entity (Locked to Thread A)
    Lock-->>TS: Locked Wallet [Thread A proceeds]
    
    note right of DB: Thread B's query blocks at the DB level<br/>until Thread A's transaction commits or rolls back
    
    Lock->>DB: SELECT * FROM wallets WHERE id=? FOR UPDATE [Thread B - BLOCKED]
    
    note over TS: Thread A executes transfer logic:<br/>balance check, fraud detection, ledger posting
    
    TS->>DB: Commit Transaction [Thread A]
    DB-->>Lock: Wallet Entity (Lock released, now Acquired by Thread B)
    Lock-->>TS: Locked Wallet [Thread B proceeds]
    
    note over TS: Thread B now executes transfer logic,<br/>but the balance was already reduced by Thread A!
    
    TS->>DB: calculateBalance(fromWallet) [Thread B]
    DB-->>TS: Insufficient Balance
    
    TS-->>User2: 400 InsufficientBalanceException
```
