package dev.nishanta.wallet.modules.admin.dto;

import java.math.BigDecimal;
import java.util.Map;

public record AnalyticsResponse(
        BigDecimal totalVolume,
        long safeCount,
        long flaggedCount,
        long totalUsers,
        Map<String, BigDecimal> volumeLast7Days
) {
}
