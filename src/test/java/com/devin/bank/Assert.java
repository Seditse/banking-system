package com.devin.bank;

/** Minimal assertion helpers used by the test suite. */
public final class Assert {

    private Assert() {
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertThrows(Class<? extends Throwable> expected, Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                return;
            }
            throw new AssertionError("expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
        throw new AssertionError("expected " + expected.getSimpleName() + " but nothing was thrown");
    }
}
