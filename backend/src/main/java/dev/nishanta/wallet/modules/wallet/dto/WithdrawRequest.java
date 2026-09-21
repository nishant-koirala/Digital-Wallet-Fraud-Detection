package dev.nishanta.wallet.modules.wallet.dto;

import java.math.BigDecimal;

public record WithdrawRequest(
        String idempotencyKey,
        BigDecimal amount,
        String otp
) {
}
