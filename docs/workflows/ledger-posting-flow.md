# Ledger Posting Flow

This sequence diagram explains the double-entry bookkeeping system handled by the `LedgerPostingService`. This strictly ensures that funds are never "lost" in transit, and that the total system value remains perfectly balanced.

## Sequence Diagram

```mermaid
sequenceDiagram
    autonumber

    participant TS as TransferService
    participant Ledger as LedgerPostingService
    participant DB as LedgerRepository
    participant TxDB as TransactionRepository

    TS->>Ledger: postAndComplete(transaction)
    
    note over Ledger: Starts @Transactional boundary
    
    Ledger->>DB: save(new LedgerEntry(fromWallet, DEBIT, amount))
    DB-->>Ledger: Saved Debit
    
    Ledger->>DB: save(new LedgerEntry(toWallet, CREDIT, amount))
    DB-->>Ledger: Saved Credit
    
    Ledger->>TxDB: save(transaction.setStatus(COMPLETED))
    TxDB-->>Ledger: Saved Transaction
    
    note over Ledger: Commits @Transactional boundary
    
    Ledger-->>TS: void
```
