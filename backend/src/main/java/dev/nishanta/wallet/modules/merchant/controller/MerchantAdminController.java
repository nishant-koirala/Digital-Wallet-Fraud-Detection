package dev.nishanta.wallet.modules.merchant.controller;

import dev.nishanta.wallet.common.constant.ApiRoutes;
import dev.nishanta.wallet.modules.merchant.dto.MerchantResponse;
import dev.nishanta.wallet.modules.merchant.service.MerchantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiRoutes.ADMIN_MERCHANTS_BASE)
@PreAuthorize("hasRole('ADMIN')")
public class MerchantAdminController {

    private final MerchantService merchantService;

    public MerchantAdminController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping(ApiRoutes.ADMIN_MERCHANTS_PENDING)
    public List<MerchantResponse> getPendingMerchants() {
        return merchantService.getPendingMerchants();
    }

    @PostMapping(ApiRoutes.ADMIN_MERCHANTS_APPROVE)
    public void approveMerchant(@PathVariable UUID merchantId, Authentication authentication) {
        merchantService.approveMerchant(merchantId, authentication.getName());
    }

    @PostMapping(ApiRoutes.ADMIN_MERCHANTS_REJECT)
    public void rejectMerchant(@PathVariable UUID merchantId, Authentication authentication) {
        merchantService.rejectMerchant(merchantId, authentication.getName());
    }
}
