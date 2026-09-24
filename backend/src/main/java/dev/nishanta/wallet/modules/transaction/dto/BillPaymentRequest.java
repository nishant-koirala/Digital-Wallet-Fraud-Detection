package dev.nishanta.wallet.modules.transaction.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BillPaymentRequest(
        String idempotencyKey,
        UUID fromWalletId,
        String billerId,
        String customerId,
        BigDecimal amount,
        String pin
) {
}
