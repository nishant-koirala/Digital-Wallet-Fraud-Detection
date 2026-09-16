package dev.nishanta.wallet.modules.transaction.service;

import dev.nishanta.wallet.common.exception.DailyLimitExceededException;
import dev.nishanta.wallet.modules.kyc.domain.KycStatus;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TransactionLimitValidator {

    private final TransactionRepository transactionRepository;

    private static final BigDecimal TIER_1_LIMIT = new BigDecimal("500");
    private static final BigDecimal TIER_2_LIMIT = new BigDecimal("5000");

    public TransactionLimitValidator(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void validateDailyLimit(Wallet wallet, BigDecimal amount) {
        KycStatus status = wallet.getUser().getKycStatus();
        BigDecimal dailyLimit = (status == KycStatus.APPROVED) ? TIER_2_LIMIT : TIER_1_LIMIT;

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        BigDecimal currentDayTotal = transactionRepository.sumAmountByFromWalletIdAndCreatedAtBetween(
                wallet.getId(), startOfDay, endOfDay);

        if (currentDayTotal.add(amount).compareTo(dailyLimit) > 0) {
            throw new DailyLimitExceededException("Transaction would exceed daily limit of Rs " + dailyLimit 
                    + " for your KYC Tier. Current usage today: Rs " + currentDayTotal);
        }
    }
}
