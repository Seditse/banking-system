# Devin Bank — A Complete Banking System in Pure Java

A console-based personal banking system written entirely in Java with **no
external libraries** — it builds and runs with nothing but a JDK (17+).

## Features

- **User sign-up & sign-in** with usernames and secure passwords
- **Salted password hashing** using PBKDF2 (HMAC-SHA-256) from the JDK — plaintext
  passwords are never stored, and verification is constant-time
- **Personal accounts** — each user gets an account on sign-up and can open more
- **Banking operations**: deposit, withdraw, transfer between accounts
- **Transaction history** per account, with timestamps and running balances
- **Persistent storage** to a local file with atomic, crash-safe writes
- **Money handled in integer cents** (no floating-point rounding bugs)
- **Dependency-free test suite** runnable with just the JDK

## Requirements

- Java 17 or newer (`java -version`)

## Build, Test, Run

```bash
./build.sh   # compile main + test sources into out/
./test.sh    # compile and run the full test suite
./run.sh     # build (if needed) and launch the interactive CLI
```

By default the CLI stores data in `~/.devin-bank/bank-data.txt`. Pass a path to
override it (handy for trying it out without touching your home directory):

```bash
./run.sh /tmp/my-bank-data.txt
```

## Example session

```
==============================================
        Welcome to the Devin Bank CLI
==============================================

1) Sign up
2) Sign in
3) Exit
Choose an option: 1
Choose a username (3-20 letters/digits/_): alice
Full name: Alice Smith
Choose a password (min 8 chars): ********
Confirm password: ********
Account created! Your account number is 1234567890
```

## Project layout

```
src/main/java/com/devin/bank/
  model/        Account, User, Transaction, TransactionType, Money
  security/     PasswordHasher (PBKDF2)
  persistence/  BankRepository, InMemoryBankRepository, FileBankRepository
  service/      BankService (all business rules)
  exception/    BankException hierarchy
  cli/          BankApp (menu), ConsoleIo (input/output)
src/test/java/com/devin/bank/
  TestRunner, Assert, *Test   (dependency-free tests)
```

## Design notes

- **Layered architecture**: the CLI talks only to `BankService`, which owns every
  business rule and persists after each change. Swapping the console for another
  front-end (web, GUI) would not touch the core logic.
- **Storage is an interface** (`BankRepository`) with an in-memory implementation
  (used by tests) and a file-backed one (used by the app).
- **Security**: per-user random salts, 120k PBKDF2 iterations, constant-time hash
  comparison, and passwords cleared from memory after use.
