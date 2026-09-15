package dev.nishanta.wallet.modules.kyc.controller;

import dev.nishanta.wallet.modules.kyc.dto.KycResponse;
import dev.nishanta.wallet.modules.kyc.dto.KycSubmitRequest;
import dev.nishanta.wallet.modules.kyc.service.KycService;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import dev.nishanta.wallet.common.exception.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kyc")
public class KycController {

    private final KycService kycService;
    private final UserRepository userRepository;

    public KycController(KycService kycService, UserRepository userRepository) {
        this.kycService = kycService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public KycResponse submitKyc(@Valid @RequestBody KycSubmitRequest request, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return kycService.submitKyc(user.getId(), request);
    }

    @GetMapping
    public KycResponse getMyKycStatus(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return kycService.getKycStatus(user.getId());
    }
}
