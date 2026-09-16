package dev.nishanta.wallet.modules.fraud.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FraudConfigRequest(
        @NotNull BigDecimal coldStartThreshold,
        @NotNull Integer minHistoryForBaseline,
        @NotNull BigDecimal averageMultiplier,
        @NotNull Double maxGeoDistanceKm,
        @NotNull Integer velocityWindowMinutes,
        @NotNull Integer velocityLookbackWindows,
        @NotNull Integer velocityColdStartMax,
        @NotNull Double velocityMultiplier
) {}
