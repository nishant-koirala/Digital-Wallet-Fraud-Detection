package dev.nishanta.wallet.modules.fraud.repository;

import dev.nishanta.wallet.modules.fraud.domain.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {
    Optional<UserDevice> findByUserIdAndDeviceId(UUID userId, String deviceId);
    List<UserDevice> findByUserId(UUID userId);
}
