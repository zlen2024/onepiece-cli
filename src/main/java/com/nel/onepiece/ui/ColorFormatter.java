package com.nel.onepiece.ui;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Utility class for colored terminal output using ANSI escape codes.
 * Provides consistent color scheme across the application.
 */
@ApplicationScoped
public class ColorFormatter {

    @PostConstruct
    void init() {
        // Initialize Jansi for Windows compatibility
        AnsiConsole.systemInstall();
    }

    @PreDestroy
    void cleanup() {
        AnsiConsole.systemUninstall();
    }

    /**
     * Format text with success color (green)
     */
    public String success(String text) {
        return Ansi.ansi().fgGreen().a(text).reset().toString();
    }

    /**
     * Format text with error color (red)
     */
    public String error(String text) {
        return Ansi.ansi().fgRed().a(text).reset().toString();
    }

    /**
     * Format text with warning color (yellow)
     */
    public String warning(String text) {
        return Ansi.ansi().fgYellow().a(text).reset().toString();
    }

    /**
     * Format text with info color (cyan)
     */
    public String info(String text) {
        return Ansi.ansi().fgCyan().a(text).reset().toString();
    }

    /**
     * Format text with highlight color (bright blue)
     */
    public String highlight(String text) {
        return Ansi.ansi().fgBrightBlue().a(text).reset().toString();
    }

    /**
     * Format text with muted color (gray)
     */
    public String muted(String text) {
        return Ansi.ansi().fgBrightBlack().a(text).reset().toString();
    }

    /**
     * Format text with bold style
     */
    public String bold(String text) {
        return Ansi.ansi().bold().a(text).reset().toString();
    }

    /**
     * Format success message with checkmark
     */
    public String successMessage(String message) {
        return success("✓ " + message);
    }

    /**
     * Format error message with X mark
     */
    public String errorMessage(String message) {
        return error("✗ " + message);
    }

    /**
     * Format warning message with warning symbol
     */
    public String warningMessage(String message) {
        return warning("⚠️  " + message);
    }

    /**
     * Format info message with info symbol
     */
    public String infoMessage(String message) {
        return info("ℹ️  " + message);
    }

    /**
     * Format progress message with spinner
     */
    public String progressMessage(String message) {
        return warning("⏳ " + message);
    }

    /**
     * Format header with pirate flag
     */
    public String header(String text) {
        return bold(highlight("🏴‍☠️ " + text));
    }

    /**
     * Format section header
     */
    public String section(String text) {
        return "\n" + bold(text) + "\n" + "─".repeat(text.length());
    }

    /**
     * Create a horizontal separator
     */
    public String separator() {
        return muted("────────────────────────────────────────");
    }

    /**
     * Format a menu option
     */
    public String menuOption(String icon, String text, boolean selected) {
        if (selected) {
            return highlight("❯ " + icon + " " + bold(text));
        } else {
        return "  " + icon + " " + text;
        }
    }

    /**
     * Clear the current line
     */
    public void clearLine() {
        System.out.print(Ansi.ansi().eraseLine().cursorToColumn(0));
    }

    /**
     * Move cursor up n lines
     */
    public void cursorUp(int lines) {
        System.out.print(Ansi.ansi().cursorUp(lines));
    }

    /**
     * Print without newline
     */
    public void print(String text) {
        System.out.print(text);
        System.out.flush();
    }

    /**
     * Print with newline
     */
    public void println(String text) {
        System.out.println(text);
    }
}

// Made with Bob
