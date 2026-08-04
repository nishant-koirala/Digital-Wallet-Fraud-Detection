package dev.nishanta.wallet.transaction.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        String idempotencyKey,
        UUID fromWalletId,
        UUID toWalletId,
        BigDecimal amount,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
