# Transaction & Step-Up Auth Workflow

This document details the lifecycle of a single `TransferRequest` as it moves through the `TransferService`, highlighting how locking, balance validation, fraud detection, and OTP Step-Up Authentication are sequenced.

## Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    
    actor User
    participant API as RateLimitInterceptor
    participant TS as TransferService
    participant Lock as WalletLockingService
    participant Bal as BalanceCalculator
    participant KYC as TransactionLimitValidator
    participant OTP as OtpService
    participant DB as TransactionRepository
    participant Fraud as FraudDetectionService
    participant Ledger as LedgerPostingService
    
    User->>API: POST /api/v1/transactions/transfer
    
    alt Rate Limit Exceeded (10/min)
        API-->>User: 429 Too Many Requests
    else Rate Limit OK
        API->>TS: transfer(request)
        
        TS->>Lock: lockForTransfer(fromWallet, toWallet)
        Lock-->>TS: WalletPair (locked)
        
        TS->>Bal: calculateBalance(fromWallet)
        Bal-->>TS: currentBalance
        
        alt currentBalance < amount
            TS-->>User: 400 InsufficientBalanceException
        end
        
        TS->>KYC: validateDailyLimit(fromWallet, amount)
        alt limit exceeded
            KYC-->>User: 400 DailyLimitExceededException
        end
        
        TS->>DB: save(new Transaction(PENDING))
        DB-->>TS: Saved Transaction
        
        TS->>Fraud: evaluateFraud(Transaction)
        Fraud-->>TS: FraudDetectionResult (CLEAN, MINOR, MAJOR)
        
        alt FraudDetectionResult == MAJOR (FLAGGED)
            TS-->>User: 200 OK (Status: FLAGGED)
        else FraudDetectionResult == MINOR
            alt OTP not provided in request
                TS->>OTP: generateAndSendOtp(userEmail)
                TS-->>User: 400 OtpRequiredException (Rolls back PENDING tx)
            else OTP provided in request
                TS->>OTP: validateOtp(userEmail, request.otp)
                OTP-->>TS: Valid
            end
        end
        
        TS->>Ledger: postAndComplete(transaction)
        Ledger-->>TS: void (Ledgers saved, Tx = COMPLETED)
        
        TS-->>User: 200 OK (Status: COMPLETED)
    end
```
