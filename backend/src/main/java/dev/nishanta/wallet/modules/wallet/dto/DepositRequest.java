package dev.nishanta.wallet.modules.wallet.dto;

import java.math.BigDecimal;

public record DepositRequest(
        String idempotencyKey,
        BigDecimal amount
) {
}
