package com.devin.bank;

import static com.devin.bank.Assert.assertEquals;
import static com.devin.bank.Assert.assertThrows;
import static com.devin.bank.Assert.assertTrue;

import com.devin.bank.exception.AuthenticationException;
import com.devin.bank.exception.BankException;
import com.devin.bank.exception.DuplicateUserException;
import com.devin.bank.exception.InsufficientFundsException;
import com.devin.bank.model.Account;
import com.devin.bank.persistence.InMemoryBankRepository;
import com.devin.bank.security.PasswordHasher;
import com.devin.bank.service.BankService;

/** Tests for the core {@link BankService} business rules. */
public final class BankServiceTest {

    private BankServiceTest() {
    }

    private static BankService newService() {
        return new BankService(new InMemoryBankRepository(), new PasswordHasher());
    }

    public static void register(TestRunner runner) {
        runner.test("registration creates an account and authenticates", () -> {
            BankService bank = newService();
            Account account = bank.register("alice", "Alice Smith", "password1".toCharArray());
            assertTrue(account.getAccountNumber().length() == 10, "account number is 10 digits");
            assertEquals(0L, account.getBalanceCents());
            assertEquals("alice", bank.authenticate("alice", "password1".toCharArray()).getUsername());
        });

        runner.test("authentication is case-insensitive on username", () -> {
            BankService bank = newService();
            bank.register("Bob", "Bob Jones", "password1".toCharArray());
            assertEquals("Bob", bank.authenticate("bob", "password1".toCharArray()).getUsername());
        });

        runner.test("wrong password is rejected", () -> {
            BankService bank = newService();
            bank.register("carol", "Carol", "password1".toCharArray());
            assertThrows(AuthenticationException.class,
                    () -> bank.authenticate("carol", "wrongpass1".toCharArray()));
        });

        runner.test("duplicate username is rejected", () -> {
            BankService bank = newService();
            bank.register("dave", "Dave", "password1".toCharArray());
            assertThrows(DuplicateUserException.class,
                    () -> bank.register("dave", "Dave Two", "password2".toCharArray()));
        });

        runner.test("short password and bad username are rejected", () -> {
            BankService bank = newService();
            assertThrows(BankException.class, () -> bank.register("ok", "X", "short".toCharArray()));
            assertThrows(BankException.class, () -> bank.register("a", "X", "password1".toCharArray()));
        });

        runner.test("deposit and withdraw update balance", () -> {
            BankService bank = newService();
            Account account = bank.register("erin", "Erin", "password1".toCharArray());
            String number = account.getAccountNumber();
            bank.deposit("erin", number, 10000, "paycheck");
            assertEquals(10000L, bank.getOwnedAccount("erin", number).getBalanceCents());
            bank.withdraw("erin", number, 2500, "groceries");
            assertEquals(7500L, bank.getOwnedAccount("erin", number).getBalanceCents());
        });

        runner.test("overdraft is prevented", () -> {
            BankService bank = newService();
            Account account = bank.register("frank", "Frank", "password1".toCharArray());
            assertThrows(InsufficientFundsException.class,
                    () -> bank.withdraw("frank", account.getAccountNumber(), 100, "nope"));
        });

        runner.test("transfer moves money between accounts and records both sides", () -> {
            BankService bank = newService();
            Account from = bank.register("grace", "Grace", "password1".toCharArray());
            Account to = bank.register("heidi", "Heidi", "password1".toCharArray());
            bank.deposit("grace", from.getAccountNumber(), 5000, "seed");
            bank.transfer("grace", from.getAccountNumber(), to.getAccountNumber(), 2000, "rent");
            assertEquals(3000L, bank.getOwnedAccount("grace", from.getAccountNumber()).getBalanceCents());
            assertEquals(2000L, bank.getOwnedAccount("heidi", to.getAccountNumber()).getBalanceCents());
            assertEquals(2, bank.history("grace", from.getAccountNumber()).size());
            assertEquals(1, bank.history("heidi", to.getAccountNumber()).size());
        });

        runner.test("cannot operate on an account you do not own", () -> {
            BankService bank = newService();
            Account a = bank.register("ivan", "Ivan", "password1".toCharArray());
            bank.register("judy", "Judy", "password1".toCharArray());
            assertThrows(AuthenticationException.class,
                    () -> bank.deposit("judy", a.getAccountNumber(), 100, "x"));
        });

        runner.test("transfer to same account is rejected", () -> {
            BankService bank = newService();
            Account a = bank.register("ken", "Ken", "password1".toCharArray());
            bank.deposit("ken", a.getAccountNumber(), 1000, "x");
            assertThrows(BankException.class,
                    () -> bank.transfer("ken", a.getAccountNumber(), a.getAccountNumber(), 100, "x"));
        });
    }
}
