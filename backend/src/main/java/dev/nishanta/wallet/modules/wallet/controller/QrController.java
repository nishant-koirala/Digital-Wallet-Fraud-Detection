package dev.nishanta.wallet.modules.wallet.controller;

import dev.nishanta.wallet.common.constant.ApiRoutes;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import dev.nishanta.wallet.security.JwtUtil;
import dev.nishanta.wallet.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping(ApiRoutes.BASE_API + "/qr")
@PreAuthorize("isAuthenticated()")
public class QrController {

    private final JwtUtil jwtUtil;
    private final SecurityUtils securityUtils;
    private final WalletRepository walletRepository;

    public QrController(JwtUtil jwtUtil, SecurityUtils securityUtils, WalletRepository walletRepository) {
        this.jwtUtil = jwtUtil;
        this.securityUtils = securityUtils;
        this.walletRepository = walletRepository;
    }

    @GetMapping("/generate")
    public java.util.Map<String, String> generateQr(@RequestParam String walletId, @RequestParam(required = false) BigDecimal amount) {
        Wallet wallet = walletRepository.findById(java.util.UUID.fromString(walletId))
                .orElseThrow(() -> new dev.nishanta.wallet.common.exception.BusinessRuleException("Wallet not found"));
        securityUtils.verifyWalletOwnership(wallet);
        
        // Return as a standard JSON object mapping
        return java.util.Map.of("qrPayload", jwtUtil.generateQrToken(walletId, amount));
    }
}
