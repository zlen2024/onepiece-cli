package com.nel.onepiece.commands;

import com.nel.onepiece.ui.ColorFormatter;
import com.nel.onepiece.ui.InteractiveMenu;
import com.nel.onepiece.ui.ProgressIndicator;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Setup command - Bootstrap AI agent environment
 * Analyzes the project and generates configuration files for the selected AI agent.
 */
@Command(
    name = "setup",
    description = "Bootstrap AI agent environment",
    mixinStandardHelpOptions = true
)
public class SetupCommand implements Runnable {

    @Inject
    ColorFormatter formatter;

    @Inject
    InteractiveMenu menu;

    @Inject
    ProgressIndicator progress;

    @Option(
        names = {"--agent"},
        description = "AI agent to configure: ${COMPLETION-CANDIDATES}",
        defaultValue = "bob"
    )
    AgentType agent;

    @Option(
        names = {"--project-dir"},
        description = "Project directory (default: current directory)",
        defaultValue = "."
    )
    String projectDir;

    @Option(
        names = {"--auto-detect"},
        description = "Auto-detect project settings",
        defaultValue = "true"
    )
    boolean autoDetect;

    @Option(
        names = {"--no-interactive"},
        description = "Skip interactive prompts"
    )
    boolean nonInteractive;

    public enum AgentType {
        BOB("🤖", "IBM Bob"),
        CLAUDECODE("🔮", "Claude Code"),
        OPENCODE("🌟", "Open Code"),
        PI("🥧", "Pi");

        final String icon;
        final String label;

        AgentType(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }

        public String getIcon() {
            return icon;
        }

        public String getLabel() {
            return label;
        }
    }

    @Override
    public void run() {
        formatter.println("");
        formatter.println(formatter.section("⚙️  Setup - Bootstrap AI Agent Environment"));
        formatter.println("");

        // If interactive mode, show agent selection
        if (!nonInteractive && agent == null) {
            formatter.println(formatter.bold("? Select your AI agent:"));
            formatter.println("");
            
            for (int i = 0; i < AgentType.values().length; i++) {
                AgentType type = AgentType.values()[i];
                String recommended = (type == AgentType.BOB) ? " (Recommended for POC)" : "";
                formatter.println(String.format("  %d. %s %s%s", 
                    i + 1, type.icon, type.label, recommended));
            }
            
            formatter.println("");
            String input = menu.promptInput("Select option (1-" + AgentType.values().length + ")");
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= AgentType.values().length) {
                    agent = AgentType.values()[choice - 1];
                } else {
                    formatter.println(formatter.errorMessage("Invalid selection"));
                    return;
                }
            } catch (NumberFormatException e) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }
        }

        // Default to BOB if not specified
        if (agent == null) {
            agent = AgentType.BOB;
        }

        formatter.println("");
        formatter.println(formatter.info("Selected agent: " + agent.icon + " " + agent.label));
        formatter.println("");

        // Step 1: Analyze project
        progress.startSpinner("Analyzing project directory");
        
        try {
            Thread.sleep(2000); // Simulate analysis
            progress.success("Project structure analyzed");
            
            // Show detected information
            formatter.println(formatter.info("   📁 Detected: Java Quarkus project"));
            formatter.println(formatter.info("   📦 Found: pom.xml, src/main/java"));
            formatter.println(formatter.info("   🎯 Recommended MCPs: filesystem, github, maven"));
            formatter.println("");
            
        } catch (InterruptedException e) {
            progress.error("Analysis interrupted");
            return;
        }

        // Step 2: Generate configuration
        progress.startSpinner("Generating configuration with AI (this may take 10-20 seconds)");
        
        try {
            Thread.sleep(3000); // Simulate AI generation
            progress.success("Configuration generated");
            formatter.println("");
            
        } catch (InterruptedException e) {
            progress.error("Configuration generation interrupted");
            return;
        }

        // Step 3: Register MCPs
        String[] mcpSteps = {
            "filesystem-mcp (v1.2.0)",
            "github-mcp (v2.0.1)",
            "maven-mcp (v1.5.3)"
        };

        formatter.println(formatter.bold("Registering MCP servers..."));
        for (int i = 0; i < mcpSteps.length; i++) {
            progress.loading("   ├─ " + mcpSteps[i]);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
            formatter.clearLine();
            formatter.println(formatter.successMessage("   ├─ " + mcpSteps[i]));
        }
        formatter.println("");

        // Step 4: Show completion
        formatter.println(formatter.success("✅ Setup Complete!"));
        formatter.println("");
        
        formatter.println(formatter.bold("📝 Generated files:"));
        formatter.println(formatter.successMessage("   • .bob.workspace (AI agent configuration)"));
        formatter.println(formatter.successMessage("   • .onepiece/mcp-registry.json (MCP server list)"));
        formatter.println("");
        
        formatter.println(formatter.bold("🚀 Next steps:"));
        formatter.println("   1. Review the generated configuration");
        formatter.println("   2. Run your AI agent: bob start");
        formatter.println("   3. When ready to deploy: onepiece deploy");
        formatter.println("");

        // Ask if user wants to start the agent
        if (!nonInteractive) {
            boolean startAgent = menu.promptConfirm("Would you like to start " + agent.label + " now?", true);
            if (startAgent) {
                formatter.println(formatter.info("Starting " + agent.label + "..."));
                formatter.println(formatter.muted("(This would launch the agent in a real implementation)"));
            }
        }
    }
}

// Made with Bob
