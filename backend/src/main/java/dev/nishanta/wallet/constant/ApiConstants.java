package dev.nishanta.wallet.constant;

// Single source of truth for every URL path the API exposes. Controllers
// reference these constants instead of hardcoding strings, so renaming a
// route or changing the API version prefix is a one-line change here.
public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException("ApiConstants is a utility class and cannot be instantiated");
    }

    public static final String API_BASE = "/api/v1";

    // Wallet endpoints: /api/v1/wallets
    public static final class Wallet {
        private Wallet() {
        }

        public static final String BASE = API_BASE + "/wallets";
    }

    // Transfer endpoints: /api/v1/transactions
    public static final class Transaction {
        private Transaction() {
        }

        public static final String BASE = API_BASE + "/transactions";
        public static final String TRANSFER = "/transfer";
    }

    // Admin fraud-review endpoints: /api/v1/fraud-flags
    public static final class FraudFlag {
        private FraudFlag() {
        }

        public static final String BASE = API_BASE + "/fraud-flags";
        public static final String PENDING = "/pending";
        public static final String APPROVE = "/approve";
        public static final String REJECT = "/reject";
    }

    // Dev-only endpoints: /api/v1/dev
    public static final class Dev {
        private Dev() {
        }

        public static final String BASE = API_BASE + "/dev";
        public static final String SEED = "/seed";
    }
}
