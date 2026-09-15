package dev.nishanta.wallet.modules.kyc.service;

import dev.nishanta.wallet.common.exception.BusinessRuleException;
import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.audit.service.AuditService;
import dev.nishanta.wallet.modules.kyc.domain.KycDocument;
import dev.nishanta.wallet.modules.kyc.domain.KycStatus;
import dev.nishanta.wallet.modules.kyc.dto.KycResponse;
import dev.nishanta.wallet.modules.kyc.dto.KycSubmitRequest;
import dev.nishanta.wallet.modules.kyc.repository.KycRepository;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.domain.WalletType;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KycService {

    private final KycRepository kycRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;

    public KycService(KycRepository kycRepository, UserRepository userRepository, WalletRepository walletRepository, AuditService auditService, SimpMessagingTemplate messagingTemplate) {
        this.kycRepository = kycRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.auditService = auditService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public KycResponse submitKyc(UUID userId, KycSubmitRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (kycRepository.findByUserId(userId).isPresent()) {
            throw new BusinessRuleException("User already has a KYC submission.");
        }

        KycDocument document = new KycDocument(user, request.documentType(), request.documentNumber(), request.frontImageUrl(), request.backImageUrl());
        kycRepository.save(document);

        user.setKycStatus(KycStatus.PENDING);
        userRepository.save(user);

        auditService.logAction("USER_KYC", document.getId(), "SUBMIT", user.getEmail(), "NOT_SUBMITTED", "PENDING");

        return mapToResponse(document);
    }

    public KycResponse getKycStatus(UUID userId) {
        KycDocument document = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("KYC not submitted yet."));
        return mapToResponse(document);
    }

    public List<KycResponse> getPendingKyc() {
        return kycRepository.findAllByStatus(KycStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveKyc(UUID documentId, String adminEmail) {
        KycDocument document = kycRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("KYC Document not found."));

        document.approve();
        kycRepository.save(document);

        User user = document.getUser();
        user.setKycStatus(KycStatus.APPROVED);
        userRepository.save(user);

        auditService.logAction("USER_KYC", document.getId(), "APPROVE", adminEmail, "PENDING", "APPROVED");

        walletRepository.findByUserIdAndType(user.getId(), WalletType.PERSONAL).ifPresent(wallet -> {
            messagingTemplate.convertAndSend("/topic/notifications/" + wallet.getId(),
                    "Your KYC Application has been APPROVED. You can now make larger transfers.");
        });
    }

    @Transactional
    public void rejectKyc(UUID documentId, String adminEmail) {
        KycDocument document = kycRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("KYC Document not found."));

        document.reject();
        kycRepository.save(document);

        User user = document.getUser();
        user.setKycStatus(KycStatus.REJECTED);
        userRepository.save(user);

        auditService.logAction("USER_KYC", document.getId(), "REJECT", adminEmail, "PENDING", "REJECTED");

        walletRepository.findByUserIdAndType(user.getId(), WalletType.PERSONAL).ifPresent(wallet -> {
            messagingTemplate.convertAndSend("/topic/notifications/" + wallet.getId(),
                    "Your KYC Application was REJECTED. Please contact support.");
        });
    }

    private KycResponse mapToResponse(KycDocument doc) {
        return new KycResponse(
                doc.getId(),
                doc.getUser().getId(),
                doc.getDocumentType(),
                doc.getDocumentNumber(),
                doc.getFrontImageUrl(),
                doc.getBackImageUrl(),
                doc.getStatus().name(),
                doc.getCreatedAt()
        );
    }
}
