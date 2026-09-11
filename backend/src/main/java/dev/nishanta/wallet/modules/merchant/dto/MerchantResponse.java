package dev.nishanta.wallet.modules.merchant.dto;

import dev.nishanta.wallet.modules.merchant.domain.MerchantStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String businessName,
        String category,
        String settlementAccount,
        MerchantStatus status,
        UUID walletId,
        LocalDateTime createdAt
) {
}
