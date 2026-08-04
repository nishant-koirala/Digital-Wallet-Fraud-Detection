package dev.nishanta.wallet.fraud.dto;

import java.util.UUID;

public record ReviewResponse(
        UUID transactionId,
        String decision,
        String transactionStatus
) {
}
