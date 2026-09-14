package dev.nishanta.wallet.common.exception;

public class InsufficientBalanceException extends BusinessRuleException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
