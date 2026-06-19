package com.devin.bank.model;

import java.util.Objects;

/**
 * A registered customer who can sign in and own a personal account.
 *
 * <p>The plaintext password is never stored. Only a salted PBKDF2 hash and its
 * salt are persisted (see {@code com.devin.bank.security.PasswordHasher}).</p>
 */
public final class User {

    private final String username;
    private final String fullName;
    private final String passwordHash;
    private final String passwordSalt;

    public User(String username, String fullName, String passwordHash, String passwordSalt) {
        this.username = Objects.requireNonNull(username, "username");
        this.fullName = Objects.requireNonNull(fullName, "fullName");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.passwordSalt = Objects.requireNonNull(passwordSalt, "passwordSalt");
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }
}
