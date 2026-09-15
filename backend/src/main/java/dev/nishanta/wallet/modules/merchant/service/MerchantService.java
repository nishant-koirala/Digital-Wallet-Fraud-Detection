package dev.nishanta.wallet.modules.merchant.service;

import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.merchant.domain.MerchantProfile;
import dev.nishanta.wallet.modules.merchant.dto.MerchantCreateRequest;
import dev.nishanta.wallet.modules.merchant.dto.MerchantResponse;
import dev.nishanta.wallet.modules.merchant.repository.MerchantRepository;
import dev.nishanta.wallet.modules.merchant.domain.MerchantStatus;
import dev.nishanta.wallet.modules.user.domain.Role;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.domain.WalletType;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import dev.nishanta.wallet.modules.audit.service.AuditService;
import dev.nishanta.wallet.common.exception.BusinessRuleException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import dev.nishanta.wallet.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;

    public MerchantService(MerchantRepository merchantRepository, UserRepository userRepository, WalletRepository walletRepository, AuditService auditService, SimpMessagingTemplate messagingTemplate) {
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.auditService = auditService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public MerchantResponse onboardMerchant(UUID userId, MerchantCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (merchantRepository.findByWallet_User_Id(userId).isPresent()) {
            throw new BusinessRuleException("User already has a merchant profile");
        }

        // Provision a second wallet for merchant activities
        Wallet merchantWallet = new Wallet(user, WalletType.MERCHANT, "NPR");
        walletRepository.save(merchantWallet);

        // Upgrade user role if they are just a USER
        if (user.getRole() == Role.USER) {
            user.setRole(Role.MERCHANT);
            userRepository.save(user);
        }

        // Create Merchant Profile
        MerchantProfile profile = new MerchantProfile(
                merchantWallet,
                request.businessName(),
                request.category(),
                request.settlementAccount()
        );
        merchantRepository.save(profile);

        auditService.logAction("MERCHANT_PROFILE", profile.getId(), "ONBOARD", user.getEmail(), null, request);

        return mapToResponse(profile);
    }

    public MerchantResponse getMerchantProfile(UUID userId) {
        MerchantProfile profile = merchantRepository.findByWallet_User_Id(userId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found for user"));
        return mapToResponse(profile);
    }

    public java.util.List<MerchantResponse> getPendingMerchants() {
        return merchantRepository.findAllByStatus(MerchantStatus.PENDING).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void approveMerchant(UUID merchantId, String adminEmail) {
        MerchantProfile profile = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant not found"));
        profile.approve();
        merchantRepository.save(profile);
        auditService.logAction("MERCHANT_PROFILE", profile.getId(), "APPROVE", adminEmail, null, null);

        messagingTemplate.convertAndSend("/topic/notifications/" + profile.getWallet().getId(), 
                "Your merchant application for '" + profile.getBusinessName() + "' was APPROVED.");
    }

    @Transactional
    public void rejectMerchant(UUID merchantId, String adminEmail) {
        MerchantProfile profile = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant not found"));
        profile.reject();
        merchantRepository.save(profile);
        auditService.logAction("MERCHANT_PROFILE", profile.getId(), "REJECT", adminEmail, null, null);

        messagingTemplate.convertAndSend("/topic/notifications/" + profile.getWallet().getId(), 
                "Your merchant application for '" + profile.getBusinessName() + "' was REJECTED.");
    }

    private MerchantResponse mapToResponse(MerchantProfile profile) {
        return new MerchantResponse(
                profile.getId(),
                profile.getBusinessName(),
                profile.getCategory(),
                profile.getSettlementAccount(),
                profile.getStatus(),
                profile.getWallet().getId(),
                profile.getCreatedAt()
        );
    }
}
