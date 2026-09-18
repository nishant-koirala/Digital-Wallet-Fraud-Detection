package dev.nishanta.wallet.modules.admin.dto;

import java.time.LocalDateTime;

public class ReportUserDTO {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private String kycStatus;
    private LocalDateTime createdAt;

    public ReportUserDTO(String id, String name, String email, String phoneNumber, String role, String kycStatus, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.kycStatus = kycStatus;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
