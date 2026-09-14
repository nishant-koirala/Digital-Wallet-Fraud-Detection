package dev.nishanta.wallet.common.exception;

public class OtpRequiredException extends RuntimeException {
    public OtpRequiredException(String message) {
        super(message);
    }
}
