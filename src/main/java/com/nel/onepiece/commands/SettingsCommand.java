package com.nel.onepiece.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nel.onepiece.config.ConfigManager;
import com.nel.onepiece.config.PresetLibraryManager;
import com.nel.onepiece.model.config.AIProviderConfig;
import com.nel.onepiece.model.config.AIProviderType;
import com.nel.onepiece.model.config.IbmCloudConfig;
import com.nel.onepiece.model.config.VaultConfig;
import com.nel.onepiece.model.presets.AgentPreset;
import com.nel.onepiece.model.presets.McpPreset;
import com.nel.onepiece.model.presets.PresetLibrary;
import com.nel.onepiece.model.presets.SkillPreset;
import com.nel.onepiece.security.VaultClient;
import com.nel.onepiece.ui.ColorFormatter;
import com.nel.onepiece.ui.InteractiveMenu;
import com.nel.onepiece.ui.ProgressIndicator;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Settings command - Configure credentials and preferences
 * Manages HashiCorp Vault configuration and user preferences.
 */
@Command(
    name = "settings",
    description = "Configure credentials and preferences",
    mixinStandardHelpOptions = true
)
public class SettingsCommand implements Runnable {

    @Inject
    ColorFormatter formatter;

    @Inject
    InteractiveMenu menu;

    @Inject
    ProgressIndicator progress;

    @Inject
    ConfigManager configManager;

    @Inject
    PresetLibraryManager presetLibraryManager;

    @Inject
    VaultClient vaultClient;

    @Option(
        names = {"--vault-url"},
        description = "HashiCorp Vault URL"
    )
    String vaultUrl;

    @Option(
        names = {"--vault-token"},
        description = "HashiCorp Vault token"
    )
    String vaultToken;

    @Option(
        names = {"--show"},
        description = "Show current configuration"
    )
    boolean show;

    @Option(
        names = {"--reset"},
        description = "Reset configuration"
    )
    boolean reset;

    @Option(
        names = {"--no-interactive"},
        description = "Skip interactive prompts"
    )
    boolean nonInteractive;

    @Option(
        names = {"--ibmcloud-api-key"},
        description = "IBM Cloud API key (stored in ~/.onepiece/config.json)"
    )
    String ibmCloudApiKey;

    @Option(
        names = {"--ibmcloud-region"},
        description = "Default IBM Cloud region (stored in ~/.onepiece/config.json)"
    )
    String ibmCloudRegion;

    @Option(
        names = {"--ibmcloud-org"},
        description = "Default IBM Cloud Cloud Foundry org (stored in ~/.onepiece/config.json)"
    )
    String ibmCloudOrg;

    @Option(
        names = {"--ibmcloud-space"},
        description = "Default IBM Cloud Cloud Foundry space (stored in ~/.onepiece/config.json)"
    )
    String ibmCloudSpace;

    @Option(
        names = {"--ibmcloud-resource-group"},
        description = "Default IBM Cloud resource group (stored in ~/.onepiece/config.json)"
    )
    String ibmCloudResourceGroup;

    @Option(
        names = {"--ibmcloud-ce-project"},
        description = "Default IBM Cloud Code Engine project (stored in ~/.onepiece/config.json)"
    )
    String ibmCloudCodeEngineProject;

    private enum SettingsMenuOption {
        AI_PROVIDER("🤖", "AI Provider Configuration"),
        DEPLOYMENT_CONFIG("☁️", "Deployment Config"),
        PRESETS("📚", "Presets Library"),
        UPDATE("🔄", "Update Vault configuration"),
        TEST("🧪", "Test connection"),
        SHOW("📋", "Show stored secrets (masked)"),
        RESET("🗑️", "Reset configuration"),
        BACK("🔙", "Back to main menu");

        final String icon;
        final String label;

        SettingsMenuOption(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }
    }

    private enum AIProviderMenuOption {
        CHANGE("🔄", "Change Provider"),
        UPDATE("⚙️", "Update Current Configuration"),
        SHOW("📋", "Show Configuration"),
        BACK("🔙", "Back");

        final String icon;
        final String label;

        AIProviderMenuOption(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }
    }

    private enum DeploymentConfigMenuOption {
        IBM_CLOUD("☁️", "IBM Cloud (Code Engine)"),
        FLYIO("🪰", "Fly.io (Coming soon)"),
        BACK("🔙", "Back");

        final String icon;
        final String label;

        DeploymentConfigMenuOption(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }
    }

    @Override
    public void run() {
        formatter.println("");
        formatter.println(formatter.section("🔐 Settings - Configure Credentials"));
        formatter.println("");

        // Handle non-interactive flags
        if (show) {
            showConfiguration();
            return;
        }

        if (reset) {
            resetConfiguration();
            return;
        }

        if (vaultUrl != null && vaultToken != null) {
            configureVault(vaultUrl, vaultToken);
            return;
        }

        if (ibmCloudApiKey != null) {
            configureIbmCloud(
                ibmCloudApiKey,
                ibmCloudRegion,
                ibmCloudOrg,
                ibmCloudSpace,
                ibmCloudResourceGroup,
                ibmCloudCodeEngineProject
            );
            return;
        }

        // Always show the settings menu in interactive mode
        // Users can choose what to configure from the menu
        showSettingsMenu();
    }

    private boolean checkVaultConfiguration() {
        return configManager.hasVault();
    }

    private void setupVault() {
        String url = menu.promptInput("Enter your Vault URL");
        if (url == null || url.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("Vault URL is required"));
            return;
        }

        String token = menu.promptInput("Enter your Vault token");
        if (token == null || token.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("Vault token is required"));
            return;
        }

        formatter.println("");
        configureVault(url, token);
    }

    private void configureVault(String url, String token) {
        progress.startSpinner("Validating connection");
        String normalizedUrl = url.trim();
        String normalizedToken = token.trim();

        boolean reachable = vaultClient.testConnection(normalizedUrl, normalizedToken);
        if (!reachable) {
            progress.error("Vault not reachable");
            return;
        }

        boolean tokenValid = vaultClient.isTokenValid(normalizedUrl, normalizedToken);
        if (!tokenValid) {
            progress.error("Token is not valid");
            return;
        }

        progress.stopSpinner();

        progress.loading("💾 Saving configuration to ~/.onepiece/config.json");
        try {
            configManager.updateVaultConfig(new VaultConfig(normalizedUrl, normalizedToken));
        } catch (Exception e) {
            formatter.println("");
            formatter.println(formatter.errorMessage("Failed to save configuration: " + e.getMessage()));
            formatter.println("");
            return;
        }
        
        formatter.println("");
        formatter.println(formatter.success("✅ Vault configured successfully!"));
        formatter.println("");

        if (!nonInteractive) {
            boolean testRetrieval = menu.promptConfirm("Would you like to test credential retrieval?", true);
            if (testRetrieval) {
                testConnection();
            }
        }
    }

    private void showSettingsMenu() {
        formatter.println(formatter.bold("Current Configuration:"));
        
        // Show AI Provider status
        AIProviderConfig aiConfig = configManager.getAIProviderConfig();
        if (aiConfig != null && aiConfig.isValid()) {
            formatter.println(formatter.info("   AI Provider: " + aiConfig.getType().getDisplayName() + " (" + aiConfig.getModelName() + ")"));
            formatter.println(formatter.successMessage("   Status: ✓ Configured"));
        } else {
            formatter.println(formatter.warningMessage("   AI Provider: Not configured"));
        }
        
        // Show Vault status
        if (configManager.hasVault()) {
            formatter.println(formatter.info("   Vault: Configured"));
        } else {
            formatter.println(formatter.muted("   Vault: Not configured"));
        }

        // Show IBM Cloud status
        IbmCloudConfig ibmCloud = configManager.getIbmCloudConfig();
        if (ibmCloud != null && ibmCloud.isConfigured()) {
            String regionInfo = ibmCloud.getRegion() != null && !ibmCloud.getRegion().isBlank()
                ? " (" + ibmCloud.getRegion().trim() + ")"
                : "";
            formatter.println(formatter.info("   Deployment (IBM Cloud): Configured" + regionInfo));
        } else {
            formatter.println(formatter.muted("   Deployment (IBM Cloud): Not configured"));
        }
        formatter.println(formatter.muted("   Deployment (Fly.io): Not configured"));
        formatter.println("");

        if (nonInteractive) {
            return;
        }

        formatter.println(formatter.bold("? What would you like to do?"));
        formatter.println("");

        for (int i = 0; i < SettingsMenuOption.values().length; i++) {
            SettingsMenuOption option = SettingsMenuOption.values()[i];
            formatter.println(String.format("  %d. %s %s", i + 1, option.icon, option.label));
        }

        formatter.println("");
        String input = menu.promptInput("Select option (1-" + SettingsMenuOption.values().length + ")");
        if (input == null) {
            formatter.println(formatter.errorMessage("Invalid input"));
            return;
        }

        try {
            int choice = Integer.parseInt(input.trim());
            if (choice >= 1 && choice <= SettingsMenuOption.values().length) {
                SettingsMenuOption selected = SettingsMenuOption.values()[choice - 1];
                formatter.println("");
                handleSettingsOption(selected);
            }
        } catch (NumberFormatException e) {
            formatter.println(formatter.errorMessage("Invalid input"));
        }
    }

    private void handleSettingsOption(SettingsMenuOption option) {
        switch (option) {
            case AI_PROVIDER:
                showAIProviderMenu();
                break;
            case DEPLOYMENT_CONFIG:
                showDeploymentConfigMenu();
                break;
            case PRESETS:
                showPresetsLibraryMenu();
                break;
            case UPDATE:
                setupVault();
                break;
            case TEST:
                testConnection();
                break;
            case SHOW:
                showConfiguration();
                break;
            case RESET:
                resetConfiguration();
                break;
            case BACK:
                // Return to main menu
                break;
        }
    }

    private enum PresetsMenuOption {
        AGENTS("🧩", "Agents"),
        SKILLS("🛠️", "Skills"),
        MCP("🔌", "MCP Servers"),
        EXPORT("📤", "Export templates"),
        IMPORT("📥", "Import templates"),
        BACK("🔙", "Back");

        final String icon;
        final String label;

        PresetsMenuOption(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }
    }

    private void showPresetsLibraryMenu() {
        while (true) {
            formatter.println(formatter.section("📚 Presets Library"));
            formatter.println("");
            for (int i = 0; i < PresetsMenuOption.values().length; i++) {
                PresetsMenuOption opt = PresetsMenuOption.values()[i];
                formatter.println(String.format("  %d. %s %s", i + 1, opt.icon, opt.label));
            }
            formatter.println("");

            String input = menu.promptInput("Select option (1-" + PresetsMenuOption.values().length + ")");
            if (input == null) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }

            int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                formatter.println(formatter.errorMessage("Invalid input"));
                continue;
            }

            if (choice < 1 || choice > PresetsMenuOption.values().length) {
                formatter.println(formatter.errorMessage("Invalid selection"));
                continue;
            }

            PresetsMenuOption selected = PresetsMenuOption.values()[choice - 1];
            formatter.println("");
            switch (selected) {
                case AGENTS -> manageAgentPresets();
                case SKILLS -> manageSkillPresets();
                case MCP -> manageMcpPresets();
                case EXPORT -> exportPresetsTemplates();
                case IMPORT -> importPresetsTemplates();
                case BACK -> {
                    return;
                }
            }

            formatter.println("");
        }
    }

    private void exportPresetsTemplates() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();
        progress.startSpinner("Exporting templates");
        try {
            presetLibraryManager.exportTemplates(lib);
            progress.success("Templates exported");
        } catch (Exception e) {
            progress.error("Export failed: " + e.getMessage());
        }
    }

    private void importPresetsTemplates() {
        boolean confirm = menu.promptConfirm("Import will overwrite presets.json. Continue?", false);
        if (!confirm) {
            return;
        }

        progress.startSpinner("Importing templates");
        try {
            presetLibraryManager.importTemplates();
            progress.success("Templates imported");
        } catch (Exception e) {
            progress.error("Import failed: " + e.getMessage());
        }
    }

    private enum ManageMenuOption {
        LIST("📋", "List"),
        ADD("➕", "Add"),
        DELETE("🗑️", "Delete"),
        BACK("🔙", "Back");

        final String icon;
        final String label;

        ManageMenuOption(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }
    }

    private void manageAgentPresets() {
        while (true) {
            formatter.println(formatter.section("🧩 Agent Presets"));
            formatter.println("");
            for (int i = 0; i < ManageMenuOption.values().length; i++) {
                ManageMenuOption opt = ManageMenuOption.values()[i];
                formatter.println(String.format("  %d. %s %s", i + 1, opt.icon, opt.label));
            }
            formatter.println("");

            String input = menu.promptInput("Select option (1-" + ManageMenuOption.values().length + ")");
            if (input == null) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }

            int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                formatter.println(formatter.errorMessage("Invalid input"));
                continue;
            }

            if (choice < 1 || choice > ManageMenuOption.values().length) {
                formatter.println(formatter.errorMessage("Invalid selection"));
                continue;
            }

            ManageMenuOption selected = ManageMenuOption.values()[choice - 1];
            switch (selected) {
                case LIST -> listAgentPresets();
                case ADD -> addAgentPreset();
                case DELETE -> deleteAgentPreset();
                case BACK -> {
                    return;
                }
            }
            formatter.println("");
        }
    }

    private void listAgentPresets() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();
        formatter.println(formatter.bold("Agents:"));
        for (AgentPreset a : lib.getAgents()) {
            formatter.println("  - " + a.getId() + " (" + a.getAgentType() + "): " + a.getDisplayName());
        }
    }

    private void addAgentPreset() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();

        String id = menu.promptInput("Agent id (slug)");
        if (id == null || id.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("id is required"));
            return;
        }
        String displayName = menu.promptInput("Display name");
        if (displayName == null || displayName.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("displayName is required"));
            return;
        }
        String description = menu.promptInput("Description (optional)");
        String systemPrompt = menu.promptInput("System prompt");
        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("systemPrompt is required"));
            return;
        }

        AgentPreset preset = new AgentPreset();
        preset.setId(id.trim());
        preset.setAgentType("bob");
        preset.setDisplayName(displayName.trim());
        preset.setDescription(description != null ? description.trim() : "");
        preset.setSystemPrompt(systemPrompt.trim());

        AgentPreset.CustomMode mode = new AgentPreset.CustomMode();
        mode.setSlug(id.trim());
        mode.setName(displayName.trim());
        String roleDefinition = menu.promptInput("Role definition (optional)");
        String whenToUse = menu.promptInput("When to use (optional)");
        mode.setRoleDefinition(roleDefinition != null && !roleDefinition.trim().isEmpty() ? roleDefinition.trim() : "Custom agent");
        mode.setWhenToUse(whenToUse != null && !whenToUse.trim().isEmpty() ? whenToUse.trim() : "Use when you want this behavior.");
        mode.setCustomInstructions(systemPrompt.trim());
        mode.setGroups(List.of("read", "edit", "command", "mcp"));
        preset.setCustomMode(mode);

        presetLibraryManager.upsertAgent(lib, preset);
        try {
            presetLibraryManager.save(lib);
            presetLibraryManager.exportTemplates(lib);
            formatter.println(formatter.success("Saved"));
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Save failed: " + e.getMessage()));
        }
    }

    private void deleteAgentPreset() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();
        if (lib.getAgents().isEmpty()) {
            formatter.println(formatter.warningMessage("No agents to delete"));
            return;
        }

        for (int i = 0; i < lib.getAgents().size(); i++) {
            AgentPreset a = lib.getAgents().get(i);
            formatter.println(String.format("  %d. %s", i + 1, a.getId() + " - " + a.getDisplayName()));
        }
        String input = menu.promptInput("Select agent number to delete");
        if (input == null) {
            return;
        }
        try {
            int idx = Integer.parseInt(input.trim());
            if (idx < 1 || idx > lib.getAgents().size()) {
                formatter.println(formatter.errorMessage("Invalid selection"));
                return;
            }
            String id = lib.getAgents().get(idx - 1).getId();
            presetLibraryManager.deleteAgent(lib, id);
            presetLibraryManager.save(lib);
            presetLibraryManager.exportTemplates(lib);
            formatter.println(formatter.success("Deleted"));
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Delete failed: " + e.getMessage()));
        }
    }

    private void manageSkillPresets() {
        while (true) {
            formatter.println(formatter.section("🛠️ Skill Presets"));
            formatter.println("");
            for (int i = 0; i < ManageMenuOption.values().length; i++) {
                ManageMenuOption opt = ManageMenuOption.values()[i];
                formatter.println(String.format("  %d. %s %s", i + 1, opt.icon, opt.label));
            }
            formatter.println("");

            String input = menu.promptInput("Select option (1-" + ManageMenuOption.values().length + ")");
            if (input == null) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }

            int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                formatter.println(formatter.errorMessage("Invalid input"));
                continue;
            }

            if (choice < 1 || choice > ManageMenuOption.values().length) {
                formatter.println(formatter.errorMessage("Invalid selection"));
                continue;
            }

            ManageMenuOption selected = ManageMenuOption.values()[choice - 1];
            switch (selected) {
                case LIST -> listSkillPresets();
                case ADD -> addSkillPreset();
                case DELETE -> deleteSkillPreset();
                case BACK -> {
                    return;
                }
            }
            formatter.println("");
        }
    }

    private void listSkillPresets() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();
        formatter.println(formatter.bold("Skills:"));
        for (SkillPreset s : lib.getSkills()) {
            formatter.println("  - " + s.getSlug() + ": " + s.getName());
        }
    }

    private void addSkillPreset() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();

        String slug = menu.promptInput("Skill slug");
        if (slug == null || slug.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("slug is required"));
            return;
        }
        String name = menu.promptInput("Skill name");
        if (name == null || name.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("name is required"));
            return;
        }
        String description = menu.promptInput("Description (optional)");
        String markdown = menu.promptInput("SKILL.md content (use \\n for new lines)");
        if (markdown == null) {
            markdown = "";
        }
        markdown = markdown.replace("\\n", "\n");

        SkillPreset preset = new SkillPreset();
        preset.setSlug(slug.trim());
        preset.setName(name.trim());
        preset.setDescription(description != null ? description.trim() : "");
        preset.setSkillMarkdown(markdown);

        presetLibraryManager.upsertSkill(lib, preset);
        try {
            presetLibraryManager.save(lib);
            presetLibraryManager.exportTemplates(lib);
            formatter.println(formatter.success("Saved"));
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Save failed: " + e.getMessage()));
        }
    }

    private void deleteSkillPreset() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();
        if (lib.getSkills().isEmpty()) {
            formatter.println(formatter.warningMessage("No skills to delete"));
            return;
        }

        for (int i = 0; i < lib.getSkills().size(); i++) {
            SkillPreset s = lib.getSkills().get(i);
            formatter.println(String.format("  %d. %s", i + 1, s.getSlug() + " - " + s.getName()));
        }
        String input = menu.promptInput("Select skill number to delete");
        if (input == null) {
            return;
        }
        try {
            int idx = Integer.parseInt(input.trim());
            if (idx < 1 || idx > lib.getSkills().size()) {
                formatter.println(formatter.errorMessage("Invalid selection"));
                return;
            }
            String slug = lib.getSkills().get(idx - 1).getSlug();
            presetLibraryManager.deleteSkill(lib, slug);
            presetLibraryManager.save(lib);
            presetLibraryManager.exportTemplates(lib);
            formatter.println(formatter.success("Deleted"));
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Delete failed: " + e.getMessage()));
        }
    }

    private void manageMcpPresets() {
        while (true) {
            formatter.println(formatter.section("🔌 MCP Presets"));
            formatter.println("");
            for (int i = 0; i < ManageMenuOption.values().length; i++) {
                ManageMenuOption opt = ManageMenuOption.values()[i];
                formatter.println(String.format("  %d. %s %s", i + 1, opt.icon, opt.label));
            }
            formatter.println("");

            String input = menu.promptInput("Select option (1-" + ManageMenuOption.values().length + ")");
            if (input == null) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }

            int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                formatter.println(formatter.errorMessage("Invalid input"));
                continue;
            }

            if (choice < 1 || choice > ManageMenuOption.values().length) {
                formatter.println(formatter.errorMessage("Invalid selection"));
                continue;
            }

            ManageMenuOption selected = ManageMenuOption.values()[choice - 1];
            switch (selected) {
                case LIST -> listMcpPresets();
                case ADD -> addMcpPreset();
                case DELETE -> deleteMcpPreset();
                case BACK -> {
                    return;
                }
            }
            formatter.println("");
        }
    }

    private void listMcpPresets() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();
        formatter.println(formatter.bold("MCP presets:"));
        for (McpPreset m : lib.getMcps()) {
            formatter.println("  - " + m.getName());
        }
    }

    private void addMcpPreset() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();

        String name = menu.promptInput("MCP preset name (e.g., github-mcp)");
        if (name == null || name.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("name is required"));
            return;
        }

        formatter.println("Paste MCP server JSON (single line).");
        String json = menu.promptInput("server");
        if (json == null || json.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("server JSON is required"));
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> server = mapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {});
            McpPreset preset = new McpPreset();
            preset.setName(name.trim());
            preset.setServer(server);
            presetLibraryManager.upsertMcp(lib, preset);
            presetLibraryManager.save(lib);
            presetLibraryManager.exportTemplates(lib);
            formatter.println(formatter.success("Saved"));
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Invalid JSON: " + e.getMessage()));
        }
    }

    private void deleteMcpPreset() {
        PresetLibrary lib = presetLibraryManager.loadOrCreate();
        if (lib.getMcps().isEmpty()) {
            formatter.println(formatter.warningMessage("No MCP presets to delete"));
            return;
        }

        for (int i = 0; i < lib.getMcps().size(); i++) {
            McpPreset m = lib.getMcps().get(i);
            formatter.println(String.format("  %d. %s", i + 1, m.getName()));
        }
        String input = menu.promptInput("Select MCP preset number to delete");
        if (input == null) {
            return;
        }
        try {
            int idx = Integer.parseInt(input.trim());
            if (idx < 1 || idx > lib.getMcps().size()) {
                formatter.println(formatter.errorMessage("Invalid selection"));
                return;
            }
            String name = lib.getMcps().get(idx - 1).getName();
            presetLibraryManager.deleteMcp(lib, name);
            presetLibraryManager.save(lib);
            presetLibraryManager.exportTemplates(lib);
            formatter.println(formatter.success("Deleted"));
        } catch (Exception e) {
            formatter.println(formatter.errorMessage("Delete failed: " + e.getMessage()));
        }
    }

    // ========== AI Provider Configuration Methods ==========

    private void showAIProviderMenu() {
        formatter.println(formatter.section("🤖 AI Provider Configuration"));
        formatter.println("");

        AIProviderConfig currentConfig = configManager.getAIProviderConfig();
        
        if (currentConfig != null && currentConfig.isValid()) {
            formatter.println(formatter.bold("Current Provider: " + currentConfig.getType().getDisplayName()));
            formatter.println("   Model: " + currentConfig.getModelName());
            formatter.println("   API Key: " + currentConfig.getMaskedApiKey());
            formatter.println(formatter.successMessage("   Status: ✓ Configured"));
        } else {
            formatter.println(formatter.warningMessage("No AI provider configured"));
        }
        
        formatter.println("");
        formatter.println(formatter.bold("? Select an option:"));
        formatter.println("");

        for (int i = 0; i < AIProviderMenuOption.values().length; i++) {
            AIProviderMenuOption option = AIProviderMenuOption.values()[i];
            formatter.println(String.format("  %d. %s %s", i + 1, option.icon, option.label));
        }

        formatter.println("");
        String input = menu.promptInput("Select option (1-" + AIProviderMenuOption.values().length + ")");
        if (input == null) {
            formatter.println(formatter.errorMessage("Invalid input"));
            return;
        }

        try {
            int choice = Integer.parseInt(input.trim());
            if (choice >= 1 && choice <= AIProviderMenuOption.values().length) {
                AIProviderMenuOption selected = AIProviderMenuOption.values()[choice - 1];
                formatter.println("");
                handleAIProviderOption(selected);
            }
        } catch (NumberFormatException e) {
            formatter.println(formatter.errorMessage("Invalid input"));
        }
    }

    private void handleAIProviderOption(AIProviderMenuOption option) {
        switch (option) {
            case CHANGE:
                selectAndConfigureProvider();
                break;
            case UPDATE:
                updateCurrentProvider();
                break;
            case SHOW:
                showAIProviderConfiguration();
                break;
            case BACK:
                // Return to settings menu
                break;
        }
    }

    private void selectAndConfigureProvider() {
        formatter.println(formatter.section("🤖 Select AI Provider"));
        formatter.println("");
        formatter.println("Choose your AI provider:");
        formatter.println("");
        formatter.println("  1. 🟢 OpenAI");
        formatter.println("     Most popular, GPT-4 support");
        formatter.println("");
        formatter.println("  2. 🔵 OpenRouter");
        formatter.println("     Access multiple models");
        formatter.println("");
        formatter.println("  3. ⚙️  Custom Provider");
        formatter.println("     Use your own API endpoint");
        formatter.println("");
        formatter.println("  4. 🔙 Back");
        formatter.println("");

        String input = menu.promptInput("Select provider (1-4)");
        if (input == null) {
            formatter.println(formatter.errorMessage("Invalid input"));
            return;
        }

        try {
            int choice = Integer.parseInt(input.trim());
            formatter.println("");
            
            switch (choice) {
                case 1:
                    configureOpenAI();
                    break;
                case 2:
                    configureOpenRouter();
                    break;
                case 3:
                    configureCustomProvider();
                    break;
                case 4:
                    // Back
                    break;
                default:
                    formatter.println(formatter.errorMessage("Invalid selection"));
            }
        } catch (NumberFormatException e) {
            formatter.println(formatter.errorMessage("Invalid input"));
        }
    }

    private void configureOpenAI() {
        formatter.println(formatter.bold("Configure OpenAI"));
        formatter.println("");
        formatter.println("You'll need an OpenAI API key from https://platform.openai.com/api-keys");
        formatter.println("");

        String apiKey = menu.promptInput("Enter your OpenAI API key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("API key is required"));
            return;
        }

        AIProviderConfig config = new AIProviderConfig(AIProviderType.OPENAI, apiKey.trim());
        
        saveAIProviderConfig(config);
    }

    private void configureOpenRouter() {
        formatter.println(formatter.bold("Configure OpenRouter"));
        formatter.println("");
        formatter.println("You'll need an OpenRouter API key from https://openrouter.ai/keys");
        formatter.println("");

        String apiKey = menu.promptInput("Enter your OpenRouter API key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("API key is required"));
            return;
        }

        AIProviderConfig config = new AIProviderConfig(AIProviderType.OPENROUTER, apiKey.trim());
        
        // Ask for optional custom headers
        boolean addHeaders = menu.promptConfirm("Add custom headers? (optional)", false);
        if (addHeaders) {
            Map<String, String> headers = new HashMap<>();
            
            String referer = menu.promptInput("HTTP-Referer (press Enter to skip)");
            if (referer != null && !referer.trim().isEmpty()) {
                headers.put("HTTP-Referer", referer.trim());
            }
            
            String title = menu.promptInput("X-Title (press Enter to skip)");
            if (title != null && !title.trim().isEmpty()) {
                headers.put("X-Title", title.trim());
            }
            
            if (!headers.isEmpty()) {
                config.setHeaders(headers);
            }
        }
        
        saveAIProviderConfig(config);
    }

    private void configureCustomProvider() {
        formatter.println(formatter.bold("Configure Custom Provider"));
        formatter.println("");
        formatter.println("Configure your custom OpenAI-compatible API endpoint");
        formatter.println("");

        String baseUrl = menu.promptInput("Enter base URL (e.g., https://api.example.com/v1)");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("Base URL is required"));
            return;
        }

        String apiKey = menu.promptInput("Enter API key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("API key is required"));
            return;
        }

        String modelName = menu.promptInput("Enter model name (e.g., gpt-4, custom-model)");
        if (modelName == null || modelName.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("Model name is required"));
            return;
        }

        AIProviderConfig config = new AIProviderConfig(AIProviderType.CUSTOM, apiKey.trim());
        config.setBaseUrl(baseUrl.trim());
        config.setModelName(modelName.trim());
        
        // Ask for optional custom headers
        boolean addHeaders = menu.promptConfirm("Add custom headers? (optional)", false);
        if (addHeaders) {
            Map<String, String> headers = new HashMap<>();
            
            while (true) {
                String headerName = menu.promptInput("Header name (press Enter to finish)");
                if (headerName == null || headerName.trim().isEmpty()) {
                    break;
                }
                
                String headerValue = menu.promptInput("Header value for " + headerName);
                if (headerValue != null && !headerValue.trim().isEmpty()) {
                    headers.put(headerName.trim(), headerValue.trim());
                }
                
                boolean addMore = menu.promptConfirm("Add another header?", false);
                if (!addMore) {
                    break;
                }
            }
            
            if (!headers.isEmpty()) {
                config.setHeaders(headers);
            }
        }
        
        saveAIProviderConfig(config);
    }

    private void updateCurrentProvider() {
        AIProviderConfig currentConfig = configManager.getAIProviderConfig();
        
        if (currentConfig == null || !currentConfig.isValid()) {
            formatter.println(formatter.warningMessage("No provider configured. Please select a provider first."));
            return;
        }

        formatter.println(formatter.bold("Update " + currentConfig.getType().getDisplayName() + " Configuration"));
        formatter.println("");
        
        switch (currentConfig.getType()) {
            case OPENAI:
                configureOpenAI();
                break;
            case OPENROUTER:
                configureOpenRouter();
                break;
            case CUSTOM:
                configureCustomProvider();
                break;
        }
    }

    private void showAIProviderConfiguration() {
        AIProviderConfig config = configManager.getAIProviderConfig();
        
        if (config == null || !config.isValid()) {
            formatter.println(formatter.warningMessage("No AI provider configured"));
            return;
        }

        formatter.println(formatter.bold("AI Provider Configuration:"));
        formatter.println("");
        formatter.println(formatter.info("Provider: " + config.getType().getDisplayName()));
        formatter.println("   Base URL: " + config.getBaseUrl());
        formatter.println("   Model: " + config.getModelName());
        formatter.println("   API Key: " + config.getMaskedApiKey());
        formatter.println("   Temperature: " + config.getTemperature());
        formatter.println("   Max Tokens: " + config.getMaxTokens());
        
        if (config.getHeaders() != null && !config.getHeaders().isEmpty()) {
            formatter.println("");
            formatter.println(formatter.info("Custom Headers:"));
            config.getHeaders().forEach((key, value) ->
                formatter.println("   " + key + ": " + maskValue(value))
            );
        }
        
        formatter.println("");
    }

    private void saveAIProviderConfig(AIProviderConfig config) {
        formatter.println("");
        progress.loading("💾 Saving configuration to ~/.onepiece/config.json");
        
        try {
            configManager.updateAIProviderConfig(config);
            Thread.sleep(1000);
            
            formatter.println("");
            formatter.println(formatter.success("✅ AI Provider configured successfully!"));
            formatter.println("");
            formatter.println(formatter.info("Provider: " + config.getType().getDisplayName()));
            formatter.println(formatter.info("Model: " + config.getModelName()));
            formatter.println("");
            formatter.println("You can now use AI features in One Piece CLI.");
            formatter.println("");
        } catch (Exception e) {
            formatter.println("");
            formatter.println(formatter.errorMessage("Failed to save configuration: " + e.getMessage()));
            formatter.println("");
        }
    }

    private String maskValue(String value) {
        if (value == null || value.length() < 8) {
            return "***";
        }
        return value.substring(0, 4) + "..." + "*".repeat(Math.min(value.length() - 4, 10));
    }

    private void testConnection() {
        VaultConfig config = configManager.getVaultConfig();
        if (config == null || !config.isConfigured()) {
            formatter.println(formatter.errorMessage("Vault is not configured. Use 'onepiece settings' to configure it."));
            return;
        }

        progress.startSpinner("Testing Vault connection");
        boolean reachable = vaultClient.testConnection(config.getUrl(), config.getToken());
        boolean tokenValid = vaultClient.isTokenValid(config.getUrl(), config.getToken());
        if (!reachable) {
            progress.error("Vault not reachable");
            return;
        }
        if (!tokenValid) {
            progress.error("Token is not valid");
            return;
        }

        progress.success("Connection successful");
        formatter.println("");
        formatter.println(formatter.bold("Test Results:"));
        formatter.println(formatter.successMessage("   ✓ Vault is reachable"));
        formatter.println(formatter.successMessage("   ✓ Token is valid"));
        formatter.println("");

        try {
            vaultClient.listSecrets(config.getUrl(), config.getToken(), "secret");
            formatter.println(formatter.successMessage("   ✓ Can list secrets"));
        } catch (Exception e) {
            formatter.println(formatter.warningMessage("   Limited permissions to list secrets"));
        }
        formatter.println("");
    }

    private void showIbmCloudMenu() {
        formatter.println(formatter.section("☁️ IBM Cloud Deployment Credentials"));
        formatter.println("");

        IbmCloudConfig current = configManager.getIbmCloudConfig();
        if (current != null && current.isConfigured()) {
            formatter.println(formatter.info("Current:"));
            formatter.println("   API Key: " + current.getMaskedApiKey());
            if (current.getResourceGroup() != null && !current.getResourceGroup().isBlank()) {
                formatter.println("   Resource group: " + current.getResourceGroup().trim());
            }
            if (current.getCodeEngineProject() != null && !current.getCodeEngineProject().isBlank()) {
                formatter.println("   Code Engine project: " + current.getCodeEngineProject().trim());
            }
            if (current.getRegion() != null && !current.getRegion().isBlank()) {
                formatter.println("   Region: " + current.getRegion().trim());
            }
            if (current.getOrg() != null && !current.getOrg().isBlank()) {
                formatter.println("   Org: " + current.getOrg().trim());
            }
            if (current.getSpace() != null && !current.getSpace().isBlank()) {
                formatter.println("   Space: " + current.getSpace().trim());
            }
            formatter.println("");
        }

        if (nonInteractive) {
            return;
        }

        String apiKey = menu.promptInput("Enter your IBM Cloud API key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            formatter.println(formatter.errorMessage("IBM Cloud API key is required"));
            return;
        }

        String resourceGroup = menu.promptInput("Default resource group (press Enter to skip)");
        String ceProject = menu.promptInput("Default Code Engine project (press Enter to skip)");
        String region = menu.promptInput("Default region (press Enter to skip)");
        String org = menu.promptInput("Default Cloud Foundry org (press Enter to skip)");
        String space = menu.promptInput("Default Cloud Foundry space (press Enter to skip)");

        configureIbmCloud(apiKey, region, org, space, resourceGroup, ceProject);
    }

    private void showDeploymentConfigMenu() {
        while (true) {
            formatter.println(formatter.section("☁️ Deployment Config"));
            formatter.println("");

            for (int i = 0; i < DeploymentConfigMenuOption.values().length; i++) {
                DeploymentConfigMenuOption opt = DeploymentConfigMenuOption.values()[i];
                formatter.println(String.format("  %d. %s %s", i + 1, opt.icon, opt.label));
            }

            formatter.println("");
            String input = menu.promptInput("Select option (1-" + DeploymentConfigMenuOption.values().length + ")");
            if (input == null) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }

            int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                formatter.println(formatter.errorMessage("Invalid input"));
                continue;
            }

            if (choice < 1 || choice > DeploymentConfigMenuOption.values().length) {
                formatter.println(formatter.errorMessage("Invalid selection"));
                continue;
            }

            DeploymentConfigMenuOption selected = DeploymentConfigMenuOption.values()[choice - 1];
            formatter.println("");
            switch (selected) {
                case IBM_CLOUD -> showIbmCloudMenu();
                case FLYIO -> formatter.println(formatter.warningMessage("Fly.io deployment config is not implemented yet."));
                case BACK -> {
                    return;
                }
            }

            formatter.println("");
        }
    }

    private void configureIbmCloud(String apiKey, String region, String org, String space, String resourceGroup, String codeEngineProject) {
        progress.loading("💾 Saving configuration to ~/.onepiece/config.json");
        try {
            String normalizedApiKey = apiKey.trim();
            String normalizedRegion = region != null && !region.trim().isEmpty() ? region.trim() : null;
            String normalizedOrg = org != null && !org.trim().isEmpty() ? org.trim() : null;
            String normalizedSpace = space != null && !space.trim().isEmpty() ? space.trim() : null;
            String normalizedResourceGroup = resourceGroup != null && !resourceGroup.trim().isEmpty() ? resourceGroup.trim() : null;
            String normalizedCeProject = codeEngineProject != null && !codeEngineProject.trim().isEmpty() ? codeEngineProject.trim() : null;

            configManager.updateIbmCloudConfig(
                new IbmCloudConfig(
                    normalizedApiKey,
                    normalizedRegion,
                    normalizedOrg,
                    normalizedSpace,
                    normalizedResourceGroup,
                    normalizedCeProject
                )
            );
        } catch (Exception e) {
            formatter.println("");
            formatter.println(formatter.errorMessage("Failed to save configuration: " + e.getMessage()));
            formatter.println("");
            return;
        }

        formatter.println("");
        formatter.println(formatter.success("✅ IBM Cloud credentials saved successfully!"));
        formatter.println("");
    }

    private void showConfiguration() {
        formatter.println(formatter.bold("Current Configuration:"));
        formatter.println("");
        
        // Show AI Provider configuration
        AIProviderConfig aiConfig = configManager.getAIProviderConfig();
        if (aiConfig != null && aiConfig.isValid()) {
            formatter.println(formatter.info("AI Provider:"));
            formatter.println("   Type: " + aiConfig.getType().getDisplayName());
            formatter.println("   Model: " + aiConfig.getModelName());
            formatter.println("   API Key: " + aiConfig.getMaskedApiKey());
            formatter.println("");
        }
        
        // Show Vault configuration
        formatter.println(formatter.info("Vault Settings:"));
        if (configManager.hasVault()) {
            formatter.println("   URL: " + configManager.getVaultConfig().getUrl());
            formatter.println("   Token: " + configManager.getVaultConfig().getMaskedToken());
            formatter.println("   Status: Configured");
        } else {
            formatter.println("   Status: Not configured");
        }
        formatter.println("");

        IbmCloudConfig ibmCloud = configManager.getIbmCloudConfig();
        formatter.println(formatter.info("IBM Cloud Settings:"));
        if (ibmCloud != null && ibmCloud.isConfigured()) {
            formatter.println("   API Key: " + ibmCloud.getMaskedApiKey());
            if (ibmCloud.getResourceGroup() != null && !ibmCloud.getResourceGroup().isBlank()) {
                formatter.println("   Resource group: " + ibmCloud.getResourceGroup().trim());
            }
            if (ibmCloud.getCodeEngineProject() != null && !ibmCloud.getCodeEngineProject().isBlank()) {
                formatter.println("   Code Engine project: " + ibmCloud.getCodeEngineProject().trim());
            }
            if (ibmCloud.getRegion() != null && !ibmCloud.getRegion().isBlank()) {
                formatter.println("   Region: " + ibmCloud.getRegion().trim());
            }
            if (ibmCloud.getOrg() != null && !ibmCloud.getOrg().isBlank()) {
                formatter.println("   Org: " + ibmCloud.getOrg().trim());
            }
            if (ibmCloud.getSpace() != null && !ibmCloud.getSpace().isBlank()) {
                formatter.println("   Space: " + ibmCloud.getSpace().trim());
            }
            formatter.println("   Status: Configured");
        } else {
            formatter.println("   Status: Not configured");
        }
        formatter.println("");
    }

    private void resetConfiguration() {
        if (!nonInteractive) {
            boolean confirm = menu.promptConfirm("Are you sure you want to reset all settings?", false);
            if (!confirm) {
                formatter.println(formatter.info("Reset cancelled"));
                return;
            }
        }

        formatter.println("");
        progress.loading("Resetting configuration...");
        try {
            configManager.resetConfig();
        } catch (Exception e) {
            formatter.println("");
            formatter.println(formatter.errorMessage("Failed to reset configuration: " + e.getMessage()));
            formatter.println("");
            return;
        }
        
        formatter.println("");
        formatter.println(formatter.success("✅ Configuration reset successfully"));
        formatter.println("");
    }
}

// Made with Bob
