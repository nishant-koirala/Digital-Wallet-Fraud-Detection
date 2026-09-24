package dev.nishanta.wallet.modules.auth.service;

import dev.nishanta.wallet.common.exception.BusinessRuleException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    
    // In-memory cache for demo purposes. Real apps use Redis with TTL.
    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();

    private static class OtpRecord {
        final String otp;
        final long createdAt;
        int attempts;

        OtpRecord(String otp, long createdAt) {
            this.otp = otp;
            this.createdAt = createdAt;
            this.attempts = 0;
        }
    }

    public OtpService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void generateAndSendOtp(String email) {
        // Generate a 6-digit OTP
        String otp = String.format("%06d", secureRandom.nextInt(999999));
        
        otpStore.put(email, new OtpRecord(otp, System.currentTimeMillis()));
        
        emailService.sendOtpEmail(email, otp);
    }

    public void validateOtp(String email, String inputOtp) {
        OtpRecord record = otpStore.get(email);
        
        if (record == null) {
            throw new BusinessRuleException("No active OTP session found or OTP expired");
        }
        
        if (System.currentTimeMillis() - record.createdAt > 5 * 60 * 1000) { // 5 mins
            otpStore.remove(email);
            throw new BusinessRuleException("OTP expired");
        }
        
        record.attempts++;
        if (record.attempts > 5) {
            otpStore.remove(email);
            throw new BusinessRuleException("Maximum OTP attempts exceeded. Please request a new OTP.");
        }
        
        if (!record.otp.equals(inputOtp)) {
            throw new BusinessRuleException("Invalid OTP provided");
        }
        
        // OTP is valid, clear it
        otpStore.remove(email);
    }

    @Scheduled(fixedRate = 60000) // Runs every minute
    public void cleanupExpiredOtps() {
        long expiryTime = System.currentTimeMillis() - (5 * 60 * 1000);
        otpStore.entrySet().removeIf(entry -> entry.getValue().createdAt < expiryTime);
    }
}
