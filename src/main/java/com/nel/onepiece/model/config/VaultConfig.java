package com.nel.onepiece.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration model for HashiCorp Vault settings
 */
public class VaultConfig {
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("token")
    private String token;

    public VaultConfig() {}

    public VaultConfig(String url, String token) {
        this.url = url;
        this.token = token;
    }

    // Getters and setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Check if vault is configured
     */
    public boolean isConfigured() {
        return url != null && !url.trim().isEmpty() 
            && token != null && !token.trim().isEmpty();
    }

    /**
     * Get masked token for display
     */
    public String getMaskedToken() {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + "*".repeat(20);
    }
}

// Made with Bob