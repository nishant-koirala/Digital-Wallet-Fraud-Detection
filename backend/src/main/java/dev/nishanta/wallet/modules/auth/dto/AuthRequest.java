package dev.nishanta.wallet.modules.auth.dto;

public record AuthRequest(String email, String password, String name, String phone) {
}
