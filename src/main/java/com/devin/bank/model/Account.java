package com.devin.bank.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A personal bank account owned by a single user.
 *
 * <p>The balance is kept in minor units (cents). Mutating operations are
 * package-controlled through the service layer; the model itself only enforces
 * the invariant that a balance can never go negative.</p>
 */
public final class Account {

    private final String accountNumber;
    private final String ownerUsername;
    private long balanceCents;
    private final List<Transaction> transactions;

    public Account(String accountNumber, String ownerUsername) {
        this(accountNumber, ownerUsername, 0L, new ArrayList<>());
    }

    public Account(String accountNumber,
                   String ownerUsername,
                   long balanceCents,
                   List<Transaction> transactions) {
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber");
        this.ownerUsername = Objects.requireNonNull(ownerUsername, "ownerUsername");
        if (balanceCents < 0) {
            throw new IllegalArgumentException("balanceCents must be non-negative");
        }
        this.balanceCents = balanceCents;
        this.transactions = new ArrayList<>(Objects.requireNonNull(transactions, "transactions"));
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public long getBalanceCents() {
        return balanceCents;
    }

    /** Returns an unmodifiable, chronological view of this account's transactions. */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void credit(long amountCents) {
        requirePositive(amountCents);
        balanceCents = Math.addExact(balanceCents, amountCents);
    }

    public void debit(long amountCents) {
        requirePositive(amountCents);
        if (amountCents > balanceCents) {
            throw new IllegalStateException("Insufficient funds");
        }
        balanceCents -= amountCents;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(Objects.requireNonNull(transaction, "transaction"));
    }

    private static void requirePositive(long amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
