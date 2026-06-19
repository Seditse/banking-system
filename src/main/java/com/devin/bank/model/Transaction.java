package com.devin.bank.model;

import java.time.Instant;
import java.util.Objects;

/**
 * An immutable record of a single change to an account balance.
 *
 * <p>Monetary amounts are stored as a non-negative number of minor units
 * (cents) to avoid floating-point rounding errors. The {@link TransactionType}
 * indicates the direction of the movement.</p>
 */
public final class Transaction {

    private final String id;
    private final String accountNumber;
    private final TransactionType type;
    private final long amountCents;
    private final long balanceAfterCents;
    private final Instant timestamp;
    private final String description;

    public Transaction(String id,
                        String accountNumber,
                        TransactionType type,
                        long amountCents,
                        long balanceAfterCents,
                        Instant timestamp,
                        String description) {
        this.id = Objects.requireNonNull(id, "id");
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber");
        this.type = Objects.requireNonNull(type, "type");
        if (amountCents < 0) {
            throw new IllegalArgumentException("amountCents must be non-negative");
        }
        this.amountCents = amountCents;
        this.balanceAfterCents = balanceAfterCents;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.description = description == null ? "" : description;
    }

    public String getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public long getBalanceAfterCents() {
        return balanceAfterCents;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}
