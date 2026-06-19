package com.devin.bank.exception;

/** Thrown when sign-in fails or an operation is attempted without a valid session. */
public class AuthenticationException extends BankException {

    public AuthenticationException(String message) {
        super(message);
    }
}
