package dev.nishanta.wallet.modules.fraud.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_devices")
public class UserDevice {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "is_trusted", nullable = false)
    private boolean isTrusted;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    protected UserDevice() {}

    public UserDevice(UUID userId, String deviceId, String ipAddress, boolean isTrusted) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.isTrusted = isTrusted;
        this.lastSeenAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isTrusted() {
        return isTrusted;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setTrusted(boolean trusted) {
        isTrusted = trusted;
    }
}
