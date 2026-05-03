package com.nel.onepiece.model.presets;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public class McpPreset {

    @JsonProperty("name")
    private String name;

    @JsonProperty("server")
    private Map<String, Object> server = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getServer() {
        return server;
    }

    public void setServer(Map<String, Object> server) {
        this.server = server;
    }
}

