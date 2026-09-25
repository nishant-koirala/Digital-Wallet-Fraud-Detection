package dev.nishanta.wallet.modules.fraud.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FraudFlagResponse(
        UUID transactionId,
        String ruleTriggered,
        int riskScore,
        String transactionStatus,
        java.math.BigDecimal amount,
        LocalDateTime flaggedAt
) {
}
