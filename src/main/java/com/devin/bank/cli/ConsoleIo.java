package com.devin.bank.cli;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Thin wrapper around console input/output. When a real {@link Console} is
 * available it is used for masked password entry; otherwise it degrades
 * gracefully to a buffered reader (e.g. when input is piped from a file or test).
 */
public class ConsoleIo {

    private final Console console;
    private final BufferedReader reader;
    private final PrintStream out;

    public ConsoleIo() {
        this(System.console(), System.in, System.out);
    }

    public ConsoleIo(Console console, InputStream in, PrintStream out) {
        this.console = console;
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
    }

    public void print(String text) {
        out.print(text);
    }

    public void println(String text) {
        out.println(text);
    }

    public void println() {
        out.println();
    }

    /** Prompts and reads a line of text. Returns {@code null} at end of input. */
    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        if (console != null) {
            return console.readLine();
        }
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Prompts and reads a password, masking it when a real console is present. */
    public char[] readPassword(String prompt) {
        if (console != null) {
            return console.readPassword(prompt);
        }
        String line = readLine(prompt);
        return line == null ? new char[0] : line.toCharArray();
    }
}
