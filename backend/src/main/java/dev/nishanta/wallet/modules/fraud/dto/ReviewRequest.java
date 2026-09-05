package dev.nishanta.wallet.modules.fraud.dto;

import java.util.UUID;

public record ReviewRequest(
        UUID adminUserId
) {
}
