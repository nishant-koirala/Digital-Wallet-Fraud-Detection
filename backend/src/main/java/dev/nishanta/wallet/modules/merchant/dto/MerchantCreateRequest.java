package dev.nishanta.wallet.modules.merchant.dto;

import jakarta.validation.constraints.NotBlank;

public record MerchantCreateRequest(
        @NotBlank(message = "Business name is required")
        String businessName,
        
        @NotBlank(message = "Category is required")
        String category,
        
        @NotBlank(message = "Settlement account is required")
        String settlementAccount
) {
}
