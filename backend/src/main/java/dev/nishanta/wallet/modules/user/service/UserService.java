package dev.nishanta.wallet.modules.user.service;

import dev.nishanta.wallet.common.exception.BusinessRuleException;
import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.audit.service.AuditService;
import dev.nishanta.wallet.modules.user.domain.Role;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.dto.PasswordUpdateRequest;
import dev.nishanta.wallet.modules.user.dto.ProfileUpdateRequest;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public User getUserProfile(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public void updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = getUserProfile(userId);
        String oldPhone = user.getPhoneNumber();
        
        user.setPhoneNumber(request.phone());
        userRepository.save(user);

        auditService.logAction("USER", userId, "UPDATE_PROFILE", user.getEmail(), oldPhone, request.phone());
    }

    @Transactional
    public void changePassword(UUID userId, PasswordUpdateRequest request) {
        User user = getUserProfile(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        auditService.logAction("USER", userId, "CHANGE_PASSWORD", user.getEmail(), null, null);
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = getUserProfile(userId);
        
        // Soft delete: randomize PII to maintain constraints while anonymizing
        String oldEmail = user.getEmail();
        user.setEmail("deleted_" + UUID.randomUUID() + "@example.com");
        user.setName("Deleted User");
        user.setPhoneNumber("0000000000");
        user.setPasswordHash(UUID.randomUUID().toString()); // Unusable password
        user.setRole(Role.USER); // Reset role if admin

        userRepository.save(user);

        auditService.logAction("USER", userId, "DELETE_ACCOUNT", oldEmail, oldEmail, user.getEmail());
    }
}
