package com.devin.bank;

import java.util.ArrayList;
import java.util.List;

/**
 * A tiny, dependency-free test harness so the whole project can be built and
 * tested with nothing but the JDK ("only Java"). Each test is a named runnable
 * that throws on failure; the runner reports a summary and exits non-zero if any
 * test fails, which CI uses as the pass/fail signal.
 */
public final class TestRunner {

    @FunctionalInterface
    public interface Check {
        void run() throws Exception;
    }

    private final List<String> failures = new ArrayList<>();
    private int passed;

    public void test(String name, Check check) {
        try {
            check.run();
            passed++;
            System.out.println("  [PASS] " + name);
        } catch (Throwable t) {
            failures.add(name + " -> " + t);
            System.out.println("  [FAIL] " + name + " -> " + t);
        }
    }

    public int summary() {
        System.out.println();
        System.out.println("Passed: " + passed + ", Failed: " + failures.size());
        return failures.isEmpty() ? 0 : 1;
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        System.out.println("Running BankServiceTest...");
        BankServiceTest.register(runner);
        System.out.println("Running MoneyTest...");
        MoneyTest.register(runner);
        System.out.println("Running PersistenceTest...");
        PersistenceTest.register(runner);
        int code = runner.summary();
        if (code != 0) {
            System.exit(code);
        }
    }
}
