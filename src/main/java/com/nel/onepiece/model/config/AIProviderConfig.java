package com.nel.onepiece.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration model for AI provider settings
 */
public class AIProviderConfig {
    
    @JsonProperty("type")
    private AIProviderType type;
    
    @JsonProperty("apiKey")
    private String apiKey;
    
    @JsonProperty("baseUrl")
    private String baseUrl;
    
    @JsonProperty("modelName")
    private String modelName;
    
    @JsonProperty("temperature")
    private double temperature = 0.7;
    
    @JsonProperty("maxTokens")
    private int maxTokens = 2000;
    
    @JsonProperty("headers")
    private Map<String, String> headers;

    public AIProviderConfig() {
        this.headers = new HashMap<>();
    }

    public AIProviderConfig(AIProviderType type, String apiKey) {
        this.type = type;
        this.apiKey = apiKey;
        this.baseUrl = type.getDefaultBaseUrl();
        this.modelName = type.getDefaultModel();
        this.temperature = 0.7;
        this.maxTokens = 2000;
        this.headers = new HashMap<>();
    }

    // Getters and setters
    public AIProviderType getType() {
        return type;
    }

    public void setType(AIProviderType type) {
        this.type = type;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Check if the configuration is valid
     */
    public boolean isValid() {
        if (type == null || apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        if (type == AIProviderType.CUSTOM) {
            return baseUrl != null && !baseUrl.trim().isEmpty() 
                && modelName != null && !modelName.trim().isEmpty();
        }
        
        return true;
    }

    /**
     * Get masked API key for display (show first 7 chars and last 4 chars)
     */
    public String getMaskedApiKey() {
        if (apiKey == null || apiKey.length() < 12) {
            return "***";
        }
        return apiKey.substring(0, 7) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}

// Made with Bob