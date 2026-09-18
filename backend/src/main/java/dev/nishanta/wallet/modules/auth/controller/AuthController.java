package dev.nishanta.wallet.modules.auth.controller;

import dev.nishanta.wallet.modules.auth.dto.AuthRequest;
import dev.nishanta.wallet.modules.auth.dto.AuthResponse;
import dev.nishanta.wallet.modules.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request,
                                 @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                                 @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        return authService.register(request, deviceId, ipAddress);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request,
                              @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                              @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        return authService.login(request, deviceId, ipAddress);
    }
}
