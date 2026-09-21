package dev.nishanta.wallet.modules.fraud.controller;

import dev.nishanta.wallet.modules.fraud.domain.FraudConfig;
import dev.nishanta.wallet.modules.fraud.dto.FraudConfigRequest;
import dev.nishanta.wallet.modules.fraud.repository.FraudConfigRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/fraud-config")
@PreAuthorize("hasRole('ADMIN')")
public class FraudConfigController {

    private final FraudConfigRepository fraudConfigRepository;

    public FraudConfigController(FraudConfigRepository fraudConfigRepository) {
        this.fraudConfigRepository = fraudConfigRepository;
    }

    @GetMapping
    public FraudConfig getConfig() {
        return fraudConfigRepository.findById(1)
                .orElseGet(FraudConfig::createDefault);
    }

    @PutMapping
    public FraudConfig updateConfig(@Valid @RequestBody FraudConfigRequest request) {
        FraudConfig config = fraudConfigRepository.findById(1).orElse(new FraudConfig());
        config.setColdStartThreshold(request.coldStartThreshold());
        config.setMinHistoryForBaseline(request.minHistoryForBaseline());
        config.setAverageMultiplier(request.averageMultiplier());
        config.setMaxGeoDistanceKm(request.maxGeoDistanceKm());
        config.setVelocityWindowMinutes(request.velocityWindowMinutes());
        config.setVelocityLookbackWindows(request.velocityLookbackWindows());
        config.setVelocityColdStartMax(request.velocityColdStartMax());
        config.setVelocityMultiplier(request.velocityMultiplier());
        return fraudConfigRepository.save(config);
    }
}
