package com.nel.onepiece.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Project analysis result from AI
 */
public class ProjectAnalysis {
    
    @JsonProperty("framework")
    private String framework;
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("buildTool")
    private String buildTool;
    
    @JsonProperty("recommendedMcps")
    private List<String> recommendedMcps;
    
    @JsonProperty("projectType")
    private String projectType;
    
    @JsonProperty("dependencies")
    private List<String> dependencies;
    
    @JsonProperty("hasTests")
    private boolean hasTests;
    
    @JsonProperty("hasDocumentation")
    private boolean hasDocumentation;

    // Constructors
    public ProjectAnalysis() {}

    public ProjectAnalysis(String framework, String language, String buildTool, 
                          List<String> recommendedMcps, String projectType) {
        this.framework = framework;
        this.language = language;
        this.buildTool = buildTool;
        this.recommendedMcps = recommendedMcps;
        this.projectType = projectType;
    }

    // Getters and Setters
    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public List<String> getRecommendedMcps() {
        return recommendedMcps;
    }

    public void setRecommendedMcps(List<String> recommendedMcps) {
        this.recommendedMcps = recommendedMcps;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public boolean isHasTests() {
        return hasTests;
    }

    public void setHasTests(boolean hasTests) {
        this.hasTests = hasTests;
    }

    public boolean isHasDocumentation() {
        return hasDocumentation;
    }

    public void setHasDocumentation(boolean hasDocumentation) {
        this.hasDocumentation = hasDocumentation;
    }

    @Override
    public String toString() {
        return "ProjectAnalysis{" +
                "framework='" + framework + '\'' +
                ", language='" + language + '\'' +
                ", buildTool='" + buildTool + '\'' +
                ", recommendedMcps=" + recommendedMcps +
                ", projectType='" + projectType + '\'' +
                '}';
    }
}

// Made with Bob
