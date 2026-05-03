package com.nel.onepiece.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nel.onepiece.ai.AgentPresetBuilderAIImpl;
import com.nel.onepiece.ai.ProjectAnalyzerService;
import com.nel.onepiece.ai.AIProviderService;
import com.nel.onepiece.config.PlaceholderRenderer;
import com.nel.onepiece.config.PresetLibraryManager;
import com.nel.onepiece.config.ConfigurationGenerator;
import com.nel.onepiece.model.ProjectAnalysis;
import com.nel.onepiece.model.presets.AgentPreset;
import com.nel.onepiece.model.presets.McpPreset;
import com.nel.onepiece.model.presets.PresetLibrary;
import com.nel.onepiece.model.presets.SkillPreset;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    AIProviderService aiProviderService;

    @Inject
    PresetLibraryManager presetLibraryManager;

    @Inject
    AgentPresetBuilderAIImpl agentPresetBuilderAI;

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

        String effectiveProjectDir = projectDir;
        if (effectiveProjectDir == null || effectiveProjectDir.isBlank()) {
            effectiveProjectDir = ".";
        }

        Path projectPath = Paths.get(effectiveProjectDir).toAbsolutePath().normalize();
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

        if (!nonInteractive && agent == AgentType.BOB) {
            runBobWizard(projectPath, analysis);
            return;
        }

        boolean advanced = false;
        if (!nonInteractive) {
            boolean useRecommended = menu.promptConfirm("Use recommended settings (fast setup)?", true);
            advanced = !useRecommended;
            formatter.println("");
        }

        if (!aiProviderService.isConfigured()) {
            formatter.println(formatter.warning("⚠️  AI provider is not configured. Setup will use fallback defaults."));
            formatter.println(formatter.muted("Run: onepiece settings"));
            formatter.println("");
        }

        String workflow = "agile";
        if (!nonInteractive && advanced) {
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
        } catch (Throwable t) {
            progress.success("Configuration generated");
            formatter.println(formatter.warning("⚠️  AI generation failed; using fallback defaults."));
            formatter.println(formatter.muted(t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName()));
            systemPrompt = String.format(
                "You are %s, an AI coding assistant working on a %s project.",
                agent.label,
                analysis.getFramework()
            );
            skills = List.of("bug-hunter", "context7-auto-research");
        }

        List<String> selectedSkills = skills;
        if (!nonInteractive && advanced) {
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
        Map<String, Map<String, Object>> bobMcpServers = new LinkedHashMap<>();
        if (!nonInteractive && advanced) {
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

        for (String mcp : selectedMcps) {
            Map<String, Object> server = new LinkedHashMap<>();
            server.put("disabled", false);

            if (!nonInteractive && advanced) {
                formatter.println(formatter.bold("? Transport for " + mcp + ":"));
                formatter.println("  1. STDIO (local)");
                formatter.println("  2. Remote (HTTP)");
                formatter.println("");
                String t = menu.promptInput("Select option (1-2)");
                boolean remote = "2".equals(t != null ? t.trim() : "");

                if (remote) {
                    String url = menu.promptInput("Enter MCP server URL (example: https://host/mcp)");
                    if (url == null || url.trim().isEmpty()) {
                        formatter.println(formatter.errorMessage("URL is required for remote MCP server"));
                        return;
                    }
                    server.put("url", url.trim());

                    boolean addHeaders = menu.promptConfirm("Add HTTP headers?", false);
                    if (addHeaders) {
                        Map<String, String> headers = new LinkedHashMap<>();
                        while (true) {
                            String headerName = menu.promptInput("Header name (press Enter to finish)");
                            if (headerName == null || headerName.trim().isEmpty()) {
                                break;
                            }
                            String headerValue = menu.promptInput("Header value (use ${ENV_VAR} for env placeholders)");
                            if (headerValue != null && !headerValue.trim().isEmpty()) {
                                headers.put(headerName.trim(), headerValue.trim());
                            }
                        }
                        if (!headers.isEmpty()) {
                            server.put("headers", headers);
                        }
                    }
                } else {
                    server.put("cwd", projectPath.toString());

                    switch (mcp) {
                        case "filesystem-mcp" -> {
                            server.put("command", "npx");
                            server.put("args", List.of("-y", "@modelcontextprotocol/server-filesystem", projectPath.toString()));
                        }
                        case "github-mcp" -> {
                            server.put("command", "npx");
                            server.put("args", List.of("-y", "@modelcontextprotocol/server-github"));
                            server.put("env", Map.of(
                                "GITHUB_PERSONAL_ACCESS_TOKEN",
                                "${" + envVarNames.getOrDefault("GITHUB_PERSONAL_ACCESS_TOKEN", "GITHUB_TOKEN") + "}"
                            ));
                        }
                        case "maven-mcp" -> {
                            server.put("command", "npx");
                            server.put("args", List.of("-y", "@modelcontextprotocol/server-maven", projectPath.toString()));
                        }
                        case "gradle-mcp" -> {
                            server.put("command", "npx");
                            server.put("args", List.of("-y", "@modelcontextprotocol/server-gradle", projectPath.toString()));
                        }
                        case "npm-mcp" -> {
                            server.put("command", "npx");
                            server.put("args", List.of("-y", "@modelcontextprotocol/server-npm", projectPath.toString()));
                        }
                        case "postgres-mcp" -> {
                            server.put("command", "npx");
                            server.put("args", List.of("-y", "@modelcontextprotocol/server-postgres"));
                            server.put("env", Map.of(
                                "POSTGRES_CONNECTION_STRING",
                                "${" + envVarNames.getOrDefault("POSTGRES_CONNECTION_STRING", "DATABASE_URL") + "}"
                            ));
                        }
                        case "docker-mcp" -> {
                            server.put("command", "npx");
                            server.put("args", List.of("-y", "@modelcontextprotocol/server-docker"));
                        }
                        default -> {
                            server.put("command", "npx");
                            server.put("args", List.of("-y", mcp));
                        }
                    }

                    String allow = menu.promptInput("Always allow tools (comma-separated, optional)");
                    if (allow != null && !allow.trim().isEmpty()) {
                        List<String> tools = new ArrayList<>();
                        for (String part : allow.split(",")) {
                            String s = part.trim();
                            if (!s.isEmpty()) {
                                tools.add(s);
                            }
                        }
                        if (!tools.isEmpty()) {
                            server.put("alwaysAllow", tools);
                        }
                    }
                }
                formatter.println("");
            } else {
                server.put("cwd", projectPath.toString());
                switch (mcp) {
                    case "filesystem-mcp" -> {
                        server.put("command", "npx");
                        server.put("args", List.of("-y", "@modelcontextprotocol/server-filesystem", projectPath.toString()));
                    }
                    case "github-mcp" -> {
                        server.put("command", "npx");
                        server.put("args", List.of("-y", "@modelcontextprotocol/server-github"));
                        server.put("env", Map.of(
                            "GITHUB_PERSONAL_ACCESS_TOKEN",
                            "${" + envVarNames.getOrDefault("GITHUB_PERSONAL_ACCESS_TOKEN", "GITHUB_TOKEN") + "}"
                        ));
                    }
                    case "maven-mcp" -> {
                        server.put("command", "npx");
                        server.put("args", List.of("-y", "@modelcontextprotocol/server-maven", projectPath.toString()));
                    }
                    case "gradle-mcp" -> {
                        server.put("command", "npx");
                        server.put("args", List.of("-y", "@modelcontextprotocol/server-gradle", projectPath.toString()));
                    }
                    case "npm-mcp" -> {
                        server.put("command", "npx");
                        server.put("args", List.of("-y", "@modelcontextprotocol/server-npm", projectPath.toString()));
                    }
                    case "postgres-mcp" -> {
                        server.put("command", "npx");
                        server.put("args", List.of("-y", "@modelcontextprotocol/server-postgres"));
                        server.put("env", Map.of(
                            "POSTGRES_CONNECTION_STRING",
                            "${" + envVarNames.getOrDefault("POSTGRES_CONNECTION_STRING", "DATABASE_URL") + "}"
                        ));
                    }
                    case "docker-mcp" -> {
                        server.put("command", "npx");
                        server.put("args", List.of("-y", "@modelcontextprotocol/server-docker"));
                    }
                    default -> {
                        server.put("command", "npx");
                        server.put("args", List.of("-y", mcp));
                    }
                }
            }

            bobMcpServers.put(mcp, server);
        }

        boolean wroteBobConfig = false;
        try {
            ProjectAnalysis analysisForConfig = analysis;
            analysisForConfig.setRecommendedMcps(selectedMcps);

            if (agent == AgentType.BOB) {
                String modelName = "gpt-4";
                double temperature = 0.7;
                int maxTokens = 2000;
                var providerConfig = aiProviderService.getCurrentConfig();
                if (providerConfig != null && providerConfig.isValid()) {
                    if (providerConfig.getModelName() != null && !providerConfig.getModelName().isBlank()) {
                        modelName = providerConfig.getModelName();
                    }
                    temperature = providerConfig.getTemperature();
                    maxTokens = providerConfig.getMaxTokens();
                }

                configurationGenerator.generateBobWorkspace(
                    projectPath.toString(),
                    analysisForConfig,
                    systemPrompt,
                    selectedSkills,
                    modelName,
                    temperature,
                    maxTokens
                );
                configurationGenerator.generateBobProjectMcpConfig(projectPath.toString(), bobMcpServers);
                configurationGenerator.generateBobCustomModes(projectPath.toString(), workflow);
                configurationGenerator.generateBobRules(projectPath.toString(), workflow, selectedSkills, selectedMcps);
                configurationGenerator.generateBobSkills(projectPath.toString(), selectedSkills);
                wroteBobConfig = true;
            }
            configurationGenerator.generateMcpRegistry(projectPath.toString(), analysisForConfig);
            configurationGenerator.generateProjectMetadata(projectPath.toString(), analysisForConfig, agent.name().toLowerCase());
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

    private static class BobSetupState {
        List<AgentPreset> agentPresets = new ArrayList<>();
        AgentPreset primaryAgent;
        List<String> skillSlugs = new ArrayList<>();
        List<String> mcpNames = new ArrayList<>();
        String githubTokenEnv = "GITHUB_TOKEN";
        String databaseUrlEnv = "DATABASE_URL";
        boolean generated = false;
    }

    private void runBobWizard(Path projectPath, ProjectAnalysis analysis) {
        PresetLibrary library = presetLibraryManager.loadOrCreate();

        BobSetupState state = new BobSetupState();
        if (!library.getAgents().isEmpty()) {
            AgentPreset first = library.getAgents().get(0);
            state.agentPresets.add(first);
            state.primaryAgent = first;
        }
        if (library.getSkills().stream().anyMatch(s -> "bug-hunter".equals(s.getSlug()))) {
            state.skillSlugs.add("bug-hunter");
        }
        if (library.getSkills().stream().anyMatch(s -> "context7-auto-research".equals(s.getSlug()))) {
            state.skillSlugs.add("context7-auto-research");
        }
        List<String> recommended = analysis.getRecommendedMcps() != null ? analysis.getRecommendedMcps() : List.of("filesystem-mcp");
        for (String name : recommended) {
            if (library.getMcps().stream().anyMatch(m -> name.equals(m.getName()))) {
                state.mcpNames.add(name);
            }
        }
        if (!state.mcpNames.contains("filesystem-mcp") && library.getMcps().stream().anyMatch(m -> "filesystem-mcp".equals(m.getName()))) {
            state.mcpNames.add(0, "filesystem-mcp");
        }
        if (state.mcpNames.isEmpty() && library.getMcps().stream().anyMatch(m -> "filesystem-mcp".equals(m.getName()))) {
            state.mcpNames.add("filesystem-mcp");
        }

        while (true) {
            formatter.println(formatter.section("🤖 IBM Bob Setup"));
            formatter.println("");

            formatter.println(formatter.bold("Current selection:"));
            formatter.println("   Agents: " + state.agentPresets.size() + (state.primaryAgent != null ? " (primary: " + state.primaryAgent.getDisplayName() + ")" : ""));
            formatter.println("   Skills: " + state.skillSlugs.size());
            formatter.println("   MCP: " + state.mcpNames.size());
            formatter.println("   Generated: " + (state.generated ? "yes" : "no"));
            formatter.println("");

            formatter.println(formatter.bold("? What would you like to configure?"));
            formatter.println("");
            formatter.println("  1. > agent");
            formatter.println("  2. > skills");
            formatter.println("  3. > mcp");
            formatter.println("  4. > generate");
            formatter.println("  5. > start-bob");
            formatter.println("  6. > back");
            formatter.println("");

            String input = menu.promptInput("Select option (1-6)");
            if (input == null) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }

            switch (input.trim()) {
                case "1" -> {
                    selectBobAgentPreset(projectPath, analysis, library, state);
                    library = presetLibraryManager.loadOrCreate();
                }
                case "2" -> selectBobSkills(library, state);
                case "3" -> selectBobMcps(library, state);
                case "4" -> {
                    boolean ok = generateBobFromWizard(projectPath, analysis, library, state);
                    if (ok) {
                        state.generated = true;
                    }
                }
                case "5" -> printStartBobInstructions(projectPath, state);
                case "6" -> {
                    formatter.println("");
                    return;
                }
                default -> formatter.println(formatter.errorMessage("Invalid selection"));
            }

            formatter.println("");
        }
    }

    private void selectBobAgentPreset(Path projectPath, ProjectAnalysis analysis, PresetLibrary library, BobSetupState state) {
        formatter.println(formatter.section("> agent"));
        formatter.println("");

        List<AgentPreset> presets = library.getAgents().stream()
            .filter(a -> a != null && "bob".equalsIgnoreCase(a.getAgentType()))
            .toList();

        formatter.println("  0. [ ] none");
        for (int i = 0; i < presets.size(); i++) {
            AgentPreset p = presets.get(i);
            boolean selected = state.agentPresets.stream().anyMatch(a -> a != null && safe(p.getId()).equals(a.getId()));
            boolean primary = state.primaryAgent != null && safe(p.getId()).equals(state.primaryAgent.getId());
            formatter.println(String.format(
                "  %d. [%s] %s%s - %s",
                i + 1,
                selected ? "x" : " ",
                safe(p.getDisplayName()),
                primary ? " (primary)" : "",
                safe(p.getDescription())
            ));
        }
        formatter.println("");
        formatter.println("  C. Custom agent");
        formatter.println("  B. Back");
        formatter.println("");

        String input = menu.promptInput("Select agents (comma-separated), or C/B");
        if (input == null) {
            formatter.println(formatter.errorMessage("Invalid input"));
            return;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        if ("b".equalsIgnoreCase(trimmed)) {
            return;
        }

        if ("c".equalsIgnoreCase(trimmed)) {
            AgentPreset custom = buildCustomAgent(projectPath, analysis, library);
            if (custom == null) {
                return;
            }

            state.agentPresets.add(custom);
            state.primaryAgent = custom;

            boolean save = menu.promptConfirm("Save this agent to presets library?", true);
            if (save) {
                presetLibraryManager.upsertAgent(library, custom);
                try {
                    presetLibraryManager.save(library);
                    presetLibraryManager.exportTemplates(library);
                } catch (Exception e) {
                    formatter.println(formatter.warningMessage("Failed to save presets: " + e.getMessage()));
                }
            }
            return;
        }

        List<Integer> choices = new ArrayList<>();
        for (String part : trimmed.split(",")) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            try {
                choices.add(Integer.parseInt(p));
            } catch (NumberFormatException ignored) {
            }
        }

        if (choices.contains(0)) {
            state.agentPresets = new ArrayList<>();
            state.primaryAgent = null;
            return;
        }

        List<AgentPreset> selected = new ArrayList<>();
        for (int idx : choices) {
            if (idx < 1 || idx > presets.size()) {
                continue;
            }
            selected.add(presets.get(idx - 1));
        }

        if (selected.isEmpty()) {
            formatter.println(formatter.errorMessage("Select at least one agent, or choose 0 for none"));
            return;
        }

        state.agentPresets = selected;
        state.primaryAgent = selected.get(0);
    }

    private void selectBobSkills(PresetLibrary library, BobSetupState state) {
        formatter.println(formatter.section("> skills"));
        formatter.println("");

        List<SkillPreset> skills = library.getSkills();
        if (skills.isEmpty()) {
            formatter.println(formatter.warningMessage("No skill presets found"));
            return;
        }

        formatter.println("  0. [ ] none");
        for (int i = 0; i < skills.size(); i++) {
            SkillPreset s = skills.get(i);
            boolean enabled = state.skillSlugs.contains(s.getSlug());
            formatter.println(String.format("  %d. [%s] %s (%s)", i + 1, enabled ? "x" : " ", safe(s.getName()), safe(s.getSlug())));
        }

        formatter.println("");
        String input = menu.promptInput("Enter skill numbers (comma-separated), 0 for none, or press Enter to keep");
        if (input == null) {
            formatter.println(formatter.errorMessage("Invalid input"));
            return;
        }
        if (input.trim().isEmpty()) {
            return;
        }

        if (input.trim().equals("0")) {
            state.skillSlugs = new ArrayList<>();
            return;
        }

        List<String> selected = new ArrayList<>();
        for (String part : input.split(",")) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            if (p.equals("0")) {
                state.skillSlugs = new ArrayList<>();
                return;
            }
            try {
                int idx = Integer.parseInt(p);
                if (idx >= 1 && idx <= skills.size()) {
                    String slug = skills.get(idx - 1).getSlug();
                    if (slug != null && !slug.isBlank() && !selected.contains(slug)) {
                        selected.add(slug);
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        state.skillSlugs = selected;
    }

    private void selectBobMcps(PresetLibrary library, BobSetupState state) {
        formatter.println(formatter.section("> mcp"));
        formatter.println("");

        List<McpPreset> mcps = library.getMcps();
        if (mcps.isEmpty()) {
            formatter.println(formatter.warningMessage("No MCP presets found"));
            return;
        }

        formatter.println("  0. [ ] none");
        for (int i = 0; i < mcps.size(); i++) {
            McpPreset m = mcps.get(i);
            boolean enabled = state.mcpNames.contains(m.getName());
            formatter.println(String.format("  %d. [%s] %s", i + 1, enabled ? "x" : " ", safe(m.getName())));
        }

        formatter.println("");
        String input = menu.promptInput("Enter MCP numbers (comma-separated), 0 for none, or press Enter to keep");
        if (input == null) {
            formatter.println(formatter.errorMessage("Invalid input"));
            return;
        }
        if (input.trim().isEmpty()) {
            return;
        }

        if (input.trim().equals("0")) {
            state.mcpNames = new ArrayList<>();
            return;
        }

        List<String> selected = new ArrayList<>();
        for (String part : input.split(",")) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            if (p.equals("0")) {
                state.mcpNames = new ArrayList<>();
                return;
            }
            try {
                int idx = Integer.parseInt(p);
                if (idx >= 1 && idx <= mcps.size()) {
                    String name = mcps.get(idx - 1).getName();
                    if (name != null && !name.isBlank() && !selected.contains(name)) {
                        selected.add(name);
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        state.mcpNames = selected;

        if (state.mcpNames.contains("github-mcp")) {
            String v = menu.promptInput("Env var name for GitHub token (default: " + state.githubTokenEnv + ")");
            if (v != null && !v.trim().isEmpty()) {
                state.githubTokenEnv = v.trim();
            }
        }

        if (state.mcpNames.contains("postgres-mcp")) {
            String v = menu.promptInput("Env var name for Postgres connection (default: " + state.databaseUrlEnv + ")");
            if (v != null && !v.trim().isEmpty()) {
                state.databaseUrlEnv = v.trim();
            }
        }
    }

    private boolean generateBobFromWizard(Path projectPath, ProjectAnalysis analysis, PresetLibrary library, BobSetupState state) {
        if (state.primaryAgent == null || state.agentPresets.isEmpty()) {
            formatter.println(formatter.errorMessage("Select at least one agent first"));
            return false;
        }

        Map<String, Map<String, Object>> bobMcpServers = new LinkedHashMap<>();
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("PROJECT_DIR", projectPath.toString());
        placeholders.put("GITHUB_TOKEN_ENV", state.githubTokenEnv);
        placeholders.put("DATABASE_URL_ENV", state.databaseUrlEnv);
        PlaceholderRenderer renderer = new PlaceholderRenderer(placeholders);

        for (String name : state.mcpNames) {
            Optional<McpPreset> presetOpt = presetLibraryManager.findMcpByName(library, name);
            if (presetOpt.isEmpty()) {
                continue;
            }
            Map<String, Object> rendered = renderer.renderMap(presetOpt.get().getServer());
            bobMcpServers.put(name, rendered);
        }

        List<SkillPreset> skillPresets = new ArrayList<>();
        for (String slug : state.skillSlugs) {
            presetLibraryManager.findSkillBySlug(library, slug).ifPresent(skillPresets::add);
        }

        progress.startSpinner("Generating Bob configuration");

        try {
            ProjectAnalysis analysisForConfig = analysis;
            analysisForConfig.setRecommendedMcps(new ArrayList<>(state.mcpNames));

            String modelName = "gpt-4";
            double temperature = 0.7;
            int maxTokens = 2000;
            var providerConfig = aiProviderService.getCurrentConfig();
            if (providerConfig != null && providerConfig.isValid()) {
                if (providerConfig.getModelName() != null && !providerConfig.getModelName().isBlank()) {
                    modelName = providerConfig.getModelName();
                }
                temperature = providerConfig.getTemperature();
                maxTokens = providerConfig.getMaxTokens();
            }

            configurationGenerator.generateBobWorkspace(
                projectPath.toString(),
                analysisForConfig,
                state.primaryAgent.getSystemPrompt(),
                new ArrayList<>(state.skillSlugs),
                modelName,
                temperature,
                maxTokens
            );
            configurationGenerator.generateBobProjectMcpConfig(projectPath.toString(), bobMcpServers);
            List<AgentPreset.CustomMode> modes = new ArrayList<>();
            for (AgentPreset agent : state.agentPresets) {
                if (agent != null && agent.getCustomMode() != null) {
                    modes.add(agent.getCustomMode());
                }
            }
            configurationGenerator.generateBobCustomModesFromPresets(projectPath.toString(), modes);
            configurationGenerator.generateBobSkillsFromPresets(projectPath.toString(), skillPresets);
            configurationGenerator.generateBobRules(projectPath.toString(), "agile", new ArrayList<>(state.skillSlugs), new ArrayList<>(state.mcpNames));

            configurationGenerator.generateMcpRegistry(projectPath.toString(), analysisForConfig);
            configurationGenerator.generateProjectMetadata(projectPath.toString(), analysisForConfig, "bob");

            progress.success("Bob configuration generated");
        } catch (Exception e) {
            progress.error("Failed to generate configuration: " + e.getMessage());
            return false;
        }

        formatter.println("");
        formatter.println(formatter.success("✅ Bob setup generated!"));
        formatter.println("");

        formatter.println(formatter.bold("📝 Generated files:"));
        formatter.println(formatter.successMessage("   • .bob.workspace"));
        formatter.println(formatter.successMessage("   • .bob/mcp.json"));
        formatter.println(formatter.successMessage("   • .bob/custom_modes.yaml"));
        formatter.println(formatter.successMessage("   • .bob/rules/01-workspace.md"));
        formatter.println(formatter.successMessage("   • .bob/skills/**/SKILL.md (if selected)"));
        formatter.println(formatter.successMessage("   • .onepiece/mcp-registry.json"));
        formatter.println(formatter.successMessage("   • .onepiece/project.json"));
        return true;
    }

    private AgentPreset buildCustomAgent(Path projectPath, ProjectAnalysis analysis, PresetLibrary library) {
        String userRequest = menu.promptInput("Describe your custom agent");
        if (userRequest == null || userRequest.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("Description is required"));
            return null;
        }

        String displayName = null;
        String systemPrompt = null;

        if (aiProviderService.isConfigured()) {
            progress.startSpinner("Building custom agent with AI");
            try {
                String projectContext = String.format(
                    "Project: %s\nLanguage: %s\nFramework: %s\nBuild tool: %s",
                    projectPath,
                    analysis.getLanguage(),
                    analysis.getFramework(),
                    analysis.getBuildTool()
                );
                String json = agentPresetBuilderAI.buildAgent(projectContext, userRequest.trim());
                progress.stopSpinner();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(json);
                displayName = node.hasNonNull("displayName") ? node.get("displayName").asText() : null;
                systemPrompt = node.hasNonNull("systemPrompt") ? node.get("systemPrompt").asText() : null;
            } catch (Exception e) {
                progress.stopSpinner();
                formatter.println(formatter.warningMessage("AI generation failed; please enter values manually."));
            }
        }

        if (displayName == null || displayName.isBlank()) {
            displayName = menu.promptInput("Agent name");
        }
        if (displayName == null || displayName.isBlank()) {
            formatter.println(formatter.errorMessage("Agent name is required"));
            return null;
        }

        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt = menu.promptInput("System prompt");
        }
        if (systemPrompt == null || systemPrompt.isBlank()) {
            formatter.println(formatter.errorMessage("System prompt is required"));
            return null;
        }

        AgentPreset preset = new AgentPreset();
        preset.setAgentType("bob");
        preset.setDisplayName(displayName.trim());
        preset.setDescription("Custom agent");
        preset.setSystemPrompt(systemPrompt.trim());

        String baseId = slugify(displayName);
        String id = baseId;
        int suffix = 2;
        while (true) {
            final String candidate = id;
            boolean exists = library.getAgents().stream().anyMatch(a -> a != null && candidate.equals(a.getId()));
            if (!exists) {
                break;
            }
            id = baseId + "-" + suffix;
            suffix++;
        }
        preset.setId(id);

        AgentPreset.CustomMode mode = new AgentPreset.CustomMode();
        mode.setSlug(id);
        mode.setName(displayName.trim());
        mode.setRoleDefinition("Custom agent");
        mode.setWhenToUse("Use when you want this custom behavior.");
        mode.setCustomInstructions(systemPrompt.trim());
        mode.setGroups(List.of("read", "edit", "command", "mcp"));
        preset.setCustomMode(mode);

        return preset;
    }

    private void printStartBobInstructions(Path projectPath, BobSetupState state) {
        if (!state.generated) {
            formatter.println(formatter.warningMessage("Generate configuration first"));
            return;
        }

        formatter.println(formatter.section("> start-bob"));
        formatter.println("");
        formatter.println("Next, run:");
        formatter.println(formatter.muted("  cd " + projectPath));
        formatter.println(formatter.muted("  bob start"));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String slugify(String input) {
        if (input == null) {
            return "custom-agent";
        }
        String s = input.trim().toLowerCase();
        s = s.replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("-{2,}", "-");
        s = s.replaceAll("^-+", "").replaceAll("-+$", "");
        return s.isEmpty() ? "custom-agent" : s;
    }
}

// Made with Bob
