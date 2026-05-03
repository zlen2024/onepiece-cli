package com.nel.onepiece.commands;

import com.nel.onepiece.ai.ProjectAnalyzerService;
import com.nel.onepiece.config.ConfigManager;
import com.nel.onepiece.deployment.IbmCloudExecutor;
import com.nel.onepiece.model.ProjectAnalysis;
import com.nel.onepiece.model.config.IbmCloudConfig;
import com.nel.onepiece.ui.ColorFormatter;
import com.nel.onepiece.ui.InteractiveMenu;
import com.nel.onepiece.ui.ProgressIndicator;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Deploy command - Automate cloud deployment
 * Deploys the application to the selected cloud provider.
 */
@Command(
    name = "deploy",
    description = "Deploy your project to the cloud",
    mixinStandardHelpOptions = true
)
public class DeployCommand implements Runnable {

    @Inject
    ColorFormatter formatter;

    @Inject
    InteractiveMenu menu;

    @Inject
    ProgressIndicator progress;

    @Inject
    ConfigManager configManager;

    @Inject
    IbmCloudExecutor ibmCloudExecutor;

    @Inject
    ProjectAnalyzerService projectAnalyzerService;

    @Option(
        names = {"--target"},
        description = "Deployment target: ${COMPLETION-CANDIDATES}"
    )
    DeploymentTarget target;

    @Option(
        names = {"--region"},
        description = "Cloud region",
        defaultValue = ""
    )
    String region;

    @Option(
        names = {"--app-name"},
        description = "Application name"
    )
    String appName;

    @Option(
        names = {"--project-dir"},
        description = "Project directory to deploy (default: current directory)",
        defaultValue = "."
    )
    String projectDir;

    @Option(
        names = {"--no-interactive"},
        description = "Skip interactive prompts"
    )
    boolean nonInteractive;

    public enum DeploymentTarget {
        IBMCLOUD("☁️", "IBM Cloud"),
        FLYIO("🪰", "Fly.io");

        final String icon;
        final String label;

        DeploymentTarget(String icon, String label) {
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
        formatter.println(formatter.section("🚀 Deploy - Automate Cloud Deployment"));
        formatter.println("");

        // If interactive mode, show target selection
        if (!nonInteractive && target == null) {
            formatter.println(formatter.bold("? Select deployment target:"));
            formatter.println("");
            
            for (int i = 0; i < DeploymentTarget.values().length; i++) {
                DeploymentTarget type = DeploymentTarget.values()[i];
                String recommended = (type == DeploymentTarget.IBMCLOUD) ? " (Recommended for POC)" : "";
                formatter.println(String.format("  %d. %s %s%s", 
                    i + 1, type.icon, type.label, recommended));
            }
            
            formatter.println("");
            String input = menu.promptInput("Select option (1-" + DeploymentTarget.values().length + ")");
            if (input == null) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }
            
            try {
                int choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= DeploymentTarget.values().length) {
                    target = DeploymentTarget.values()[choice - 1];
                } else {
                    formatter.println(formatter.errorMessage("Invalid selection"));
                    return;
                }
            } catch (NumberFormatException e) {
                formatter.println(formatter.errorMessage("Invalid input"));
                return;
            }
        }

        // Default to IBM Cloud if not specified
        if (target == null) {
            target = DeploymentTarget.IBMCLOUD;
        }

        formatter.println("");
        formatter.println(formatter.info("Selected target: " + target.icon + " " + target.label));
        formatter.println("");

        String effectiveProjectDir = projectDir;
        if (effectiveProjectDir == null || effectiveProjectDir.isBlank()) {
            effectiveProjectDir = ".";
        }
        if (region == null || region.isBlank()) {
            region = null;
        }

        Path projectPath = Paths.get(effectiveProjectDir).toAbsolutePath().normalize();
        if (!Files.exists(projectPath) || !Files.isDirectory(projectPath)) {
            formatter.println(formatter.errorMessage("Project directory not found: " + projectPath));
            return;
        }

        // Get app name if not provided
        if (appName == null && !nonInteractive) {
            appName = menu.promptInput("Enter your app name");
            if (appName == null || appName.trim().isEmpty()) {
                formatter.println(formatter.errorMessage("App name is required"));
                return;
            }
        }

        if (appName == null) {
            appName = "my-app";
        }

        IbmCloudConfig ibmCloudConfig = configManager.getIbmCloudConfig();
        if (ibmCloudConfig == null || !ibmCloudConfig.isConfigured()) {
            if (nonInteractive) {
                formatter.println(formatter.errorMessage("IBM Cloud credentials not configured. Run 'onepiece settings' to set IBM Cloud API key."));
                return;
            }

            formatter.println(formatter.warningMessage("IBM Cloud credentials not configured."));
            boolean configureNow = menu.promptConfirm("Configure deployment credentials now?", true);
            formatter.println("");
            if (!configureNow) {
                formatter.println(formatter.muted("Run 'onepiece settings' later to configure deployment credentials."));
                return;
            }

            String apiKeyInput = menu.promptInput("Enter your IBM Cloud API key");
            if (apiKeyInput == null || apiKeyInput.trim().isEmpty()) {
                formatter.println(formatter.errorMessage("IBM Cloud API key is required"));
                return;
            }

            String rgInput = menu.promptInput("Default resource group (default: Default)");
            if (rgInput != null && rgInput.trim().isEmpty()) {
                rgInput = null;
            }
            String ceProjectInput = menu.promptInput("Code Engine project name (default: onepiece)");
            if (ceProjectInput != null && ceProjectInput.trim().isEmpty()) {
                ceProjectInput = null;
            }
            String regionInput = menu.promptInput("Default region (default: us-south)");
            if (regionInput != null && regionInput.trim().isEmpty()) {
                regionInput = null;
            }

            String normalizedRg = (rgInput == null || rgInput.isBlank()) ? "Default" : rgInput.trim();
            String normalizedCeProject = (ceProjectInput == null || ceProjectInput.isBlank()) ? "onepiece" : ceProjectInput.trim();
            String normalizedRegion = (regionInput == null || regionInput.isBlank()) ? "us-south" : regionInput.trim();

            progress.loading("💾 Saving configuration to ~/.onepiece/config.json");
            try {
                configManager.updateIbmCloudConfig(new IbmCloudConfig(apiKeyInput.trim(), normalizedRegion, null, null, normalizedRg, normalizedCeProject));
                ibmCloudConfig = configManager.getIbmCloudConfig();
            } catch (Exception e) {
                formatter.println("");
                formatter.println(formatter.errorMessage("Failed to save configuration: " + e.getMessage()));
                formatter.println("");
                return;
            }

            formatter.println("");
        }

        String apiKey = ibmCloudConfig.getApiKey();
        String finalRegion = region;
        if (finalRegion == null || finalRegion.isBlank()) {
            String configRegion = ibmCloudConfig.getRegion();
            finalRegion = (configRegion != null && !configRegion.isBlank()) ? configRegion.trim() : "us-south";
        }
        String resourceGroup = ibmCloudConfig.getResourceGroup();
        String codeEngineProject = ibmCloudConfig.getCodeEngineProject();

        formatter.println(formatter.info("App name: " + appName));
        formatter.println(formatter.info("Region: " + finalRegion));
        formatter.println(formatter.info("Project: " + projectPath));
        formatter.println("");

        if (target != DeploymentTarget.IBMCLOUD) {
            formatter.println(formatter.warningMessage("Only IBM Cloud deployment is implemented for this POC."));
            return;
        }

        if (!ibmCloudExecutor.isCliInstalled()) {
            formatter.println(formatter.errorMessage("IBM Cloud CLI not found. Install it first to use deploy."));
            formatter.println(formatter.muted("https://cloud.ibm.com/docs/cli"));
            return;
        }

        if (!ibmCloudExecutor.isPluginInstalled("code-engine")) {
            formatter.println(formatter.errorMessage("IBM Cloud Code Engine plugin not found. Install it first to use deploy."));
            formatter.println(formatter.muted("ibmcloud plugin install code-engine"));
            return;
        }

        progress.startSpinner("Logging in to IBM Cloud");
        try {
            boolean loggedIn = ibmCloudExecutor.login(apiKey, finalRegion);
            if (!loggedIn) {
                progress.error("IBM Cloud login failed");
                return;
            }
            progress.success("Logged in");
        } catch (Exception e) {
            progress.error("IBM Cloud login failed");
            formatter.println(formatter.muted(e.getMessage()));
            return;
        }

        if (resourceGroup != null && !resourceGroup.isBlank()) {
            progress.startSpinner("Targeting resource group");
            try {
                boolean targeted = ibmCloudExecutor.targetResourceGroup(resourceGroup);
                if (!targeted) {
                    progress.error("Targeting resource group failed");
                    return;
                }
                progress.success("Resource group target set");
            } catch (Exception e) {
                progress.error("Targeting resource group failed");
                formatter.println(formatter.muted(e.getMessage()));
                return;
            }
        }

        if (codeEngineProject == null || codeEngineProject.isBlank()) {
            formatter.println(formatter.errorMessage("Code Engine project is not configured. Run 'onepiece settings' to set --ibmcloud-ce-project."));
            return;
        }

        progress.startSpinner("Preparing Code Engine project");
        try {
            boolean ok = ibmCloudExecutor.ensureCodeEngineProject(codeEngineProject);
            if (!ok) {
                progress.error("Failed to prepare Code Engine project");
                return;
            }
            progress.success("Code Engine project ready: " + codeEngineProject.trim());
        } catch (Exception e) {
            progress.error("Failed to prepare Code Engine project");
            formatter.println(formatter.muted(e.getMessage()));
            return;
        }

        ProjectAnalysis analysis;
        progress.startSpinner("Detecting project build settings");
        try {
            analysis = projectAnalyzerService.analyzeProject(projectPath.toString());
            progress.success("Project detected: " + analysis.getFramework());
        } catch (Exception e) {
            progress.error("Project detection failed");
            formatter.println(formatter.muted(e.getMessage()));
            return;
        }

        progress.startSpinner("Deploying to IBM Cloud (Code Engine)");
        try {
            IbmCloudExecutor.CommandResult created = ibmCloudExecutor.createCodeEngineAppFromLocalSource(appName, projectPath.toString());
            if (!created.isSuccess()) {
                IbmCloudExecutor.CommandResult updated = ibmCloudExecutor.updateCodeEngineAppFromLocalSource(appName, projectPath.toString());
                if (!updated.isSuccess()) {
                    progress.error("Deploy failed");
                    return;
                }
            }
            progress.success("Deploy complete");
        } catch (Exception e) {
            progress.error("Deploy failed");
            formatter.println(formatter.muted(e.getMessage()));
            return;
        }

        String status;
        try {
            status = ibmCloudExecutor.getCodeEngineApp(appName);
        } catch (Exception e) {
            status = null;
        }

        formatter.println(formatter.success("✅ Deployment Complete!"));
        formatter.println("");

        String liveUrl = ibmCloudExecutor.extractFirstUrl(status);
        if (liveUrl != null) {
            formatter.println(formatter.bold("🌐 Your app is live at:"));
            formatter.println(formatter.highlight("   " + liveUrl));
            formatter.println("");
        }
        
        if (status != null && !status.isBlank()) {
            formatter.println(formatter.bold("📊 ibmcloud ce application get output:"));
            formatter.println(formatter.separator());
            formatter.println(status.trim());
            formatter.println(formatter.separator());
            formatter.println("");
        }
    }
}

// Made with Bob
