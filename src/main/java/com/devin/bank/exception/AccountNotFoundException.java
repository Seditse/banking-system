package com.devin.bank.exception;

/** Thrown when an account number does not correspond to any existing account. */
public class AccountNotFoundException extends BankException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}
