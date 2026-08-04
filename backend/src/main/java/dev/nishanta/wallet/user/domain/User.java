package dev.nishanta.wallet.user.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)

    private UUID id;

    private String name;

    private String email;

    private String passwordHash;

    private String phoneNumber;

    private String pinHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // JPA requires a no-arg constructor for its own internal use
    protected User() {
    }

    // This is the constructor YOU call when creating a new user
    public User(String name, String email, String passwordHash, Role role, String phoneNumber) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // getters — no setters on id/createdAt, they shouldn't change after creation
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPinHash() { return pinHash; }

    public void setPin(String pinHash) {
        this.pinHash = pinHash;
    }
}
