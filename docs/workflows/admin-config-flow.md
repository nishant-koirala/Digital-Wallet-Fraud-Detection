# Admin Config Update Flow

This sequence diagram illustrates how an Administrator updates the system's live fraud thresholds via the Angular Frontend. Because the engine queries the `FraudConfig` at runtime, the changes take effect instantly for all new transactions without a server deployment.

## Sequence Diagram

```mermaid
sequenceDiagram
    autonumber

    actor Admin
    participant UI as Angular Dashboard
    participant Ctrl as FraudConfigController
    participant DB as FraudConfigRepository
    participant Engine as FraudDetectionService

    Admin->>UI: Navigates to /fraud-settings
    UI->>Ctrl: GET /api/v1/admin/fraud/config
    Ctrl->>DB: findById(1)
    DB-->>Ctrl: Current FraudConfig
    Ctrl-->>UI: 200 OK (Config JSON)
    
    UI-->>Admin: Displays Form (populated)
    
    Admin->>UI: Tweaks Velocity rules & Submits
    
    UI->>Ctrl: PUT /api/v1/admin/fraud/config
    Ctrl->>DB: save(Updated FraudConfig)
    DB-->>Ctrl: Saved Entity
    Ctrl-->>UI: 200 OK
    UI-->>Admin: "Settings Saved" Toast
    
    note over Engine: The next incoming transaction<br/>will immediately read the new config<br/>from the DB.
```
