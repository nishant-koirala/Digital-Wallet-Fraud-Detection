package dev.nishanta.wallet.modules.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        String idempotencyKey,
        UUID fromWalletId,
        UUID toWalletId,
        String toPhoneNumber,
        String qrPayload,
        @NotNull
        @Positive
        BigDecimal amount,
        String currency,
        BigDecimal latitude,
        BigDecimal longitude,
        String otp,
        String pin
) {}
