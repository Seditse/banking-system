package com.devin.bank.persistence;

import com.devin.bank.model.Account;
import com.devin.bank.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction for users and accounts. Implementations decide whether
 * data lives only in memory or is also persisted to disk.
 */
public interface BankRepository {

    Optional<User> findUser(String username);

    void saveUser(User user);

    Optional<Account> findAccount(String accountNumber);

    /** Inserts or replaces the account identified by its account number. */
    void saveAccount(Account account);

    List<Account> accountsForUser(String username);

    /** Persists any pending changes to the backing store. A no-op for in-memory stores. */
    void flush();
}
