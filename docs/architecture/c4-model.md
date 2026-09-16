# C4 Model: Architecture Diagrams

This document visualizes the architecture of the Digital Wallet Fraud Detection System using the C4 model and Mermaid diagrams.

## Context Diagram (Level 1)
Shows how the system interacts with its users and external systems.

```mermaid
C4Context
    title System Context for Digital Wallet System

    Person(customer, "Wallet Customer", "A user with a digital wallet who wants to send/receive money.")
    Person(admin, "Fraud Administrator", "Monitors transactions and configures fraud detection rules.")

    System(walletSystem, "Digital Wallet & Fraud Detection System", "Allows customers to transfer funds while continuously monitoring for fraudulent activity.")
    
    System_Ext(notificationSystem, "Email/SMS Notification System", "Sends OTPs and alerts to customers.")

    Rel(customer, walletSystem, "Sends money, views balance, inputs OTPs")
    Rel(admin, walletSystem, "Configures rules, reviews flagged transactions")
    Rel(walletSystem, notificationSystem, "Triggers OTPs and alerts")
    Rel(notificationSystem, customer, "Delivers OTPs and alerts")
```

## Container Diagram (Level 2)
Zooming into the `Digital Wallet & Fraud Detection System` to see the high-level technical containers.

```mermaid
C4Container
    title Container Diagram for Digital Wallet System

    Person(customer, "Wallet Customer", "A user with a digital wallet.")
    Person(admin, "Fraud Administrator", "Configures fraud rules.")

    Container(frontendApp, "Customer Web App", "Angular, TypeScript", "Provides the UI for customers to transfer funds.")
    Container(adminApp, "Admin Dashboard", "Angular, TypeScript", "Provides the UI for configuring fraud rules (e.g. VelocityRule thresholds).")
    
    Container(apiGateway, "API Gateway / Rate Limiter", "Spring Web, Bucket4j", "Handles rate limiting (e.g. 5 req/min for auth, 10 req/min for transfers) and routes requests.")
    
    Container(walletApi, "Wallet & Fraud API", "Spring Boot, Java", "Core backend services orchestrating transactions, double-entry ledgers, and running the fraud engine.")
    
    ContainerDb(mysqlDb, "Relational Database", "MySQL 9.1", "Stores users, wallets, ledgers, transactions, and fraud configurations.")
    
    System_Ext(notificationSystem, "Email/SMS Notification System", "Sends OTPs.")

    Rel(customer, frontendApp, "Uses", "HTTPS")
    Rel(admin, adminApp, "Uses", "HTTPS")
    
    Rel(frontendApp, apiGateway, "Makes API calls to", "JSON/HTTPS")
    Rel(adminApp, apiGateway, "Makes API calls to", "JSON/HTTPS")
    
    Rel(apiGateway, walletApi, "Routes authenticated requests to", "Internal")
    
    Rel(walletApi, mysqlDb, "Reads from and writes to", "JDBC")
    Rel(walletApi, notificationSystem, "Sends OTP requests to", "REST")
```
