package dev.nishanta.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Deliberately flat — only what's safe/useful to hand back over the API.
// No nested Wallet or User objects, so nothing like a password hash
// can ever leak through, no matter how deep those relationships go.
public record TransferResponse(
        UUID transactionId,
        String status,
        UUID fromWalletId,
        UUID toWalletId,
        BigDecimal amount,
        String currency,
        LocalDateTime createdAt
) {
}