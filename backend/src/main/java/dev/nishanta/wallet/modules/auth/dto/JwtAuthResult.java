package dev.nishanta.wallet.modules.auth.dto;

public record JwtAuthResult(String accessToken, String refreshToken, String walletId, String role, String name) {
}
