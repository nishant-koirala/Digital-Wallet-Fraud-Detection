package dev.nishanta.wallet.dto;

import java.math.BigDecimal;

public record DepositRequest(
        String idempotencyKey,
        BigDecimal amount
) {
}