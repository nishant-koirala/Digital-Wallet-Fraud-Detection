package dev.nishanta.wallet.common.constant;

public final class ApiRoutes {
    private ApiRoutes() {} // Prevent instantiation

    // Base Routes
    public static final String BASE_API = "/api/v1";
    
    // Dev Routes
    public static final String DEV_BASE = BASE_API + "/dev";
    public static final String DEV_SEED = "/seed";

    // Wallet Routes
    public static final String WALLET_BASE = BASE_API + "/wallets";
    public static final String WALLET_BALANCE = "/{walletId}/balance";
    public static final String WALLET_DEPOSIT = "/{walletId}/deposit";
    public static final String WALLET_WITHDRAW = "/{walletId}/withdraw";

    // Transaction Routes
    public static final String TRANSACTION_BASE = BASE_API + "/transactions";
    public static final String TRANSACTION_TRANSFER = "/transfer";

    // Fraud Flag Routes
    public static final String FRAUD_FLAG_BASE = BASE_API + "/fraud-flags";
    public static final String FRAUD_FLAG_PENDING = "/pending";
    public static final String FRAUD_FLAG_APPROVE = "/{transactionId}/approve";
    public static final String FRAUD_FLAG_REJECT = "/{transactionId}/reject";

    // Merchant Routes
    public static final String MERCHANT_BASE = "/api/v1/merchants";
    public static final String MERCHANT_ONBOARD = "/onboard";
    public static final String MERCHANT_PROFILE = "/profile";

    public static final String ADMIN_MERCHANTS_BASE = "/api/v1/admin/merchants";
    public static final String ADMIN_MERCHANTS_PENDING = "/pending";
    public static final String ADMIN_MERCHANTS_APPROVE = "/{merchantId}/approve";
    public static final String ADMIN_MERCHANTS_REJECT = "/{merchantId}/reject";

    // Audit Routes
    public static final String AUDIT_BASE = BASE_API + "/audit-logs";
}
