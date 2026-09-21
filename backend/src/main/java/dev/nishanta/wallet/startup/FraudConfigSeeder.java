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
            FraudConfig defaultConfig = FraudConfig.createDefault();
            fraudConfigRepository.save(defaultConfig);
        }
    }
}
