package dev.nishanta.wallet.startup;

import dev.nishanta.wallet.modules.fraud.domain.FraudConfig;
import dev.nishanta.wallet.modules.fraud.repository.FraudConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FraudConfigSeeder implements CommandLineRunner {

    private final FraudConfigRepository fraudConfigRepository;

    public FraudConfigSeeder(FraudConfigRepository fraudConfigRepository) {
        this.fraudConfigRepository = fraudConfigRepository;
    }

    @Override
    public void run(String... args) {
        if (fraudConfigRepository.count() == 0) {
            FraudConfig defaultConfig = new FraudConfig();
            // Optional: You can customize default thresholds here if the domain model allows it
            // defaultConfig.setHighVelocityThreshold(10);
            fraudConfigRepository.save(defaultConfig);
        }
    }
}
