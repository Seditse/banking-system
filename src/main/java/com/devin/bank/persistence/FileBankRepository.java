package com.devin.bank.persistence;

import com.devin.bank.exception.BankException;
import com.devin.bank.model.Account;
import com.devin.bank.model.Transaction;
import com.devin.bank.model.TransactionType;
import com.devin.bank.model.User;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A {@link BankRepository} that keeps everything in memory for fast access and
 * persists the full dataset to a plain-text file on every {@link #flush()}.
 *
 * <p>The on-disk format is line-oriented and pipe-delimited. Free-text fields
 * (full name, transaction description) are Base64-encoded so they can never
 * collide with the delimiter:</p>
 * <pre>
 *   U|username|fullNameB64|passwordHash|passwordSalt
 *   A|accountNumber|ownerUsername|balanceCents
 *   T|accountNumber|txId|type|amountCents|balanceAfterCents|epochMillis|descriptionB64
 * </pre>
 *
 * <p>Writes go to a temporary file that is atomically moved into place, so a
 * crash mid-write cannot corrupt the existing data file.</p>
 */
public class FileBankRepository extends InMemoryBankRepository {

    private static final String DELIMITER = "\\|";
    private static final String SEP = "|";

    private final Path dataFile;

    public FileBankRepository(Path dataFile) {
        this.dataFile = dataFile;
        load();
    }

    private void load() {
        if (!Files.exists(dataFile)) {
            return;
        }
        final List<String> lines;
        try {
            lines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BankException("Unable to read data file: " + dataFile, e);
        }
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(DELIMITER, -1);
            try {
                switch (parts[0]) {
                    case "U":
                        loadUser(parts);
                        break;
                    case "A":
                        loadAccount(parts);
                        break;
                    case "T":
                        loadTransaction(parts);
                        break;
                    default:
                        throw new BankException("Unknown record type '" + parts[0] + "'");
                }
            } catch (RuntimeException e) {
                throw new BankException("Corrupt data file at line " + lineNumber + ": " + e.getMessage(), e);
            }
        }
    }

    private void loadUser(String[] p) {
        usersByName.put(p[1].toLowerCase(),
                new User(p[1], decode(p[2]), p[3], p[4]));
    }

    private void loadAccount(String[] p) {
        accountsByNumber.put(p[1],
                new Account(p[1], p[2], Long.parseLong(p[3]), new ArrayList<>()));
    }

    private void loadTransaction(String[] p) {
        Account account = accountsByNumber.get(p[1]);
        if (account == null) {
            throw new BankException("transaction references unknown account " + p[1]);
        }
        Transaction tx = new Transaction(
                p[2],
                p[1],
                TransactionType.valueOf(p[3]),
                Long.parseLong(p[4]),
                Long.parseLong(p[5]),
                Instant.ofEpochMilli(Long.parseLong(p[6])),
                decode(p[7]));
        account.addTransaction(tx);
    }

    @Override
    public void flush() {
        StringBuilder sb = new StringBuilder();
        for (User user : usersByName.values()) {
            sb.append("U").append(SEP)
                    .append(user.getUsername()).append(SEP)
                    .append(encode(user.getFullName())).append(SEP)
                    .append(user.getPasswordHash()).append(SEP)
                    .append(user.getPasswordSalt()).append('\n');
        }
        for (Account account : accountsByNumber.values()) {
            sb.append("A").append(SEP)
                    .append(account.getAccountNumber()).append(SEP)
                    .append(account.getOwnerUsername()).append(SEP)
                    .append(account.getBalanceCents()).append('\n');
            for (Transaction tx : account.getTransactions()) {
                sb.append("T").append(SEP)
                        .append(tx.getAccountNumber()).append(SEP)
                        .append(tx.getId()).append(SEP)
                        .append(tx.getType().name()).append(SEP)
                        .append(tx.getAmountCents()).append(SEP)
                        .append(tx.getBalanceAfterCents()).append(SEP)
                        .append(tx.getTimestamp().toEpochMilli()).append(SEP)
                        .append(encode(tx.getDescription())).append('\n');
            }
        }
        writeAtomically(sb.toString());
    }

    private void writeAtomically(String content) {
        try {
            Path parent = dataFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = Files.createTempFile(parent, "bank", ".tmp");
            Files.write(tmp, content.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(tmp, dataFile,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                Files.move(tmp, dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to persist data file: " + dataFile, e);
        }
    }

    private static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String text) {
        return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    }
}
