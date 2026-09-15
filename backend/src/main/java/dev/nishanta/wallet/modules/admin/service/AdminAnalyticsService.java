package dev.nishanta.wallet.modules.admin.service;

import dev.nishanta.wallet.modules.admin.dto.AnalyticsResponse;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminAnalyticsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public AdminAnalyticsService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public AnalyticsResponse getDashboardStats() {
        BigDecimal totalVolume = transactionRepository.findTotalVolume().orElse(BigDecimal.ZERO);
        long safeCount = transactionRepository.countSafeTransactions();
        long flaggedCount = transactionRepository.countFlaggedTransactions();
        long totalUsers = userRepository.count();

        Map<String, BigDecimal> volumeLast7Days = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            BigDecimal dayVolume = transactionRepository.findVolumeBetweenDates(startOfDay, endOfDay).orElse(BigDecimal.ZERO);
            volumeLast7Days.put(date.format(formatter), dayVolume);
        }

        return new AnalyticsResponse(totalVolume, safeCount, flaggedCount, totalUsers, volumeLast7Days);
    }
}
