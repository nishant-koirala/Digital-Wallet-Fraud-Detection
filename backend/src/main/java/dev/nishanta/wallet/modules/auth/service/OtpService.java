package dev.nishanta.wallet.modules.auth.service;

import dev.nishanta.wallet.common.exception.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final EmailService emailService;
    
    // In-memory cache for demo purposes. Real apps use Redis with TTL.
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    public OtpService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void generateAndSendOtp(String email) {
        // Generate a 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        otpStore.put(email, otp);
        
        emailService.sendOtpEmail(email, otp);
    }

    public void validateOtp(String email, String inputOtp) {
        String storedOtp = otpStore.get(email);
        
        if (storedOtp == null) {
            throw new BusinessRuleException("No active OTP session found or OTP expired");
        }
        
        if (!storedOtp.equals(inputOtp)) {
            throw new BusinessRuleException("Invalid OTP provided");
        }
        
        // OTP is valid, clear it
        otpStore.remove(email);
    }
}
