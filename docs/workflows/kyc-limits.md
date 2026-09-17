# KYC & Daily Transaction Limits

The `TransactionLimitValidator` evaluates every outgoing transfer request to ensure a user does not exceed their daily volume limit, which is strictly dictated by their current KYC (Know Your Customer) tier.

## Flowchart

```mermaid
flowchart TD
    Start[Transfer Request Initiated] --> GetKYC[Fetch User KYC Status]
    
    GetKYC --> CheckTier{KYC Tier?}
    
    CheckTier -->|UNVERIFIED / PENDING| Tier1[Tier 1 Limit: Rs 500 / day]
    CheckTier -->|APPROVED| Tier2[Tier 2 Limit: Rs 5000 / day]
    
    Tier1 --> CalcVolume
    Tier2 --> CalcVolume
    
    CalcVolume[DB Query: Sum(Amount) for today] --> Validate{Is (Sum + Current Amount) > Limit?}
    
    Validate -->|Yes| Reject[Throw DailyLimitExceededException]
    Validate -->|No| Approve[Proceed to Fraud Detection]
    
    Reject --> API[Return 400 Bad Request]
```
