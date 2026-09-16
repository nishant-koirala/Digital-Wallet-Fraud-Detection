package dev.nishanta.wallet.modules.fraud.service;

import dev.nishanta.wallet.modules.fraud.domain.FraudFlag;
import dev.nishanta.wallet.modules.fraud.domain.FraudSeverity;
import dev.nishanta.wallet.modules.fraud.domain.FraudDetectionResult;
import dev.nishanta.wallet.modules.fraud.repository.FraudFlagRepository;
import dev.nishanta.wallet.modules.fraud.rules.FraudRule;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Single responsibility: decide whether a transaction is suspicious and,
// if so, record the flag and hold the transaction for review. Money only
// moves after an admin approves.
@Service
public class FraudDetectionService {

    private final FraudFlagRepository fraudFlagRepository;
    private final TransactionRepository transactionRepository;
    private final List<FraudRule> fraudRules;

    public FraudDetectionService(FraudFlagRepository fraudFlagRepository,
                                 TransactionRepository transactionRepository,
                                 List<FraudRule> fraudRules) {
        this.fraudFlagRepository = fraudFlagRepository;
        this.transactionRepository = transactionRepository;
        this.fraudRules = fraudRules;
    }

    public FraudDetectionResult evaluateFraud(Transaction transaction) {
        boolean isMajor = false;
        boolean isMinor = false;
        java.util.List<String> majorRules = new java.util.ArrayList<>();
        java.util.List<String> minorRules = new java.util.ArrayList<>();

        for (FraudRule rule : fraudRules) {
            FraudSeverity severity = rule.evaluate(transaction);
            if (severity == FraudSeverity.MAJOR) {
                isMajor = true;
                majorRules.add(rule.ruleName());
            } else if (severity == FraudSeverity.MINOR) {
                isMinor = true;
                minorRules.add(rule.ruleName());
            }
        }

        if (isMajor) {
            fraudFlagRepository.save(new FraudFlag(transaction, String.join(",", majorRules), 100));
            transaction.markFlagged();
            transactionRepository.save(transaction);
            return FraudDetectionResult.FLAGGED;
        } else if (isMinor) {
            return FraudDetectionResult.MINOR_FRAUD;
        }
        
        return FraudDetectionResult.CLEAN;
    }
}
