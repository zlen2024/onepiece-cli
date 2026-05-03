package com.nel.onepiece.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Configuration model for .bob.workspace file
 */
public class BobWorkspaceConfig {
    
    @JsonProperty("workspace")
    private WorkspaceInfo workspace;
    
    @JsonProperty("mcpServers")
    private Map<String, McpServer> mcpServers;
    
    @JsonProperty("agent")
    private AgentConfig agent;
    
    @JsonProperty("skills")
    private List<String> skills;
    
    @JsonProperty("settings")
    private Settings settings;
    
    @JsonProperty("excludePatterns")
    private List<String> excludePatterns;

    // Nested classes
    public static class WorkspaceInfo {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("version")
        private String version;

        public WorkspaceInfo() {}

        public WorkspaceInfo(String name, String description, String version) {
            this.name = name;
            this.description = description;
            this.version = version;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    public static class McpServer {
        @JsonProperty("command")
        private String command;
        
        @JsonProperty("args")
        private List<String> args;
        
        @JsonProperty("env")
        private Map<String, String> env;

        public McpServer() {}

        public McpServer(String command, List<String> args) {
            this.command = command;
            this.args = args;
        }

        // Getters and setters
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        public List<String> getArgs() { return args; }
        public void setArgs(List<String> args) { this.args = args; }
        public Map<String, String> getEnv() { return env; }
        public void setEnv(Map<String, String> env) { this.env = env; }
    }

    public static class AgentConfig {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("model")
        private String model;
        
        @JsonProperty("temperature")
        private double temperature;
        
        @JsonProperty("maxTokens")
        private int maxTokens;
        
        @JsonProperty("systemPrompt")
        private String systemPrompt;

        public AgentConfig() {}

        public AgentConfig(String name, String model) {
            this.name = name;
            this.model = model;
            this.temperature = 0.7;
            this.maxTokens = 2000;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
        public String getSystemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    }

    public static class Settings {
        @JsonProperty("autoSave")
        private boolean autoSave;
        
        @JsonProperty("contextWindow")
        private int contextWindow;
        
        @JsonProperty("enableLogging")
        private boolean enableLogging;

        public Settings() {
            this.autoSave = true;
            this.contextWindow = 8000;
            this.enableLogging = true;
        }

        // Getters and setters
        public boolean isAutoSave() { return autoSave; }
        public void setAutoSave(boolean autoSave) { this.autoSave = autoSave; }
        public int getContextWindow() { return contextWindow; }
        public void setContextWindow(int contextWindow) { this.contextWindow = contextWindow; }
        public boolean isEnableLogging() { return enableLogging; }
        public void setEnableLogging(boolean enableLogging) { this.enableLogging = enableLogging; }
    }

    // Main class constructors
    public BobWorkspaceConfig() {}

    // Getters and setters
    public WorkspaceInfo getWorkspace() { return workspace; }
    public void setWorkspace(WorkspaceInfo workspace) { this.workspace = workspace; }
    public Map<String, McpServer> getMcpServers() { return mcpServers; }
    public void setMcpServers(Map<String, McpServer> mcpServers) { this.mcpServers = mcpServers; }
    public AgentConfig getAgent() { return agent; }
    public void setAgent(AgentConfig agent) { this.agent = agent; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public Settings getSettings() { return settings; }
    public void setSettings(Settings settings) { this.settings = settings; }
    public List<String> getExcludePatterns() { return excludePatterns; }
    public void setExcludePatterns(List<String> excludePatterns) { this.excludePatterns = excludePatterns; }
}

// Made with Bob
