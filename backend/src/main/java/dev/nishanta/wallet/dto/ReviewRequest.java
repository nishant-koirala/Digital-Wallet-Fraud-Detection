package dev.nishanta.wallet.dto;

import java.util.UUID;

public record ReviewRequest(
        UUID adminUserId
) {
}