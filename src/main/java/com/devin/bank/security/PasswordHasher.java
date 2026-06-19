package com.devin.bank.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Salted, iterated password hashing built entirely on the JDK (PBKDF2 with
 * HMAC-SHA-256). No third-party libraries are required.
 *
 * <p>Each password gets a fresh random 16-byte salt. Verification recomputes the
 * hash with the stored salt and compares using a constant-time check to avoid
 * leaking information through timing.</p>
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private final SecureRandom random = new SecureRandom();

    /** Generates a new random salt encoded as Base64. */
    public String newSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Computes the Base64-encoded PBKDF2 hash of {@code password} with {@code saltBase64}. */
    public String hash(char[] password, String saltBase64) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to hash password", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Returns true if {@code password} matches the stored hash for {@code saltBase64}. */
    public boolean verify(char[] password, String saltBase64, String expectedHashBase64) {
        String actual = hash(password, saltBase64);
        return constantTimeEquals(actual, expectedHashBase64);
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] x = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] y = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (x.length != y.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < x.length; i++) {
            result |= x[i] ^ y[i];
        }
        return result == 0;
    }
}
