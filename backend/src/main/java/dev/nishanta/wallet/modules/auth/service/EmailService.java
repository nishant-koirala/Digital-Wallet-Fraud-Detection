package dev.nishanta.wallet.modules.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to Digital Wallet!");
        message.setText("Hello " + name + ",\n\n" +
                "Welcome to Digital Wallet! Your account has been successfully created and your personal wallet is ready to use.\n\n" +
                "Thank you,\n" +
                "Digital Wallet Team");
        mailSender.send(message);
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your Transfer OTP Code");
            message.setText("You are attempting a high-value transfer.\n\n" +
                    "Your One-Time Password (OTP) is: " + otpCode + "\n\n" +
                    "This code will expire in 5 minutes. Do not share it with anyone.");
            
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send OTP email to {}. Fallback to printing OTP.", toEmail);
            log.info("==== DEMO FALLBACK: OTP for {} is [{}] ====", toEmail, otpCode);
        }
    }
}
