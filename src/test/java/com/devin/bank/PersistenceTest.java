package com.devin.bank;

import static com.devin.bank.Assert.assertEquals;
import static com.devin.bank.Assert.assertTrue;

import com.devin.bank.model.Account;
import com.devin.bank.persistence.FileBankRepository;
import com.devin.bank.security.PasswordHasher;
import com.devin.bank.service.BankService;
import java.nio.file.Files;
import java.nio.file.Path;

/** Tests that data survives a restart via {@link FileBankRepository}. */
public final class PersistenceTest {

    private PersistenceTest() {
    }

    public static void register(TestRunner runner) {
        runner.test("data persists and reloads across repository instances", () -> {
            Path tmp = Files.createTempFile("bank-test", ".txt");
            Files.deleteIfExists(tmp);
            try {
                String number;
                {
                    BankService bank = new BankService(new FileBankRepository(tmp), new PasswordHasher());
                    Account account = bank.register("mallory", "Mallory O'Brien | the great",
                            "password1".toCharArray());
                    number = account.getAccountNumber();
                    bank.deposit("mallory", number, 12345, "initial | deposit");
                    bank.withdraw("mallory", number, 345, "fee");
                }

                BankService reloaded =
                        new BankService(new FileBankRepository(tmp), new PasswordHasher());
                assertEquals("mallory",
                        reloaded.authenticate("mallory", "password1".toCharArray()).getUsername());
                Account account = reloaded.getOwnedAccount("mallory", number);
                assertEquals(12000L, account.getBalanceCents());
                assertEquals(2, reloaded.history("mallory", number).size());
                assertTrue(reloaded.authenticate("mallory", "password1".toCharArray())
                                .getFullName().contains("|"),
                        "free-text with delimiter survives round trip");
            } finally {
                Files.deleteIfExists(tmp);
            }
        });
    }
}
