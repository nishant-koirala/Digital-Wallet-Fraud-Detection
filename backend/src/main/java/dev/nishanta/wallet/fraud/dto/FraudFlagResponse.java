package dev.nishanta.wallet.fraud.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FraudFlagResponse(
        UUID transactionId,
        String ruleTriggered,
        int riskScore,
        String transactionStatus,
        LocalDateTime flaggedAt
) {
}
