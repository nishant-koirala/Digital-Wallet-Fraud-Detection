package dev.nishanta.wallet.modules.auth.dto;

public record AuthResponse(String token, String walletId, String role, String name) {
}
