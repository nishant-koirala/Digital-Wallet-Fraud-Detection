# Fraud Engine Workflow

The Fraud Engine evaluates a given transaction payload against multiple rules (`FraudRule`). Each rule can return a severity (`NONE`, `MINOR`, `MAJOR`). The engine calculates the maximum severity and acts accordingly.

## Flowchart

```mermaid
flowchart TD
    Start[Transaction Payload Received] --> Loop[Iterate Registered FraudRules]
    
    Loop --> RuleA[AmountThresholdRule]
    Loop --> RuleB[VelocityRule]
    Loop --> RuleC[LocationAnomalyRule]
    Loop --> RuleD[GeoMismatchRule]
    
    RuleA --> |Evaluate| Eval[Collect Severities]
    RuleB --> |Evaluate| Eval
    RuleC --> |Evaluate| Eval
    RuleD --> |Evaluate| Eval
    
    Eval --> CheckMajor{Any rule returned MAJOR?}
    
    CheckMajor -- Yes --> Flag[Create FraudFlag]
    Flag --> MarkTx[transaction.markFlagged()]
    MarkTx --> ReturnMajor[Return FraudDetectionResult.FLAGGED]
    
    CheckMajor -- No --> CheckMinor{Any rule returned MINOR?}
    
    CheckMinor -- Yes --> ReturnMinor[Return FraudDetectionResult.MINOR_FRAUD]
    
    CheckMinor -- No --> ReturnClean[Return FraudDetectionResult.CLEAN]
```

## Rule Severities

- **NONE**: The transaction behavior fits completely within the wallet's historical baseline.
- **MINOR**: The transaction exceeds the baseline by the initial multiplier config (e.g., spending 5x more than usual, or doing 3x more transactions in 10 minutes than usual). This triggers **Step-Up Authentication**.
- **MAJOR**: The transaction vastly exceeds the baseline (e.g., multiplier + 2), or violates hard physical limits (e.g. traveling > 900 km/h). This triggers an immediate freeze and **Admin Review**.
