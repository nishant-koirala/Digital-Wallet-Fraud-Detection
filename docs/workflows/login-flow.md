# Login & Rate Limiting Flow

This sequence diagram illustrates the authentication flow, emphasizing how the `RateLimitInterceptor` acts as a shield against brute force attacks.

## Sequence Diagram

```mermaid
sequenceDiagram
    autonumber

    actor User
    participant API as RateLimitInterceptor
    participant Auth as AuthService
    participant OTP as OtpService
    participant DB as UserRepository

    User->>API: POST /api/v1/auth/login (email, password)
    
    API->>API: Check Bucket4j for IP
    
    alt Rate Limit Exceeded (5/min)
        API-->>User: 429 Too Many Requests
    else Rate Limit OK
        API->>Auth: login(email, password)
        Auth->>DB: findByEmail(email)
        
        alt User Not Found or Bad Password
            DB-->>Auth: null
            Auth-->>User: 401 Unauthorized
        else Valid Credentials
            DB-->>Auth: User Entity
            
            Auth->>OTP: generateAndSendOtp(email)
            Auth-->>User: 200 OK (Status: OTP_REQUIRED)
            
            User->>API: POST /api/v1/auth/verify-otp (email, otp)
            API->>Auth: verifyOtp(email, otp)
            Auth->>OTP: validateOtp(email, otp)
            
            alt OTP Invalid
                OTP-->>Auth: Exception
                Auth-->>User: 400 Bad Request
            else OTP Valid
                OTP-->>Auth: Valid
                Auth-->>User: 200 OK (JWT Token)
            end
        end
    end
```
