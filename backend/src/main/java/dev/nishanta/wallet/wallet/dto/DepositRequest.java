package dev.nishanta.wallet.wallet.dto;

import java.math.BigDecimal;

public record DepositRequest(
        String idempotencyKey,
        BigDecimal amount
) {
}
