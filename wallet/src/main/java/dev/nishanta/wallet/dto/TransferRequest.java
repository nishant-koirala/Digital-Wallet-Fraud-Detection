package dev.nishanta.wallet.dto;

import java.math.BigDecimal;
import java.util.UUID;

// A record auto-generates the constructor, getters, equals/hashCode,
// and toString for you — perfect for a DTO that's just carrying data
// in from a JSON request body, with no behavior of its own.
public record TransferRequest(
        String idempotencyKey,
        UUID fromWalletId,
        UUID toWalletId,
        BigDecimal amount
) {
}