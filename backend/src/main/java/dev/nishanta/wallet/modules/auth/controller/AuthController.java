package dev.nishanta.wallet.modules.auth.controller;

import dev.nishanta.wallet.modules.auth.dto.AuthRequest;
import dev.nishanta.wallet.modules.auth.dto.AuthResponse;
import dev.nishanta.wallet.modules.auth.service.AuthService;
import dev.nishanta.wallet.modules.auth.dto.JwtAuthResult;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
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
                                 @CookieValue(value = "deviceId", required = false) String deviceId,
                                 HttpServletRequest httpRequest,
                                 HttpServletResponse response) {
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = java.util.UUID.randomUUID().toString();
        }
        String ipAddress = httpRequest.getRemoteAddr();
        JwtAuthResult result = authService.register(request, deviceId, ipAddress);
        setCookies(response, result.accessToken(), result.refreshToken(), deviceId);
        return new AuthResponse(null, result.walletId(), result.role(), result.name());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request,
                              @CookieValue(value = "deviceId", required = false) String deviceId,
                              HttpServletRequest httpRequest,
                              HttpServletResponse response) {
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = java.util.UUID.randomUUID().toString();
        }
        String ipAddress = httpRequest.getRemoteAddr();
        JwtAuthResult result = authService.login(request, deviceId, ipAddress);
        setCookies(response, result.accessToken(), result.refreshToken(), deviceId);
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
        String deviceId = null;
        if (request.getCookies() != null) {
            Optional<Cookie> devCookie = Arrays.stream(request.getCookies())
                    .filter(c -> "deviceId".equals(c.getName()))
                    .findFirst();
            if (devCookie.isPresent()) {
                deviceId = devCookie.get().getValue();
            }
        }
        if (deviceId == null) {
            deviceId = java.util.UUID.randomUUID().toString();
        }
        setCookies(response, result.accessToken(), result.refreshToken(), deviceId);
        return new AuthResponse(null, result.walletId(), result.role(), result.name());
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void setCookies(HttpServletResponse response, String accessToken, String refreshToken, String deviceId) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(15 * 60)
                .sameSite("Strict")
                .build();
        
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        
        ResponseCookie devCookie = ResponseCookie.from("deviceId", deviceId)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(365 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, devCookie.toString());
    }
}
