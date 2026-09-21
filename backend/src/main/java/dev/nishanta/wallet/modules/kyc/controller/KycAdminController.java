package dev.nishanta.wallet.modules.kyc.controller;

import dev.nishanta.wallet.common.constant.ApiRoutes;
import dev.nishanta.wallet.modules.kyc.dto.KycResponse;
import dev.nishanta.wallet.modules.kyc.service.KycService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/kyc")
@PreAuthorize("hasRole('ADMIN')")
public class KycAdminController {

    private final KycService kycService;

    public KycAdminController(KycService kycService) {
        this.kycService = kycService;
    }

    @GetMapping("/pending")
    public List<KycResponse> getPendingKyc() {
        return kycService.getPendingKyc();
    }

    @PostMapping("/{documentId}/approve")
    public void approveKyc(@PathVariable UUID documentId, Authentication authentication) {
        kycService.approveKyc(documentId, authentication.getName());
    }

    @PostMapping("/{documentId}/reject")
    public void rejectKyc(@PathVariable UUID documentId, Authentication authentication) {
        kycService.rejectKyc(documentId, authentication.getName());
    }
}
