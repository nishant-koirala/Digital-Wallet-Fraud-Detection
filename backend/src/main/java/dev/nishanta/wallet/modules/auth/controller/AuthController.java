package dev.nishanta.wallet.modules.auth.controller;

import dev.nishanta.wallet.modules.auth.dto.AuthRequest;
import dev.nishanta.wallet.modules.auth.dto.AuthResponse;
import dev.nishanta.wallet.modules.auth.service.AuthService;
import dev.nishanta.wallet.modules.auth.dto.JwtAuthResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.Optional;

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
                                 @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
                                 HttpServletResponse response) {
        JwtAuthResult result = authService.register(request, deviceId, ipAddress);
        setCookies(response, result.accessToken(), result.refreshToken());
        return new AuthResponse(null, result.walletId(), result.role(), result.name());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request,
                              @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                              @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
                              HttpServletResponse response) {
        JwtAuthResult result = authService.login(request, deviceId, ipAddress);
        setCookies(response, result.accessToken(), result.refreshToken());
        return new AuthResponse(null, result.walletId(), result.role(), result.name());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            Optional<Cookie> refreshCookie = Arrays.stream(request.getCookies())
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .findFirst();
            if (refreshCookie.isPresent()) {
                refreshToken = refreshCookie.get().getValue();
            }
        }
        
        JwtAuthResult result = authService.refresh(refreshToken);
        setCookies(response, result.accessToken(), result.refreshToken());
        return new AuthResponse(null, result.walletId(), result.role(), result.name());
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    private void setCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true); // Should be true in prod for HTTPS
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60); // 15 mins
        
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }
}
