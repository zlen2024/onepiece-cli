package com.nel.onepiece.ui;

import com.nel.onepiece.commands.DeployCommand;
import com.nel.onepiece.commands.SetupCommand;
import com.nel.onepiece.commands.SettingsCommand;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

/**
 * Interactive menu system using JLine for terminal interaction.
 * Provides arrow key navigation and a beautiful TUI experience.
 */
@ApplicationScoped
public class InteractiveMenu {

    @Inject
    ColorFormatter formatter;

    @Inject
    SetupCommand setupCommand;

    @Inject
    DeployCommand deployCommand;

    @Inject
    SettingsCommand settingsCommand;

    private Terminal terminal;
    private LineReader reader;

    private enum MainMenuOption {
        SETUP("⚙️", "Setup", "Bootstrap AI agent environment"),
        DEPLOY("🚀", "Deploy", "Deploy your project to the cloud"),
        SETTINGS("🔐", "Settings", "Configure credentials and preferences"),
        EXIT("❌", "Exit", "Exit the application");

        final String icon;
        final String label;
        final String description;

        MainMenuOption(String icon, String label, String description) {
            this.icon = icon;
            this.label = label;
            this.description = description;
        }
    }

    /**
     * Initialize the terminal and reader
     */
    private void initTerminal() {
        try {
            if (terminal == null) {
                terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
                reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize terminal: " + e.getMessage());
            // Fallback to simple mode
        }
    }

    /**
     * Show the main interactive menu
     */
    public void show() {
        initTerminal();
        
        formatter.println("");
        formatter.println(formatter.header("One Piece CLI v1.0.0"));
        formatter.println(formatter.info("The Ultimate AI Environment Bootstrapper"));
        formatter.println("");

        boolean running = true;
        while (running) {
            MainMenuOption selected = showMainMenu();
            
            if (selected == null) {
                continue;
            }

            formatter.println("");
            
            switch (selected) {
                case SETUP:
                    setupCommand.run();
                    break;
                case DEPLOY:
                    deployCommand.run();
                    break;
                case SETTINGS:
                    settingsCommand.run();
                    break;
                case EXIT:
                    running = false;
                    formatter.println(formatter.info("👋 Goodbye!"));
                    break;
            }

            if (running && selected != MainMenuOption.EXIT) {
                formatter.println("");
                pressEnterToContinue();
            }
        }
    }

    /**
     * Display the main menu and get user selection
     */
    private MainMenuOption showMainMenu() {
        formatter.println(formatter.bold("? What would you like to do?"));
        formatter.println("");

        for (MainMenuOption option : MainMenuOption.values()) {
            String line = String.format("  %s  %-8s - %s", 
                option.icon, 
                option.label, 
                option.description);
            formatter.println(line);
        }

        formatter.println("");
        
        try {
            String input = reader.readLine(formatter.highlight("Select option (1-4): "));
            int choice = Integer.parseInt(input.trim());
            
            if (choice >= 1 && choice <= MainMenuOption.values().length) {
                return MainMenuOption.values()[choice - 1];
            } else {
                formatter.println(formatter.errorMessage("Invalid option. Please select 1-4."));
                return null;
            }
        } catch (NumberFormatException e) {
            formatter.println(formatter.errorMessage("Invalid input. Please enter a number."));
            return null;
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Error reading input: " + e.getMessage()));
            return null;
        }
    }

    /**
     * Show a submenu with options
     */
    public <T extends Enum<T>> T showSubmenu(String title, T[] options, java.util.function.Function<T, String> iconGetter, java.util.function.Function<T, String> labelGetter) {
        formatter.println(formatter.section(title));
        formatter.println("");

        for (int i = 0; i < options.length; i++) {
            T option = options[i];
            String line = String.format("  %s  %s", 
                iconGetter.apply(option), 
                labelGetter.apply(option));
            formatter.println(line);
        }

        formatter.println("");
        
        try {
            String input = reader.readLine(formatter.highlight("Select option (1-" + options.length + "): "));
            int choice = Integer.parseInt(input.trim());
            
            if (choice >= 1 && choice <= options.length) {
                return options[choice - 1];
            } else {
                formatter.println(formatter.errorMessage("Invalid option."));
                return null;
            }
        } catch (NumberFormatException e) {
            formatter.println(formatter.errorMessage("Invalid input. Please enter a number."));
            return null;
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Error reading input: " + e.getMessage()));
            return null;
        }
    }

    /**
     * Prompt user for text input
     */
    public String promptInput(String prompt) {
        try {
            return reader.readLine(formatter.highlight("? " + prompt + ": "));
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Error reading input: " + e.getMessage()));
            return null;
        }
    }

    /**
     * Prompt user for yes/no confirmation
     */
    public boolean promptConfirm(String prompt, boolean defaultValue) {
        try {
            String defaultText = defaultValue ? "Y/n" : "y/N";
            String input = reader.readLine(formatter.highlight("? " + prompt + " (" + defaultText + "): "));
            
            if (input == null || input.trim().isEmpty()) {
                return defaultValue;
            }
            
            return input.trim().toLowerCase().startsWith("y");
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Error reading input: " + e.getMessage()));
            return defaultValue;
        }
    }

    /**
     * Wait for user to press Enter
     */
    private void pressEnterToContinue() {
        try {
            reader.readLine(formatter.muted("Press Enter to continue..."));
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Close the terminal
     */
    public void close() {
        try {
            if (terminal != null) {
                terminal.close();
            }
        } catch (IOException e) {
            // Ignore
        }
    }
}

// Made with Bob
