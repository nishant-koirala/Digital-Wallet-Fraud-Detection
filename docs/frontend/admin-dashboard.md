# Frontend: Admin Dashboard

The Angular frontend features an Admin Dashboard that interfaces directly with the Fraud Module APIs to allow dynamic, real-time configuration of the system's defenses.

## Fraud Settings Page
The primary component is the `FraudSettingsComponent` located at `frontend/src/app/pages/fraud-settings/`.

### Form Structure
The Angular Reactive Form contains 8 key inputs, logically grouped into two sections:

1. **Amount & Geography Rules**
   - **Cold Start Threshold:** Maximum transaction amount allowed for unverified/new users.
   - **Minimum History:** Transactions required before a baseline average is established.
   - **Amount Multiplier:** How many times larger than average a transaction can be.
   - **Max Geographic Distance (km):** Maximum distance allowed between two consecutive transactions.

2. **Velocity (Speed) Rules**
   - **Velocity Window (minutes):** The rolling timeframe to measure current activity against (e.g. 10 mins).
   - **Velocity Lookback Windows:** How many historical windows to use to build a baseline average (e.g. last 6 windows = 1 hour).
   - **Cold Start Max Count:** Absolute maximum transactions per window allowed if no baseline exists.
   - **Velocity Multiplier:** How many times higher than average the transaction rate can be.

### Workflow
1. `ngOnInit()`: The component makes a GET request to `/api/v1/admin/fraud/config`.
2. The Reactive Form is populated with the returned values.
3. The Admin can freely tweak any parameter.
4. `saveConfig()`: Submits the updated form payload via PUT to `/api/v1/admin/fraud/config`.
5. A toast notification is displayed upon success. The backend fraud engine immediately begins using the new rules on the next incoming transaction.
