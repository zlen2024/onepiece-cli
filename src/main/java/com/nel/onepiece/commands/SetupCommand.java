package com.nel.onepiece.commands;

import com.nel.onepiece.ai.ProjectAnalyzerService;
import com.nel.onepiece.config.ConfigurationGenerator;
import com.nel.onepiece.model.ProjectAnalysis;
import com.nel.onepiece.ui.ColorFormatter;
import com.nel.onepiece.ui.InteractiveMenu;
import com.nel.onepiece.ui.ProgressIndicator;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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

    @Inject
    ProjectAnalyzerService projectAnalyzerService;

    @Inject
    ConfigurationGenerator configurationGenerator;

    @Option(
        names = {"--agent"},
        description = "AI agent to configure: ${COMPLETION-CANDIDATES}"
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
            if (input == null) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }
            
            try {
                int choice = Integer.parseInt(input.trim());
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

        Path projectPath = Paths.get(projectDir).toAbsolutePath().normalize();
        if (!Files.exists(projectPath) || !Files.isDirectory(projectPath)) {
            formatter.println(formatter.errorMessage("Project directory not found: " + projectPath));
            return;
        }

        progress.startSpinner("Analyzing project directory");
        ProjectAnalysis analysis;
        try {
            analysis = projectAnalyzerService.analyzeProject(projectPath.toString());
            progress.success("Project structure analyzed");
        } catch (Exception e) {
            progress.error("Project analysis failed: " + e.getMessage());
            return;
        }

        formatter.println("");
        formatter.println(formatter.info("   📁 Project: " + projectPath));
        formatter.println(formatter.info("   🔤 Language: " + analysis.getLanguage()));
        formatter.println(formatter.info("   🧩 Framework: " + analysis.getFramework()));
        formatter.println(formatter.info("   📦 Build tool: " + analysis.getBuildTool()));
        if (analysis.getRecommendedMcps() != null && !analysis.getRecommendedMcps().isEmpty()) {
            formatter.println(formatter.info("   🎯 Recommended MCPs: " + String.join(", ", analysis.getRecommendedMcps())));
        }
        formatter.println("");

        String workflow = "agile";
        if (!nonInteractive) {
            formatter.println(formatter.bold("? Select project workflow:"));
            formatter.println("");
            formatter.println("  1. Agile");
            formatter.println("  2. Spiral");
            formatter.println("  3. Waterfall");
            formatter.println("  4. Other");
            formatter.println("");
            String input = menu.promptInput("Select option (1-4)");
            if (input != null) {
                switch (input.trim()) {
                    case "1" -> workflow = "agile";
                    case "2" -> workflow = "spiral";
                    case "3" -> workflow = "waterfall";
                    case "4" -> {
                        String other = menu.promptInput("Enter workflow name");
                        if (other != null && !other.trim().isEmpty()) {
                            workflow = other.trim();
                        }
                    }
                }
            }
            formatter.println("");
        }

        progress.startSpinner("Generating configuration");
        List<String> skills;
        String systemPrompt;
        try {
            systemPrompt = projectAnalyzerService.generateSystemPrompt(analysis, agent.label);
            skills = new ArrayList<>(projectAnalyzerService.recommendSkills(analysis));
            LinkedHashSet<String> normalized = new LinkedHashSet<>(skills);
            normalized.add("bug-hunter");
            normalized.add("context7-auto-research");
            skills = new ArrayList<>(normalized);
            progress.success("Configuration generated");
        } catch (Exception e) {
            progress.error("Configuration generation failed: " + e.getMessage());
            return;
        }

        List<String> selectedSkills = skills;
        if (!nonInteractive) {
            formatter.println("");
            formatter.println(formatter.bold("? Skills selection:"));
            formatter.println(formatter.muted("Recommended: " + String.join(", ", skills)));
            boolean useRecommended = menu.promptConfirm("Use recommended skills?", true);
            if (!useRecommended) {
                String custom = menu.promptInput("Enter skills (comma-separated)");
                if (custom != null && !custom.trim().isEmpty()) {
                    List<String> parsed = new ArrayList<>();
                    for (String part : custom.split(",")) {
                        String s = part.trim();
                        if (!s.isEmpty()) {
                            parsed.add(s);
                        }
                    }
                    LinkedHashSet<String> normalized = new LinkedHashSet<>(parsed);
                    normalized.add("bug-hunter");
                    normalized.add("context7-auto-research");
                    selectedSkills = new ArrayList<>(normalized);
                }
            }
            formatter.println("");
        }

        List<String> selectedMcps = analysis.getRecommendedMcps() != null ? new ArrayList<>(analysis.getRecommendedMcps()) : List.of("filesystem-mcp");
        Map<String, String> envVarNames = new HashMap<>();
        if (!nonInteractive) {
            formatter.println(formatter.bold("? MCP selection:"));
            formatter.println(formatter.muted("Recommended: " + String.join(", ", selectedMcps)));
            boolean useRecommended = menu.promptConfirm("Use recommended MCP servers?", true);
            if (!useRecommended) {
                String custom = menu.promptInput("Enter MCP server names (comma-separated)");
                if (custom != null && !custom.trim().isEmpty()) {
                    List<String> parsed = new ArrayList<>();
                    for (String part : custom.split(",")) {
                        String s = part.trim();
                        if (!s.isEmpty()) {
                            parsed.add(s);
                        }
                    }
                    if (!parsed.contains("filesystem-mcp")) {
                        parsed.add(0, "filesystem-mcp");
                    }
                    selectedMcps = parsed;
                }
            }

            if (selectedMcps.contains("github-mcp")) {
                String v = menu.promptInput("Env var name for GitHub token (default: GITHUB_TOKEN)");
                if (v != null && !v.trim().isEmpty()) {
                    envVarNames.put("GITHUB_PERSONAL_ACCESS_TOKEN", v.trim());
                }
            }
            if (selectedMcps.contains("postgres-mcp")) {
                String v = menu.promptInput("Env var name for Postgres connection (default: DATABASE_URL)");
                if (v != null && !v.trim().isEmpty()) {
                    envVarNames.put("POSTGRES_CONNECTION_STRING", v.trim());
                }
            }
            formatter.println("");
        }

        boolean wroteBobConfig = false;
        try {
            ProjectAnalysis analysisForConfig = analysis;
            analysisForConfig.setRecommendedMcps(selectedMcps);

            if (agent == AgentType.BOB) {
                configurationGenerator.generateBobWorkspace(projectPath.toString(), analysisForConfig, systemPrompt, selectedSkills);
                configurationGenerator.generateBobProjectMcpConfig(projectPath.toString(), selectedMcps, envVarNames);
                configurationGenerator.generateBobCustomModes(projectPath.toString(), workflow);
                configurationGenerator.generateBobRules(projectPath.toString(), workflow, selectedSkills, selectedMcps);
                wroteBobConfig = true;
            }
            configurationGenerator.generateMcpRegistry(projectPath.toString(), analysisForConfig);
            configurationGenerator.generateProjectMetadata(projectPath.toString(), analysisForConfig, agent.name().toLowerCase());
            configurationGenerator.generateEnvExample(projectPath.toString(), analysisForConfig);
        } catch (Exception e) {
            formatter.println("");
            formatter.println(formatter.errorMessage("Failed to write configuration files: " + e.getMessage()));
            return;
        }

        // Step 4: Show completion
        formatter.println(formatter.success("✅ Setup Complete!"));
        formatter.println("");
        
        formatter.println(formatter.bold("📝 Generated files:"));
        if (wroteBobConfig) {
            formatter.println(formatter.successMessage("   • .bob.workspace (IBM Bob configuration)"));
            formatter.println(formatter.successMessage("   • .bob/mcp.json (Bob MCP servers)"));
            formatter.println(formatter.successMessage("   • .bob/custom_modes.yaml (Bob custom modes)"));
            formatter.println(formatter.successMessage("   • .bob/rules/01-workspace.md (Bob custom rules)"));
        }
        formatter.println(formatter.successMessage("   • .onepiece/mcp-registry.json (MCP server list)"));
        formatter.println(formatter.successMessage("   • .onepiece/project.json (project metadata)"));
        formatter.println(formatter.successMessage("   • .env.example (environment template)"));
        formatter.println("");
        
        formatter.println(formatter.bold("🚀 Next steps:"));
        formatter.println("   1. Review the generated configuration");
        if (wroteBobConfig) {
            formatter.println("   2. Run your AI agent: bob start");
        } else {
            formatter.println("   2. Run your AI agent");
        }
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
