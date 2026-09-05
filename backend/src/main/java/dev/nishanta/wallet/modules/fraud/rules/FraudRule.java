package dev.nishanta.wallet.modules.fraud.rules;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;

// The Open/Closed contract: TransferService will hold a List<FraudRule>
// and run every one of them, without knowing or caring how many exist
// or what each one actually checks. Adding a new rule later means
// adding a new class that implements this interface — never editing
// TransferService itself.
public interface FraudRule {

    boolean isSuspicious(Transaction transaction);

    // Matches the values your schema comment listed for rule_triggered:
    // VELOCITY, AMOUNT_THRESHOLD, GEO_MISMATCH
    String ruleName();
}
