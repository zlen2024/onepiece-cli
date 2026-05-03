package com.nel.onepiece.model.presets;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class AgentPreset {

    @JsonProperty("id")
    private String id;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("agentType")
    private String agentType;

    @JsonProperty("systemPrompt")
    private String systemPrompt;

    @JsonProperty("customMode")
    private CustomMode customMode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public CustomMode getCustomMode() {
        return customMode;
    }

    public void setCustomMode(CustomMode customMode) {
        this.customMode = customMode;
    }

    public static class CustomMode {

        @JsonProperty("slug")
        private String slug;

        @JsonProperty("name")
        private String name;

        @JsonProperty("roleDefinition")
        private String roleDefinition;

        @JsonProperty("whenToUse")
        private String whenToUse;

        @JsonProperty("customInstructions")
        private String customInstructions;

        @JsonProperty("groups")
        private List<String> groups = new ArrayList<>();

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRoleDefinition() {
            return roleDefinition;
        }

        public void setRoleDefinition(String roleDefinition) {
            this.roleDefinition = roleDefinition;
        }

        public String getWhenToUse() {
            return whenToUse;
        }

        public void setWhenToUse(String whenToUse) {
            this.whenToUse = whenToUse;
        }

        public String getCustomInstructions() {
            return customInstructions;
        }

        public void setCustomInstructions(String customInstructions) {
            this.customInstructions = customInstructions;
        }

        public List<String> getGroups() {
            return groups;
        }

        public void setGroups(List<String> groups) {
            this.groups = groups;
        }
    }
}

