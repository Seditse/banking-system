package com.devin.bank.exception;

/** Thrown when registering a username that is already taken. */
public class DuplicateUserException extends BankException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
