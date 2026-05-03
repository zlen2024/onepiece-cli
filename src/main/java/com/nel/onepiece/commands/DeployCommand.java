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
            formatter.println(formatter.errorMessage("IBM Cloud credentials not configured. Run 'onepiece settings' to set IBM Cloud API key."));
            return;
        }

        String apiKey = ibmCloudConfig.getApiKey();
        String finalRegion = region;
        if (finalRegion == null || finalRegion.isBlank()) {
            String configRegion = ibmCloudConfig.getRegion();
            finalRegion = (configRegion != null && !configRegion.isBlank()) ? configRegion.trim() : "us-south";
        }
        String org = ibmCloudConfig.getOrg();
        String space = ibmCloudConfig.getSpace();

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

        try {
            String pathToPush = ibmCloudExecutor.detectDeployPath(projectPath.toString(), buildTool);
            ibmCloudExecutor.generateManifest(projectPath.toString(), appName, buildpack, 512, 1, pathToPush, true);
        } catch (Exception e) {
            formatter.println(formatter.warningMessage("Failed to generate manifest.yml"));
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

        formatter.println(formatter.success("✅ Deployment Complete!"));
        formatter.println("");

        String liveUrl = getLiveUrlFromCfOutput(status);
        if (liveUrl != null) {
            formatter.println(formatter.bold("🌐 Your app is live at:"));
            formatter.println(formatter.highlight("   " + liveUrl));
            formatter.println("");
        }
        
        if (status != null && !status.isBlank()) {
            formatter.println(formatter.bold("📊 ibmcloud cf app output:"));
            formatter.println(formatter.separator());
            formatter.println(status.trim());
            formatter.println(formatter.separator());
            formatter.println("");
        }
    }

    private String getLiveUrlFromCfOutput(String cfAppOutput) {
        if (cfAppOutput == null || cfAppOutput.isBlank()) {
            return null;
        }
        for (String line : cfAppOutput.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String lower = trimmed.toLowerCase();
            if (!lower.startsWith("routes:")) {
                continue;
            }
            String routes = trimmed.substring(trimmed.indexOf(':') + 1).trim();
            if (routes.isEmpty()) {
                return null;
            }
            String first = routes.split("[,\\s]+")[0].trim();
            if (first.isEmpty()) {
                return null;
            }
            if (first.startsWith("http://") || first.startsWith("https://")) {
                return first;
            }
            return "https://" + first;
        }
        return null;
    }
}

// Made with Bob
