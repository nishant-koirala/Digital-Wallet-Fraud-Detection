package dev.nishanta.wallet.modules.admin.service;

import dev.nishanta.wallet.modules.admin.dto.AnalyticsResponse;
import dev.nishanta.wallet.modules.fraud.repository.FraudFlagRepository;
import dev.nishanta.wallet.modules.transaction.domain.TransactionStatus;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final FraudFlagRepository fraudFlagRepository;
    private final UserRepository userRepository;

    public AnalyticsService(TransactionRepository transactionRepository,
                            FraudFlagRepository fraudFlagRepository,
                            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.fraudFlagRepository = fraudFlagRepository;
        this.userRepository = userRepository;
    }

    public AnalyticsResponse getDashboardStats() {
        // Simplified queries for demo purposes.
        long totalUsers = userRepository.count();
        long flaggedCount = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.FLAGGED || t.getStatus() == TransactionStatus.FAILED)
                .count();
        long safeCount = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .count();
        
        BigDecimal totalVolume = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Daily volume for the last 7 days
        Map<String, BigDecimal> volumeLast7Days = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.atTime(23, 59, 59);
            
            BigDecimal dayVol = transactionRepository.findAll().stream()
                    .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                    .filter(t -> t.getCreatedAt().isAfter(start) && t.getCreatedAt().isBefore(end))
                    .map(t -> t.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            volumeLast7Days.put(day.getDayOfWeek().name().substring(0, 3), dayVol);
        }

        return new AnalyticsResponse(totalVolume, safeCount, flaggedCount, totalUsers, volumeLast7Days);
    }
}
