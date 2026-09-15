package dev.nishanta.wallet.modules.kyc.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record KycResponse(
        UUID id,
        UUID userId,
        String documentType,
        String documentNumber,
        String frontImageUrl,
        String backImageUrl,
        String status,
        LocalDateTime createdAt
) {}
