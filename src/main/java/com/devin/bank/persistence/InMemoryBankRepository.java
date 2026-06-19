package com.devin.bank.persistence;

import com.devin.bank.model.Account;
import com.devin.bank.model.User;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A simple in-memory {@link BankRepository}. Useful as a base for persistent
 * stores and for tests. Usernames are treated case-insensitively.
 */
public class InMemoryBankRepository implements BankRepository {

    protected final Map<String, User> usersByName = new LinkedHashMap<>();
    protected final Map<String, Account> accountsByNumber = new LinkedHashMap<>();

    @Override
    public Optional<User> findUser(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByName.get(username.toLowerCase()));
    }

    @Override
    public void saveUser(User user) {
        Objects.requireNonNull(user, "user");
        usersByName.put(user.getUsername().toLowerCase(), user);
    }

    @Override
    public Optional<Account> findAccount(String accountNumber) {
        if (accountNumber == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsByNumber.get(accountNumber));
    }

    @Override
    public void saveAccount(Account account) {
        Objects.requireNonNull(account, "account");
        accountsByNumber.put(account.getAccountNumber(), account);
    }

    @Override
    public List<Account> accountsForUser(String username) {
        List<Account> result = new ArrayList<>();
        if (username == null) {
            return result;
        }
        for (Account account : accountsByNumber.values()) {
            if (account.getOwnerUsername().equalsIgnoreCase(username)) {
                result.add(account);
            }
        }
        return result;
    }

    @Override
    public void flush() {
        // Nothing to persist for the in-memory implementation.
    }
}
