package dev.nishanta.wallet.modules.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

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
}
