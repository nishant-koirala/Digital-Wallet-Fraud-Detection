package dev.nishanta.wallet.modules.fraud.rules;

import dev.nishanta.wallet.modules.fraud.domain.FraudSeverity;
import dev.nishanta.wallet.modules.fraud.domain.UserDevice;
import dev.nishanta.wallet.modules.fraud.repository.UserDeviceRepository;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class DeviceAnomalyRule implements FraudRule {

    private final UserDeviceRepository userDeviceRepository;

    public DeviceAnomalyRule(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    @Override
    public FraudSeverity evaluate(Transaction transaction) {
        String deviceId = transaction.getDeviceId();
        String ipAddress = transaction.getIpAddress();
        UUID userId = transaction.getFromWallet().getUser().getId();

        // If no device or IP is recorded, we can't evaluate ATO.
        if (deviceId == null || ipAddress == null) {
            return FraudSeverity.NONE;
        }

        Optional<UserDevice> deviceOpt = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId);

        if (deviceOpt.isEmpty()) {
            // New device completely unknown to the user. High risk of ATO.
            return FraudSeverity.MAJOR;
        }

        UserDevice knownDevice = deviceOpt.get();

        if (!knownDevice.isTrusted()) {
            // Known device but flagged as untrusted
            return FraudSeverity.MAJOR;
        }

        // If it's a known trusted device but from a completely different IP address, 
        // that's suspicious but maybe they are traveling.
        if (!knownDevice.getIpAddress().equals(ipAddress)) {
            return FraudSeverity.MINOR;
        }

        return FraudSeverity.NONE;
    }

    @Override
    public String ruleName() {
        return "DEVICE_ANOMALY";
    }
}
