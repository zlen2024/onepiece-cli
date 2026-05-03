package com.nel.onepiece.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IbmCloudConfig {

    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("region")
    private String region;

    @JsonProperty("org")
    private String org;

    @JsonProperty("space")
    private String space;

    @JsonProperty("resourceGroup")
    private String resourceGroup;

    @JsonProperty("codeEngineProject")
    private String codeEngineProject;

    public IbmCloudConfig() {}

    public IbmCloudConfig(String apiKey, String region, String org, String space, String resourceGroup, String codeEngineProject) {
        this.apiKey = apiKey;
        this.region = region;
        this.org = org;
        this.space = space;
        this.resourceGroup = resourceGroup;
        this.codeEngineProject = codeEngineProject;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public String getCodeEngineProject() {
        return codeEngineProject;
    }

    public void setCodeEngineProject(String codeEngineProject) {
        this.codeEngineProject = codeEngineProject;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    public String getMaskedApiKey() {
        if (apiKey == null || apiKey.length() < 12) {
            return "***";
        }
        return apiKey.substring(0, 7) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
