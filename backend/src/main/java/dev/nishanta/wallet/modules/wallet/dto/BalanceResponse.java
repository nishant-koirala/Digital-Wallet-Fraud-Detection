package dev.nishanta.wallet.modules.wallet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(
        UUID walletId,
        BigDecimal balance,
        String currency
) {
}
