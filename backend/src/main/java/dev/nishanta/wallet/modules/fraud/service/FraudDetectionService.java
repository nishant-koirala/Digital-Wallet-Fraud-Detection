package dev.nishanta.wallet.modules.fraud.service;

import dev.nishanta.wallet.modules.fraud.domain.FraudConfig;
import dev.nishanta.wallet.modules.fraud.domain.FraudFlag;
import dev.nishanta.wallet.modules.fraud.domain.FraudSeverity;
import dev.nishanta.wallet.modules.fraud.domain.FraudDetectionResult;
import dev.nishanta.wallet.modules.fraud.repository.FraudConfigRepository;
import dev.nishanta.wallet.modules.fraud.repository.FraudFlagRepository;
import dev.nishanta.wallet.modules.fraud.rules.FraudRule;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Single responsibility: decide whether a transaction is suspicious and,
// if so, record the flag and hold the transaction for review. Money only
// moves after an admin approves.
@Service
public class FraudDetectionService {

    private final FraudConfigRepository fraudConfigRepository;
    private final FraudFlagRepository fraudFlagRepository;
    private final TransactionRepository transactionRepository;
    private final List<FraudRule> fraudRules;

    public FraudDetectionService(FraudConfigRepository fraudConfigRepository,
                                 FraudFlagRepository fraudFlagRepository,
                                 TransactionRepository transactionRepository,
                                 List<FraudRule> fraudRules) {
        this.fraudConfigRepository = fraudConfigRepository;
        this.fraudFlagRepository = fraudFlagRepository;
        this.transactionRepository = transactionRepository;
        this.fraudRules = fraudRules;
    }

    public FraudDetectionResult evaluateFraud(Transaction transaction) {
        FraudConfig config = fraudConfigRepository.findById(1).orElseGet(FraudConfig::createDefault);

        boolean isMajor = false;
        boolean isMinor = false;
        java.util.List<String> majorRules = new java.util.ArrayList<>();
        java.util.List<String> minorRules = new java.util.ArrayList<>();

        for (FraudRule rule : fraudRules) {
            FraudSeverity severity = rule.evaluate(transaction, config);
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
            User user = transaction.getFromWallet().getUser();
            user.setFraudRiskScore(user.getFraudRiskScore() + 10);
            
            if (user.getFraudRiskScore() > 50) {
                fraudFlagRepository.save(new FraudFlag(transaction, "MINOR_ESCALATION", user.getFraudRiskScore()));
                transaction.markFlagged();
                transactionRepository.save(transaction);
                
                // Reset score since the user is now held for major review
                user.setFraudRiskScore(0);
                
                return FraudDetectionResult.FLAGGED;
            }
            return FraudDetectionResult.MINOR_FRAUD;
        }
        
        return FraudDetectionResult.CLEAN;
    }
}
