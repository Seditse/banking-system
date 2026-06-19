package com.devin.bank.exception;

/** Thrown when a withdrawal or transfer exceeds the available balance. */
public class InsufficientFundsException extends BankException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
