package dev.nishanta.wallet.modules.auth.service;
import dev.nishanta.wallet.common.exception.BusinessRuleException;
import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.auth.dto.AuthRequest;
import dev.nishanta.wallet.modules.auth.dto.AuthResponse;
import dev.nishanta.wallet.modules.audit.service.AuditService;
import dev.nishanta.wallet.modules.user.domain.Role;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.domain.WalletType;
import dev.nishanta.wallet.modules.wallet.repository.WalletRepository;
import dev.nishanta.wallet.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager, EmailService emailService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        if (userRepository.findAll().stream().anyMatch(u -> u.getEmail().equals(request.email()))) {
            throw new BusinessRuleException("Email already exists");
        }

        User user = new User(request.name(), request.email(), passwordEncoder.encode(request.password()), Role.USER, request.phone());
        userRepository.save(user);

        Wallet wallet = new Wallet(user, WalletType.PERSONAL, "NPR");
        walletRepository.save(wallet);

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        auditService.logAction("USER", user.getId(), "REGISTER", user.getEmail(), null, request);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), wallet.getId().toString());
        return new AuthResponse(token, wallet.getId().toString(), user.getRole().name(), user.getName());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(request.email()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found"));

        Wallet wallet = walletRepository.findAll().stream()
                .filter(w -> w.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), wallet.getId().toString());
        return new AuthResponse(token, wallet.getId().toString(), user.getRole().name(), user.getName());
    }
}
