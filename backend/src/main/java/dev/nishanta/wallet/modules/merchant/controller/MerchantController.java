package dev.nishanta.wallet.modules.merchant.controller;

import dev.nishanta.wallet.common.constant.ApiRoutes;
import dev.nishanta.wallet.modules.merchant.dto.MerchantCreateRequest;
import dev.nishanta.wallet.modules.merchant.dto.MerchantResponse;
import dev.nishanta.wallet.modules.merchant.service.MerchantService;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiRoutes.MERCHANT_BASE)
public class MerchantController {

    private final MerchantService merchantService;
    private final UserRepository userRepository;

    public MerchantController(MerchantService merchantService, UserRepository userRepository) {
        this.merchantService = merchantService;
        this.userRepository = userRepository;
    }

    @PostMapping(ApiRoutes.MERCHANT_ONBOARD)
    public MerchantResponse onboardMerchant(@RequestBody @Valid MerchantCreateRequest request, Authentication authentication) {
        User user = getUser(authentication);
        return merchantService.onboardMerchant(user.getId(), request);
    }

    @GetMapping(ApiRoutes.MERCHANT_PROFILE)
    public MerchantResponse getMerchantProfile(Authentication authentication) {
        User user = getUser(authentication);
        return merchantService.getMerchantProfile(user.getId());
    }

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
