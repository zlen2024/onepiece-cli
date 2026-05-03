package com.nel.onepiece.deployment;

import com.nel.onepiece.ui.ColorFormatter;
import com.nel.onepiece.ui.ProgressIndicator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Executor for IBM Cloud CLI commands
 * Uses ProcessBuilder to run ibmcloud CLI in the background
 */
@ApplicationScoped
public class IbmCloudExecutor {

    @Inject
    ColorFormatter formatter;

    @Inject
    ProgressIndicator progress;

    /**
     * Check if IBM Cloud CLI is installed
     */
    public boolean isCliInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ibmcloud", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Login to IBM Cloud
     */
    public boolean login(String apiKey, String region) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ibmcloud");
        command.add("login");
        command.add("--apikey");
        command.add(apiKey);
        if (region != null && !region.isBlank()) {
            command.add("--region");
            command.add(region);
        } else {
            command.add("--no-region");
        }

        CommandResult result = executeCommand(command, null);
        return result.exitCode == 0;
    }

    /**
     * Target Cloud Foundry org and space
     */
    public boolean targetCf(String org, String space) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ibmcloud");
        command.add("target");
        command.add("--cf");
        
        if (org != null) {
            command.add("-o");
            command.add(org);
        }
        
        if (space != null) {
            command.add("-s");
            command.add(space);
        }

        CommandResult result = executeCommand(command, null);
        return result.exitCode == 0;
    }

    /**
     * Build the application
     */
    public boolean buildApplication(String projectPath, String buildTool) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        
        if ("Maven".equalsIgnoreCase(buildTool)) {
            // Check for Maven wrapper
            Path mvnwCmd = Paths.get(projectPath, "mvnw.cmd");
            Path mvnw = Paths.get(projectPath, "mvnw");
            if (Files.exists(mvnwCmd)) {
                command.add(mvnwCmd.toString());
            } else if (Files.exists(mvnw)) {
                if (Files.isExecutable(mvnw)) {
                    command.add(mvnw.toString());
                } else {
                    command.add("bash");
                    command.add(mvnw.toString());
                }
            } else {
                command.add("mvn");
            }
            command.add("clean");
            command.add("package");
            command.add("-DskipTests");
        } else if ("Gradle".equalsIgnoreCase(buildTool)) {
            Path gradlewBat = Paths.get(projectPath, "gradlew.bat");
            Path gradlew = Paths.get(projectPath, "gradlew");
            if (Files.exists(gradlewBat)) {
                command.add(gradlewBat.toString());
            } else if (Files.exists(gradlew)) {
                if (Files.isExecutable(gradlew)) {
                    command.add(gradlew.toString());
                } else {
                    command.add("bash");
                    command.add(gradlew.toString());
                }
            } else {
                command.add("gradle");
            }
            command.add("clean");
            command.add("build");
            command.add("-x");
            command.add("test");
        } else if ("npm".equalsIgnoreCase(buildTool)) {
            command.add("npm");
            command.add("run");
            command.add("build");
        } else {
            formatter.println(formatter.warningMessage("Unknown build tool: " + buildTool));
            return false;
        }

        CommandResult result = executeCommand(command, projectPath);
        return result.exitCode == 0;
    }

    /**
     * Push application to Cloud Foundry
     */
    public boolean pushApplication(String appName, String projectPath) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ibmcloud");
        command.add("cf");
        command.add("push");
        command.add(appName);

        CommandResult result = executeCommand(command, projectPath);
        return result.exitCode == 0;
    }

    /**
     * Get application status
     */
    public String getAppStatus(String appName) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ibmcloud");
        command.add("cf");
        command.add("app");
        command.add(appName);

        CommandResult result = executeCommand(command, null);
        return result.output;
    }

    /**
     * Get application logs
     */
    public void streamLogs(String appName, LogCallback callback) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ibmcloud");
        command.add("cf");
        command.add("logs");
        command.add(appName);
        command.add("--recent");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                callback.onLogLine(line);
            }
        }
        
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
        }
    }

    /**
     * Execute a command and capture output
     */
    private CommandResult executeCommand(List<String> command, String workingDirectory) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        
        if (workingDirectory != null) {
            pb.directory(Paths.get(workingDirectory).toFile());
        }
        
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                // Stream output to console
                formatter.println(formatter.muted(line));
            }
        }
        
        int exitCode = process.waitFor();
        
        return new CommandResult(exitCode, output.toString());
    }

    /**
     * Execute command with progress indicator
     */
    public CommandResult executeWithProgress(List<String> command, String workingDirectory, String progressMessage) throws IOException, InterruptedException {
        progress.startSpinner(progressMessage);
        
        try {
            CommandResult result = executeCommand(command, workingDirectory);
            
            if (result.exitCode == 0) {
                progress.success(progressMessage + " - Complete");
            } else {
                progress.error(progressMessage + " - Failed");
            }
            
            return result;
        } catch (Exception e) {
            progress.error(progressMessage + " - Error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Generate manifest.yml for Cloud Foundry
     */
    public void generateManifest(
        String projectPath,
        String appName,
        String buildpack,
        int memory,
        int instances,
        String path,
        boolean randomRoute
    ) throws IOException {
        StringBuilder manifest = new StringBuilder();
        manifest.append("---\n");
        manifest.append("applications:\n");
        manifest.append("- name: ").append(appName).append("\n");
        manifest.append("  memory: ").append(memory).append("M\n");
        manifest.append("  instances: ").append(instances).append("\n");
        
        if (buildpack != null) {
            manifest.append("  buildpack: ").append(buildpack).append("\n");
        }

        if (randomRoute) {
            manifest.append("  random-route: true\n");
        }
        String normalizedPath = (path == null || path.isBlank()) ? "." : path.trim();
        manifest.append("  path: ").append(normalizedPath).append("\n");
        
        Path manifestPath = Paths.get(projectPath, "manifest.yml");
        Files.writeString(manifestPath, manifest.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        formatter.println(formatter.successMessage("Generated manifest.yml"));
    }

    public String detectDeployPath(String projectPath, String buildTool) throws IOException {
        if (buildTool == null || buildTool.isBlank()) {
            return ".";
        }

        String tool = buildTool.trim();
        if ("Maven".equalsIgnoreCase(tool)) {
            return detectJarPath(projectPath, "target");
        }
        if ("Gradle".equalsIgnoreCase(tool)) {
            return detectJarPath(projectPath, "build/libs");
        }
        if ("npm".equalsIgnoreCase(tool)) {
            Path dist = Paths.get(projectPath, "dist");
            if (Files.exists(dist) && Files.isDirectory(dist)) {
                return "dist";
            }
            return ".";
        }

        return ".";
    }

    private String detectJarPath(String projectPath, String relativeDir) throws IOException {
        Path dir = Paths.get(projectPath, relativeDir);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return ".";
        }

        String bestName = null;
        long bestSize = -1;

        try (var stream = Files.list(dir)) {
            for (Path p : (Iterable<Path>) stream::iterator) {
                if (!Files.isRegularFile(p)) {
                    continue;
                }
                String name = p.getFileName().toString();
                if (!name.endsWith(".jar")) {
                    continue;
                }
                String lower = name.toLowerCase();
                if (lower.endsWith("-sources.jar") || lower.endsWith("-javadoc.jar") || lower.startsWith("original-")) {
                    continue;
                }
                long size;
                try {
                    size = Files.size(p);
                } catch (Exception e) {
                    size = 0;
                }
                if (size > bestSize) {
                    bestSize = size;
                    bestName = name;
                }
            }
        }

        if (bestName == null) {
            return ".";
        }
        return relativeDir + "/" + bestName;
    }

    /**
     * Detect buildpack based on project type
     */
    public String detectBuildpack(String framework) {
        return switch (framework.toLowerCase()) {
            case "quarkus", "spring boot", "java" -> "java_buildpack";
            case "node.js", "express", "react", "vue" -> "nodejs_buildpack";
            case "python", "django", "flask", "fastapi" -> "python_buildpack";
            case "go" -> "go_buildpack";
            case "ruby", "rails" -> "ruby_buildpack";
            case "php" -> "php_buildpack";
            default -> null;
        };
    }

    /**
     * Command result holder
     */
    public static class CommandResult {
        public final int exitCode;
        public final String output;

        public CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }
    }

    /**
     * Callback interface for log streaming
     */
    @FunctionalInterface
    public interface LogCallback {
        void onLogLine(String line);
    }
}

// Made with Bob
