package com.devin.bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Helpers for converting between a human-facing decimal amount (e.g. "12.50")
 * and the internal minor-unit (cents) representation used throughout the system.
 */
public final class Money {

    private Money() {
    }

    /**
     * Parses a positive decimal amount into cents.
     *
     * @throws IllegalArgumentException if the text is not a valid, strictly
     *                                  positive amount with at most two decimals.
     */
    public static long parseToCents(String text) {
        if (text == null) {
            throw new IllegalArgumentException("amount is required");
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("amount is required");
        }
        final BigDecimal value;
        try {
            value = new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a valid amount");
        }
        if (value.scale() > 2) {
            throw new IllegalArgumentException("amount cannot have more than 2 decimal places");
        }
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        return value.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }

    /** Formats cents as a fixed two-decimal string, e.g. {@code 1250 -> "12.50"}. */
    public static String format(long cents) {
        return BigDecimal.valueOf(cents).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }
}
