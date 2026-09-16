package dev.nishanta.wallet.common.exception;

public class DailyLimitExceededException extends BusinessRuleException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}
