package com.nel.onepiece.commands;

import com.nel.onepiece.ai.ProjectAnalyzerService;
import com.nel.onepiece.config.ConfigManager;
import com.nel.onepiece.deployment.IbmCloudExecutor;
import com.nel.onepiece.model.ProjectAnalysis;
import com.nel.onepiece.model.config.VaultConfig;
import com.nel.onepiece.security.VaultClient;
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
    VaultClient vaultClient;

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
        defaultValue = "us-south"
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

        Path projectPath = Paths.get(projectDir).toAbsolutePath().normalize();
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

        formatter.println(formatter.info("App name: " + appName));
        formatter.println(formatter.info("Region: " + region));
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

        String apiKey = null;
        String finalRegion = region;
        String org = null;
        String space = null;

        if (configManager.hasVault()) {
            VaultConfig vaultConfig = configManager.getVaultConfig();
            progress.startSpinner("Fetching IBM Cloud credentials from Vault");
            try {
                VaultClient.IbmCloudCredentials creds = vaultClient.getIbmCloudCredentials(vaultConfig.getUrl(), vaultConfig.getToken());
                apiKey = creds.apiKey();
                if (creds.region() != null && !creds.region().isBlank()) {
                    finalRegion = creds.region();
                }
                org = creds.org();
                space = creds.space();
                progress.success("Credentials retrieved");
            } catch (Exception e) {
                progress.error("Failed to fetch credentials from Vault");
                formatter.println(formatter.muted(e.getMessage()));
                return;
            }
            formatter.println("");
        } else {
            apiKey = System.getenv("IBM_CLOUD_API_KEY");
            String envRegion = System.getenv("IBM_CLOUD_REGION");
            if (envRegion != null && !envRegion.isBlank()) {
                finalRegion = envRegion;
            }
        }

        if (apiKey == null || apiKey.isBlank()) {
            formatter.println(formatter.errorMessage("IBM Cloud API key not found. Configure Vault or set IBM_CLOUD_API_KEY."));
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

        if ((org != null && !org.isBlank()) || (space != null && !space.isBlank())) {
            progress.startSpinner("Targeting Cloud Foundry org/space");
            try {
                boolean targeted = ibmCloudExecutor.targetCf(org, space);
                if (!targeted) {
                    progress.error("Targeting Cloud Foundry failed");
                    return;
                }
                progress.success("Cloud Foundry target set");
            } catch (Exception e) {
                progress.error("Targeting Cloud Foundry failed");
                formatter.println(formatter.muted(e.getMessage()));
                return;
            }
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

        String buildTool = analysis.getBuildTool() != null ? analysis.getBuildTool() : "Maven";
        String buildpack = ibmCloudExecutor.detectBuildpack(analysis.getFramework() != null ? analysis.getFramework() : "java");
        try {
            ibmCloudExecutor.generateManifest(projectPath.toString(), appName, buildpack, 512, 1);
        } catch (Exception e) {
            formatter.println(formatter.warningMessage("Failed to generate manifest.yml"));
        }

        progress.startSpinner("Building application (" + buildTool + ")");
        try {
            boolean built = ibmCloudExecutor.buildApplication(projectPath.toString(), buildTool);
            if (!built) {
                progress.error("Build failed");
                return;
            }
            progress.success("Build successful");
        } catch (Exception e) {
            progress.error("Build failed");
            formatter.println(formatter.muted(e.getMessage()));
            return;
        }

        progress.startSpinner("Deploying to IBM Cloud (Cloud Foundry)");
        try {
            boolean pushed = ibmCloudExecutor.pushApplication(appName, projectPath.toString());
            if (!pushed) {
                progress.error("Deploy failed");
                return;
            }
            progress.success("Deploy complete");
        } catch (Exception e) {
            progress.error("Deploy failed");
            formatter.println(formatter.muted(e.getMessage()));
            return;
        }

        String status;
        try {
            status = ibmCloudExecutor.getAppStatus(appName);
        } catch (Exception e) {
            status = null;
        }

        // Step 4: Show completion
        formatter.println(formatter.success("✅ Deployment Complete!"));
        formatter.println("");
        
        String appUrl = String.format("https://%s.%s.cf.appdomain.cloud", appName, finalRegion);
        formatter.println(formatter.bold("🌐 Your app is live at:"));
        formatter.println(formatter.highlight("   " + appUrl));
        formatter.println("");
        
        if (status != null && !status.isBlank()) {
            formatter.println(formatter.bold("📊 ibmcloud cf app output:"));
            formatter.println(formatter.separator());
            formatter.println(status.trim());
            formatter.println(formatter.separator());
            formatter.println("");
        }
    }
}

// Made with Bob
