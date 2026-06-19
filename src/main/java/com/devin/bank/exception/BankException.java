package com.devin.bank.exception;

/**
 * Base type for all expected, user-facing banking errors. These represent
 * recoverable conditions (bad input, business-rule violations) rather than
 * programming bugs, and carry a message safe to show to the end user.
 */
public class BankException extends RuntimeException {

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
