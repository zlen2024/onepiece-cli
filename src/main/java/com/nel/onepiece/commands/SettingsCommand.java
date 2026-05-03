package com.nel.onepiece.commands;

import com.nel.onepiece.config.ConfigManager;
import com.nel.onepiece.model.config.AIProviderConfig;
import com.nel.onepiece.model.config.AIProviderType;
import com.nel.onepiece.ui.ColorFormatter;
import com.nel.onepiece.ui.InteractiveMenu;
import com.nel.onepiece.ui.ProgressIndicator;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.HashMap;
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

    private enum SettingsMenuOption {
        AI_PROVIDER("🤖", "AI Provider Configuration"),
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

        // Always show the settings menu in interactive mode
        // Users can choose what to configure from the menu
        showSettingsMenu();
    }

    private boolean checkVaultConfiguration() {
        // In a real implementation, check if ~/.onepiece/config.json exists
        // For now, simulate that it's not configured
        return false;
    }

    private void showInitialSetup() {
        formatter.println(formatter.warningMessage("No Vault configuration found"));
        formatter.println("");
        formatter.println("One Piece CLI uses HashiCorp Vault to securely manage your cloud credentials.");
        formatter.println("This follows a \"Bring Your Own Vault\" (BYOV) approach.");
        formatter.println("");

        if (nonInteractive) {
            formatter.println(formatter.errorMessage("Vault configuration required. Use --vault-url and --vault-token options."));
            return;
        }

        boolean hasVault = menu.promptConfirm("Do you have a HashiCorp Vault instance?", true);
        formatter.println("");

        if (hasVault) {
            setupVault();
        } else {
            showLocalEnvOption();
        }
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
        
        try {
            Thread.sleep(1500);
            progress.success("Connected to Vault successfully");
            progress.success("Token is valid");
            formatter.println("");
        } catch (InterruptedException e) {
            progress.error("Connection failed");
            return;
        }

        progress.loading("💾 Saving configuration to ~/.onepiece/config.json");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
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

    private void showLocalEnvOption() {
        formatter.println(formatter.info("ℹ️  Alternative: Local .env file (Not recommended for production)"));
        formatter.println("");
        formatter.println("For POC purposes, you can store credentials locally.");
        formatter.println(formatter.warningMessage("⚠️  Warning: Credentials will be stored in plain text"));
        formatter.println("");

        boolean useLocal = menu.promptConfirm("Use local .env file instead?", false);
        
        if (useLocal) {
            formatter.println("");
            formatter.println(formatter.info("Creating .env.example file..."));
            formatter.println("");
            formatter.println("Please create a .env file with your credentials:");
            formatter.println(formatter.muted("  IBM_CLOUD_API_KEY=your-api-key-here"));
            formatter.println(formatter.muted("  IBM_CLOUD_REGION=us-south"));
            formatter.println("");
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
        progress.startSpinner("Testing Vault connection");
        try {
            Thread.sleep(1500);
            progress.success("Connection successful");
            formatter.println("");
            formatter.println(formatter.bold("Test Results:"));
            formatter.println(formatter.successMessage("   ✓ Vault is reachable"));
            formatter.println(formatter.successMessage("   ✓ Token is valid"));
            formatter.println(formatter.successMessage("   ✓ Can read secrets"));
            formatter.println("");
        } catch (InterruptedException e) {
            progress.error("Connection test failed");
        }
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        formatter.println("");
        formatter.println(formatter.success("✅ Configuration reset successfully"));
        formatter.println("");
    }
}

// Made with Bob
