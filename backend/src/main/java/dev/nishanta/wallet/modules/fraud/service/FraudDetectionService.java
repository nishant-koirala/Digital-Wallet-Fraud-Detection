package dev.nishanta.wallet.modules.fraud.service;

import dev.nishanta.wallet.modules.fraud.domain.FraudFlag;
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

    public boolean flagIfSuspicious(Transaction transaction) {
        List<FraudRule> firedRules = fraudRules.stream()
                .filter(rule -> rule.isSuspicious(transaction))
                .collect(Collectors.toList());

        if (firedRules.isEmpty()) {
            return false;
        }

        String combinedRuleNames = firedRules.stream()
                .map(FraudRule::ruleName)
                .collect(Collectors.joining(","));

        fraudFlagRepository.save(new FraudFlag(transaction, combinedRuleNames, firedRules.size() * 10));

        transaction.markFlagged();
        transactionRepository.save(transaction);
        return true;
    }
}
