package com.devin.bank;

import static com.devin.bank.Assert.assertEquals;
import static com.devin.bank.Assert.assertThrows;

import com.devin.bank.model.Money;

/** Tests for {@link Money} parsing and formatting. */
public final class MoneyTest {

    private MoneyTest() {
    }

    public static void register(TestRunner runner) {
        runner.test("parses whole and decimal amounts to cents", () -> {
            assertEquals(1250L, Money.parseToCents("12.50"));
            assertEquals(100L, Money.parseToCents("1"));
            assertEquals(5L, Money.parseToCents("0.05"));
        });

        runner.test("formats cents with two decimals", () -> {
            assertEquals("12.50", Money.format(1250));
            assertEquals("0.05", Money.format(5));
            assertEquals("1000.00", Money.format(100000));
        });

        runner.test("rejects zero, negative and over-precise amounts", () -> {
            assertThrows(IllegalArgumentException.class, () -> Money.parseToCents("0"));
            assertThrows(IllegalArgumentException.class, () -> Money.parseToCents("-5"));
            assertThrows(IllegalArgumentException.class, () -> Money.parseToCents("1.234"));
            assertThrows(IllegalArgumentException.class, () -> Money.parseToCents("abc"));
        });
    }
}
