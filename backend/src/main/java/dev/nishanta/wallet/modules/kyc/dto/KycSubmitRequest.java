package dev.nishanta.wallet.modules.kyc.dto;

import jakarta.validation.constraints.NotBlank;

public record KycSubmitRequest(
        @NotBlank String documentType,
        @NotBlank String documentNumber,
        @NotBlank String frontImageUrl,
        @NotBlank String backImageUrl
) {}
