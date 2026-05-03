package com.nel.onepiece;

import com.nel.onepiece.commands.DeployCommand;
import com.nel.onepiece.commands.SetupCommand;
import com.nel.onepiece.commands.SettingsCommand;
import com.nel.onepiece.ui.InteractiveMenu;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 🏴‍☠️ One Piece CLI - The Ultimate AI Environment Bootstrapper
 * 
 * Main entry point for the CLI application. Supports both interactive
 * and non-interactive modes.
 */
@TopCommand
@Command(
    name = "onepiece",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = {
        "",
        "🏴‍☠️ One Piece CLI v1.0.0",
        "The Ultimate AI Environment Bootstrapper",
        "",
        "Bootstrap AI agent environments and automate cloud deployments.",
        ""
    },
    subcommands = {
        SetupCommand.class,
        DeployCommand.class,
        SettingsCommand.class,
        CommandLine.HelpCommand.class
    }
)
public class OnePieceCommand implements Runnable {

    @Inject
    InteractiveMenu interactiveMenu;

    @Option(
        names = {"-i", "--interactive"},
        description = "Launch interactive menu (default when no command specified)"
    )
    boolean interactive;

    @Option(
        names = {"-v", "--verbose"},
        description = "Enable verbose output"
    )
    boolean verbose;

    @Override
    public void run() {
        // If no subcommand is specified, launch interactive mode
        if (interactiveMenu != null) {
            interactiveMenu.show();
        } else {
            System.err.println("❌ Error: Interactive menu not available");
            System.err.println("Please use a specific command: setup, deploy, or settings");
        }
    }

    /**
     * Main method for native compilation
     */
    public static void main(String[] args) {
        // This is handled by Quarkus Picocli integration
    }
}

// Made with Bob
