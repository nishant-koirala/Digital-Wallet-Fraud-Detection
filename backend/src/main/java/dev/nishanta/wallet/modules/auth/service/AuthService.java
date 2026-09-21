package dev.nishanta.wallet.modules.auth.service;
import dev.nishanta.wallet.common.exception.BusinessRuleException;
import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.common.exception.OtpRequiredException;
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
import dev.nishanta.wallet.modules.fraud.domain.UserDevice;
import dev.nishanta.wallet.modules.fraud.repository.UserDeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AuditService auditService;
    private final OtpService otpService;
    private final UserDeviceRepository userDeviceRepository;

    public AuthService(UserRepository userRepository, WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager, EmailService emailService,
                       AuditService auditService, OtpService otpService,
                       UserDeviceRepository userDeviceRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.auditService = auditService;
        this.otpService = otpService;
        this.userDeviceRepository = userDeviceRepository;
    }

    @Transactional
    public AuthResponse register(AuthRequest request, String deviceId, String ipAddress) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessRuleException("Email already exists");
        }

        if (request.otp() == null || request.otp().isEmpty()) {
            otpService.generateAndSendOtp(request.email());
            throw new OtpRequiredException("OTP sent to " + request.email());
        } else {
            otpService.validateOtp(request.email(), request.otp());
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

        if (deviceId != null && ipAddress != null) {
            UserDevice device = new UserDevice(user.getId(), deviceId, ipAddress, true);
            userDeviceRepository.save(device);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), wallet.getId().toString());
        return new AuthResponse(token, wallet.getId().toString(), user.getRole().name(), user.getName());
    }

    public AuthResponse login(AuthRequest request, String deviceId, String ipAddress) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        if (request.otp() == null || request.otp().isEmpty()) {
            otpService.generateAndSendOtp(request.email());
            throw new OtpRequiredException("OTP sent to " + request.email());
        } else {
            otpService.validateOtp(request.email(), request.otp());
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserIdAndType(user.getId(), WalletType.PERSONAL)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        if (deviceId != null && ipAddress != null) {
            Optional<UserDevice> existingDevice = userDeviceRepository.findByUserIdAndDeviceId(user.getId(), deviceId);
            if (existingDevice.isPresent()) {
                UserDevice d = existingDevice.get();
                d.setIpAddress(ipAddress);
                d.setLastSeenAt(java.time.LocalDateTime.now());
                d.setTrusted(true);
                userDeviceRepository.save(d);
            } else {
                UserDevice newDevice = new UserDevice(user.getId(), deviceId, ipAddress, true);
                userDeviceRepository.save(newDevice);
            }
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), wallet.getId().toString());
        return new AuthResponse(token, wallet.getId().toString(), user.getRole().name(), user.getName());
    }
}
