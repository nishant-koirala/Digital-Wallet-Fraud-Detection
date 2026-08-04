package dev.nishanta.wallet.fraud.dto;

import java.util.UUID;

public record ReviewRequest(
        UUID adminUserId
) {
}
