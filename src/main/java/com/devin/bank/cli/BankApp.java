package com.devin.bank.cli;

import com.devin.bank.exception.BankException;
import com.devin.bank.model.Account;
import com.devin.bank.model.Money;
import com.devin.bank.model.Transaction;
import com.devin.bank.model.User;
import com.devin.bank.persistence.BankRepository;
import com.devin.bank.persistence.FileBankRepository;
import com.devin.bank.security.PasswordHasher;
import com.devin.bank.service.BankService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Console entry point for the banking system. Presents a menu-driven interface
 * for signing up, signing in and managing personal accounts.
 */
public class BankApp {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final ConsoleIo io;
    private final BankService bank;
    private User currentUser;

    public BankApp(ConsoleIo io, BankService bank) {
        this.io = io;
        this.bank = bank;
    }

    public static void main(String[] args) {
        Path dataFile = args.length > 0
                ? Paths.get(args[0])
                : Paths.get(System.getProperty("user.home"), ".devin-bank", "bank-data.txt");
        BankRepository repository = new FileBankRepository(dataFile);
        BankService bank = new BankService(repository, new PasswordHasher());
        new BankApp(new ConsoleIo(), bank).run();
    }

    public void run() {
        io.println("==============================================");
        io.println("        Welcome to the Devin Bank CLI");
        io.println("==============================================");
        boolean running = true;
        while (running) {
            running = currentUser == null ? showAuthMenu() : showAccountMenu();
        }
        io.println("Goodbye!");
    }

    // ----- Authentication menu (signed out) -----

    private boolean showAuthMenu() {
        io.println();
        io.println("1) Sign up");
        io.println("2) Sign in");
        io.println("3) Exit");
        String choice = io.readLine("Choose an option: ");
        if (choice == null) {
            return false;
        }
        switch (choice.trim()) {
            case "1":
                handleSignUp();
                return true;
            case "2":
                handleSignIn();
                return true;
            case "3":
                return false;
            default:
                io.println("Please choose 1, 2 or 3.");
                return true;
        }
    }

    private void handleSignUp() {
        try {
            String username = io.readLine("Choose a username (3-20 letters/digits/_): ");
            String fullName = io.readLine("Full name: ");
            char[] password = io.readPassword("Choose a password (min 8 chars): ");
            char[] confirm = io.readPassword("Confirm password: ");
            try {
                if (!Arrays.equals(password, confirm)) {
                    io.println("Passwords do not match.");
                    return;
                }
                Account account = bank.register(username, fullName, password);
                io.println("Account created! Your account number is " + account.getAccountNumber());
            } finally {
                clear(password);
                clear(confirm);
            }
        } catch (BankException e) {
            io.println("Sign-up failed: " + e.getMessage());
        }
    }

    private void handleSignIn() {
        try {
            String username = io.readLine("Username: ");
            char[] password = io.readPassword("Password: ");
            try {
                currentUser = bank.authenticate(username, password);
                io.println("Welcome back, " + currentUser.getFullName() + "!");
            } finally {
                clear(password);
            }
        } catch (BankException e) {
            io.println("Sign-in failed: " + e.getMessage());
        }
    }

    // ----- Account menu (signed in) -----

    private boolean showAccountMenu() {
        io.println();
        io.println("--- Signed in as " + currentUser.getUsername() + " ---");
        io.println("1) View accounts & balances");
        io.println("2) Deposit");
        io.println("3) Withdraw");
        io.println("4) Transfer");
        io.println("5) Transaction history");
        io.println("6) Open another account");
        io.println("7) Sign out");
        String choice = io.readLine("Choose an option: ");
        if (choice == null) {
            return false;
        }
        try {
            switch (choice.trim()) {
                case "1":
                    listAccounts();
                    break;
                case "2":
                    handleDeposit();
                    break;
                case "3":
                    handleWithdraw();
                    break;
                case "4":
                    handleTransfer();
                    break;
                case "5":
                    handleHistory();
                    break;
                case "6":
                    handleOpenAccount();
                    break;
                case "7":
                    io.println("Signed out.");
                    currentUser = null;
                    break;
                default:
                    io.println("Please choose a number from 1 to 7.");
            }
        } catch (BankException e) {
            io.println("Error: " + e.getMessage());
        }
        return true;
    }

    private void listAccounts() {
        List<Account> accounts = bank.accountsForUser(currentUser.getUsername());
        if (accounts.isEmpty()) {
            io.println("You have no accounts yet.");
            return;
        }
        io.println("Your accounts:");
        for (Account account : accounts) {
            io.println("  " + account.getAccountNumber()
                    + "   balance: " + Money.format(account.getBalanceCents()));
        }
    }

    private void handleDeposit() {
        String accountNumber = chooseOwnAccount("Deposit into which account");
        if (accountNumber == null) {
            return;
        }
        long amount = readAmount("Amount to deposit: ");
        String note = io.readLine("Description (optional): ");
        Account account = bank.deposit(currentUser.getUsername(), accountNumber, amount, note);
        io.println("Deposited. New balance: " + Money.format(account.getBalanceCents()));
    }

    private void handleWithdraw() {
        String accountNumber = chooseOwnAccount("Withdraw from which account");
        if (accountNumber == null) {
            return;
        }
        long amount = readAmount("Amount to withdraw: ");
        String note = io.readLine("Description (optional): ");
        Account account = bank.withdraw(currentUser.getUsername(), accountNumber, amount, note);
        io.println("Withdrew. New balance: " + Money.format(account.getBalanceCents()));
    }

    private void handleTransfer() {
        String from = chooseOwnAccount("Transfer from which account");
        if (from == null) {
            return;
        }
        String to = io.readLine("Destination account number: ");
        long amount = readAmount("Amount to transfer: ");
        String note = io.readLine("Description (optional): ");
        bank.transfer(currentUser.getUsername(), from, to == null ? "" : to.trim(), amount, note);
        io.println("Transfer complete.");
    }

    private void handleHistory() {
        String accountNumber = chooseOwnAccount("History for which account");
        if (accountNumber == null) {
            return;
        }
        List<Transaction> history = bank.history(currentUser.getUsername(), accountNumber);
        if (history.isEmpty()) {
            io.println("No transactions yet.");
            return;
        }
        io.println("Transactions for " + accountNumber + ":");
        for (Transaction tx : history) {
            io.println(String.format("  %s  %-12s %12s  bal:%12s  %s",
                    TIME_FORMAT.format(tx.getTimestamp()),
                    tx.getType(),
                    Money.format(tx.getAmountCents()),
                    Money.format(tx.getBalanceAfterCents()),
                    tx.getDescription()));
        }
    }

    private void handleOpenAccount() {
        Account account = bank.openAccount(currentUser.getUsername());
        io.println("Opened new account " + account.getAccountNumber());
    }

    // ----- helpers -----

    private String chooseOwnAccount(String prompt) {
        List<Account> accounts = bank.accountsForUser(currentUser.getUsername());
        if (accounts.isEmpty()) {
            io.println("You have no accounts.");
            return null;
        }
        if (accounts.size() == 1) {
            return accounts.get(0).getAccountNumber();
        }
        listAccounts();
        String input = io.readLine(prompt + " (enter account number): ");
        return input == null ? null : input.trim();
    }

    private long readAmount(String prompt) {
        String raw = io.readLine(prompt);
        return Money.parseToCents(raw);
    }

    private static void clear(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
