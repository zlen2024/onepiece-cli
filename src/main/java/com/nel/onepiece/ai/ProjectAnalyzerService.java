package com.nel.onepiece.ai;

import com.nel.onepiece.model.ProjectAnalysis;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for analyzing project structure and generating AI-powered recommendations
 */
@ApplicationScoped
public class ProjectAnalyzerService {

    private static final Logger LOG = Logger.getLogger(ProjectAnalyzerService.class);

    @Inject
    ProjectAnalyzerAIImpl analyzerAI;

    @Inject
    AIProviderService aiProviderService;

    /**
     * Analyze a project directory
     * 
     * @param projectPath Path to the project directory
     * @return ProjectAnalysis with recommendations
     */
    public ProjectAnalysis analyzeProject(String projectPath) {
        try {
            // Check if AI provider is configured
            if (!aiProviderService.isConfigured()) {
                LOG.debug("AI provider not configured. Using fallback analysis.");
                return fallbackAnalysis(projectPath);
            }

            // Scan the project directory
            String projectStructure = scanProjectDirectory(projectPath);
            
            // Use AI to analyze the structure
            ProjectAnalysis analysis = analyzerAI.analyzeProject(projectStructure);
            return normalizeAnalysis(analysis);
            
        } catch (Exception e) {
            LOG.debugf("AI analysis failed: %s", e.getMessage());
            // Fallback to manual detection if AI fails
            return fallbackAnalysis(projectPath);
        }
    }

    private ProjectAnalysis normalizeAnalysis(ProjectAnalysis analysis) {
        if (analysis == null) {
            return fallbackAnalysis(".");
        }
        List<String> mcps = analysis.getRecommendedMcps();
        if (mcps == null) {
            mcps = Collections.emptyList();
        }
        if (!mcps.contains("filesystem-mcp")) {
            List<String> updated = new ArrayList<>(mcps);
            updated.add(0, "filesystem-mcp");
            mcps = updated;
        }
        analysis.setRecommendedMcps(mcps);
        return analysis;
    }

    /**
     * Scan project directory and create a string representation
     */
    private String scanProjectDirectory(String projectPath) throws IOException {
        Path root = Paths.get(projectPath);
        StringBuilder structure = new StringBuilder();
        
        structure.append("Project Root: ").append(root.toAbsolutePath()).append("\n\n");
        structure.append("Files and Directories:\n");
        
        try (Stream<Path> paths = Files.walk(root, 3)) {
            List<Path> filteredPaths = paths
                .filter(path -> !shouldExclude(path))
                .collect(Collectors.toList());
            
            for (Path path : filteredPaths) {
                int depth = path.getNameCount() - root.getNameCount();
                String indent = "  ".repeat(depth);
                String name = path.getFileName().toString();
                
                if (Files.isDirectory(path)) {
                    structure.append(indent).append("📁 ").append(name).append("/\n");
                } else {
                    structure.append(indent).append("📄 ").append(name).append("\n");
                }
            }
        }
        
        // Add file content samples for key files
        structure.append("\n\nKey Files Content:\n");
        addFileContent(structure, root, "pom.xml");
        addFileContent(structure, root, "build.gradle");
        addFileContent(structure, root, "package.json");
        addFileContent(structure, root, "requirements.txt");
        addFileContent(structure, root, "Dockerfile");
        
        return structure.toString();
    }

    /**
     * Check if a path should be excluded from scanning
     */
    private boolean shouldExclude(Path path) {
        String pathStr = path.toString();
        return pathStr.contains("node_modules") ||
               pathStr.contains("target") ||
               pathStr.contains("build") ||
               pathStr.contains(".git") ||
               pathStr.contains(".idea") ||
               pathStr.contains("__pycache__") ||
               pathStr.contains(".vscode");
    }

    /**
     * Add file content to the structure string
     */
    private void addFileContent(StringBuilder structure, Path root, String fileName) {
        try {
            Path filePath = root.resolve(fileName);
            if (Files.exists(filePath)) {
                structure.append("\n--- ").append(fileName).append(" ---\n");
                List<String> lines = Files.readAllLines(filePath);
                // Only include first 50 lines to avoid token limits
                lines.stream()
                    .limit(50)
                    .forEach(line -> structure.append(line).append("\n"));
                if (lines.size() > 50) {
                    structure.append("... (truncated)\n");
                }
            }
        } catch (IOException e) {
            // Ignore if file can't be read
        }
    }

    /**
     * Fallback analysis when AI is not available
     */
    private ProjectAnalysis fallbackAnalysis(String projectPath) {
        ProjectAnalysis analysis = new ProjectAnalysis();
        Path root = Paths.get(projectPath);
        
        // Detect framework and language
        if (Files.exists(root.resolve("pom.xml"))) {
            analysis.setLanguage("Java");
            analysis.setBuildTool("Maven");
            
            // Check for Quarkus
            try {
                String pomContent = Files.readString(root.resolve("pom.xml"));
                if (pomContent.contains("quarkus")) {
                    analysis.setFramework("Quarkus");
                } else if (pomContent.contains("spring-boot")) {
                    analysis.setFramework("Spring Boot");
                } else {
                    analysis.setFramework("Java");
                }
            } catch (IOException e) {
                analysis.setFramework("Java");
            }
            
            analysis.setProjectType("Java Application");
            analysis.setRecommendedMcps(List.of("filesystem-mcp", "github-mcp", "maven-mcp"));
            
        } else if (Files.exists(root.resolve("build.gradle")) || Files.exists(root.resolve("build.gradle.kts"))) {
            analysis.setLanguage("Java/Kotlin");
            analysis.setBuildTool("Gradle");
            analysis.setFramework("Gradle Project");
            analysis.setProjectType("Java/Kotlin Application");
            analysis.setRecommendedMcps(List.of("filesystem-mcp", "github-mcp", "gradle-mcp"));
            
        } else if (Files.exists(root.resolve("package.json"))) {
            analysis.setLanguage("JavaScript/TypeScript");
            analysis.setBuildTool("npm");
            
            try {
                String packageJson = Files.readString(root.resolve("package.json"));
                if (packageJson.contains("express")) {
                    analysis.setFramework("Express");
                } else if (packageJson.contains("react")) {
                    analysis.setFramework("React");
                } else if (packageJson.contains("vue")) {
                    analysis.setFramework("Vue");
                } else {
                    analysis.setFramework("Node.js");
                }
            } catch (IOException e) {
                analysis.setFramework("Node.js");
            }
            
            analysis.setProjectType("Web Application");
            analysis.setRecommendedMcps(List.of("filesystem-mcp", "github-mcp", "npm-mcp"));
            
        } else if (Files.exists(root.resolve("requirements.txt")) || Files.exists(root.resolve("setup.py"))) {
            analysis.setLanguage("Python");
            analysis.setBuildTool("pip");
            
            try {
                if (Files.exists(root.resolve("requirements.txt"))) {
                    String requirements = Files.readString(root.resolve("requirements.txt"));
                    if (requirements.contains("django")) {
                        analysis.setFramework("Django");
                    } else if (requirements.contains("flask")) {
                        analysis.setFramework("Flask");
                    } else if (requirements.contains("fastapi")) {
                        analysis.setFramework("FastAPI");
                    } else {
                        analysis.setFramework("Python");
                    }
                }
            } catch (IOException e) {
                analysis.setFramework("Python");
            }
            
            analysis.setProjectType("Python Application");
            analysis.setRecommendedMcps(List.of("filesystem-mcp", "github-mcp"));
            
        } else {
            analysis.setLanguage("Unknown");
            analysis.setBuildTool("Unknown");
            analysis.setFramework("Unknown");
            analysis.setProjectType("Generic Project");
            analysis.setRecommendedMcps(List.of("filesystem-mcp"));
        }
        
        // Check for additional features
        analysis.setHasTests(Files.exists(root.resolve("src/test")) || 
                            Files.exists(root.resolve("test")) ||
                            Files.exists(root.resolve("tests")));
        
        analysis.setHasDocumentation(Files.exists(root.resolve("README.md")) ||
                                    Files.exists(root.resolve("docs")));
        
        // Add git MCP if .git exists
        if (Files.exists(root.resolve(".git"))) {
            List<String> mcps = new ArrayList<>(analysis.getRecommendedMcps());
            if (!mcps.contains("github-mcp")) {
                mcps.add("github-mcp");
            }
            analysis.setRecommendedMcps(mcps);
        }
        
        return analysis;
    }

    /**
     * Generate system prompt for the agent
     */
    public String generateSystemPrompt(ProjectAnalysis analysis, String agentType) {
        try {
            if (!aiProviderService.isConfigured()) {
                return generateFallbackSystemPrompt(analysis, agentType);
            }
            return analyzerAI.generateSystemPrompt(analysis, agentType);
        } catch (Exception e) {
            LOG.debugf("AI prompt generation failed: %s", e.getMessage());
            return generateFallbackSystemPrompt(analysis, agentType);
        }
    }

    /**
     * Fallback system prompt generation
     */
    private String generateFallbackSystemPrompt(ProjectAnalysis analysis, String agentType) {
        return String.format("""
            You are %s, an AI coding assistant working on a %s project.
            
            Project Details:
            - Language: %s
            - Framework: %s
            - Build Tool: %s
            - Type: %s
            
            You have access to the following MCP servers:
            %s
            
            Your role is to help with code development, testing, documentation, and deployment.
            Always follow best practices for %s development.
            """,
            agentType,
            analysis.getFramework(),
            analysis.getLanguage(),
            analysis.getFramework(),
            analysis.getBuildTool(),
            analysis.getProjectType(),
            String.join(", ", analysis.getRecommendedMcps()),
            analysis.getFramework()
        );
    }

    /**
     * Recommend skills for the agent
     */
    public List<String> recommendSkills(ProjectAnalysis analysis) {
        try {
            if (!aiProviderService.isConfigured()) {
                return List.of("code-review", "test-generation", "documentation");
            }
            String skills = analyzerAI.recommendSkills(analysis);
            return List.of(skills.split(",\\s*"));
        } catch (Exception e) {
            LOG.debugf("AI skill recommendation failed: %s", e.getMessage());
            // Fallback skills
            return List.of("code-review", "test-generation", "documentation");
        }
    }
}

// Made with Bob
