package com.devin.bank.service;

import com.devin.bank.exception.AccountNotFoundException;
import com.devin.bank.exception.AuthenticationException;
import com.devin.bank.exception.BankException;
import com.devin.bank.exception.DuplicateUserException;
import com.devin.bank.exception.InsufficientFundsException;
import com.devin.bank.model.Account;
import com.devin.bank.model.Transaction;
import com.devin.bank.model.TransactionType;
import com.devin.bank.model.User;
import com.devin.bank.persistence.BankRepository;
import com.devin.bank.security.PasswordHasher;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The core banking logic: registration, authentication and money movement.
 *
 * <p>The service owns all business rules (validation, ownership checks, balance
 * invariants) and persists after every state change so data is never lost if the
 * process exits. It is deliberately free of any console/IO concerns.</p>
 */
public class BankService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final BankRepository repository;
    private final PasswordHasher passwordHasher;
    private final SecureRandom random = new SecureRandom();

    public BankService(BankRepository repository, PasswordHasher passwordHasher) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
    }

    /**
     * Registers a new user and opens their first personal account.
     *
     * @return the freshly opened account for the new user.
     */
    public Account register(String username, String fullName, char[] password) {
        validateUsername(username);
        validatePassword(password);
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new BankException("Full name is required");
        }
        if (repository.findUser(username).isPresent()) {
            throw new DuplicateUserException("Username '" + username + "' is already taken");
        }
        String salt = passwordHasher.newSalt();
        String hash = passwordHasher.hash(password, salt);
        User user = new User(username, fullName.trim(), hash, salt);
        repository.saveUser(user);
        Account account = openAccount(username);
        repository.flush();
        return account;
    }

    /** Authenticates a user, returning the {@link User} on success. */
    public User authenticate(String username, char[] password) {
        User user = repository.findUser(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));
        if (!passwordHasher.verify(password, user.getPasswordSalt(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password");
        }
        return user;
    }

    /** Opens an additional account for an existing user. */
    public Account openAccount(String username) {
        User user = repository.findUser(username)
                .orElseThrow(() -> new AuthenticationException("Unknown user"));
        Account account = new Account(generateAccountNumber(), user.getUsername());
        repository.saveAccount(account);
        repository.flush();
        return account;
    }

    public List<Account> accountsForUser(String username) {
        return repository.accountsForUser(username);
    }

    public Account getOwnedAccount(String username, String accountNumber) {
        Account account = repository.findAccount(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("No account " + accountNumber));
        if (!account.getOwnerUsername().equalsIgnoreCase(username)) {
            throw new AuthenticationException("You do not own account " + accountNumber);
        }
        return account;
    }

    public Account deposit(String username, String accountNumber, long amountCents, String description) {
        requirePositive(amountCents);
        Account account = getOwnedAccount(username, accountNumber);
        account.credit(amountCents);
        record(account, TransactionType.DEPOSIT, amountCents, description);
        repository.saveAccount(account);
        repository.flush();
        return account;
    }

    public Account withdraw(String username, String accountNumber, long amountCents, String description) {
        requirePositive(amountCents);
        Account account = getOwnedAccount(username, accountNumber);
        if (amountCents > account.getBalanceCents()) {
            throw new InsufficientFundsException("Insufficient funds in account " + accountNumber);
        }
        account.debit(amountCents);
        record(account, TransactionType.WITHDRAWAL, amountCents, description);
        repository.saveAccount(account);
        repository.flush();
        return account;
    }

    public void transfer(String username, String fromAccountNumber, String toAccountNumber,
                         long amountCents, String description) {
        requirePositive(amountCents);
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new BankException("Cannot transfer to the same account");
        }
        Account from = getOwnedAccount(username, fromAccountNumber);
        Account to = repository.findAccount(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("No destination account " + toAccountNumber));
        if (amountCents > from.getBalanceCents()) {
            throw new InsufficientFundsException("Insufficient funds in account " + fromAccountNumber);
        }
        from.debit(amountCents);
        to.credit(amountCents);
        String note = description == null || description.isEmpty()
                ? "Transfer" : description;
        record(from, TransactionType.TRANSFER_OUT, amountCents, "To " + toAccountNumber + ": " + note);
        record(to, TransactionType.TRANSFER_IN, amountCents, "From " + fromAccountNumber + ": " + note);
        repository.saveAccount(from);
        repository.saveAccount(to);
        repository.flush();
    }

    public List<Transaction> history(String username, String accountNumber) {
        return getOwnedAccount(username, accountNumber).getTransactions();
    }

    private void record(Account account, TransactionType type, long amountCents, String description) {
        Transaction tx = new Transaction(
                UUID.randomUUID().toString(),
                account.getAccountNumber(),
                type,
                amountCents,
                account.getBalanceCents(),
                Instant.now(),
                description);
        account.addTransaction(tx);
    }

    private String generateAccountNumber() {
        String candidate;
        do {
            long n = (long) (1_000_000_000L + (Math.floorMod(random.nextLong(), 9_000_000_000L)));
            candidate = Long.toString(n);
        } while (repository.findAccount(candidate).isPresent());
        return candidate;
    }

    private void validateUsername(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new BankException(
                    "Username must be 3-20 characters: letters, digits or underscore");
        }
    }

    private void validatePassword(char[] password) {
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            throw new BankException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }

    private static void requirePositive(long amountCents) {
        if (amountCents <= 0) {
            throw new BankException("Amount must be greater than zero");
        }
    }
}
