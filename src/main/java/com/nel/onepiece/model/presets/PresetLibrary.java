package com.nel.onepiece.model.presets;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class PresetLibrary {

    @JsonProperty("version")
    private String version = "1.0.0";

    @JsonProperty("agents")
    private List<AgentPreset> agents = new ArrayList<>();

    @JsonProperty("skills")
    private List<SkillPreset> skills = new ArrayList<>();

    @JsonProperty("mcps")
    private List<McpPreset> mcps = new ArrayList<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<AgentPreset> getAgents() {
        return agents;
    }

    public void setAgents(List<AgentPreset> agents) {
        this.agents = agents;
    }

    public List<SkillPreset> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillPreset> skills) {
        this.skills = skills;
    }

    public List<McpPreset> getMcps() {
        return mcps;
    }

    public void setMcps(List<McpPreset> mcps) {
        this.mcps = mcps;
    }
}

