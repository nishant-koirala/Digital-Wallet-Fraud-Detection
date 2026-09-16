# Core API Endpoints

This document outlines the core REST API endpoints exposed by the Digital Wallet Fraud Detection backend.

## Rate Limiting Note
All API endpoints are protected by `Bucket4j`. If limits are exceeded, the server responds with:
- **Status Code:** `429 Too Many Requests`

## 1. Authentication
### POST `/api/v1/auth/login`
Authenticates a user and returns a JWT token.
- **Rate Limit:** 5 requests per minute per IP.
- **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "securePassword123"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "123e4567-e89b-12d3-a456-426614174000"
  }
  ```

## 2. Transactions
### POST `/api/v1/transactions/transfer`
Initiates a fund transfer from the authenticated user's wallet to a target wallet.
- **Rate Limit:** 10 requests per minute per IP.
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "fromWalletId": "123e4567-e89b-12d3-a456-426614174000",
    "toWalletId": "987e6543-e21b-34c3-b789-426614174111",
    "amount": 500.00,
    "latitude": 27.7172,
    "longitude": 85.3240,
    "otp": "123456" // Optional. Required only if step-up auth is triggered.
  }
  ```
- **Responses:**
  - `200 OK`: Transfer successful.
  - `200 OK (Status: FLAGGED)`: Transfer frozen for manual review (Major Fraud).
  - `400 Bad Request (OtpRequiredException)`: Transfer blocked. An OTP has been sent. Resubmit this request with the `"otp"` field populated (Minor Fraud).
  - `400 Bad Request (DailyLimitExceededException)`: KYC limit exceeded.

## 3. Fraud Administration
### GET `/api/v1/admin/fraud/config`
Retrieves the current real-time fraud rule thresholds.
- **Headers:** `Authorization: Bearer <admin_token>`
- **Response (200 OK):**
  ```json
  {
    "coldStartThreshold": 50000.0,
    "minHistoryForBaseline": 5,
    "averageMultiplier": 3.0,
    "maxGeoDistanceKm": 500.0,
    "velocityWindowMinutes": 10,
    "velocityLookbackWindows": 6,
    "velocityColdStartMax": 5,
    "velocityMultiplier": 3.0
  }
  ```

### PUT `/api/v1/admin/fraud/config`
Updates the fraud rule thresholds instantly without server restart.
- **Headers:** `Authorization: Bearer <admin_token>`
- **Request Body:** (Same JSON structure as the GET response).
- **Response (200 OK):** Confirmation of updated config.
