package com.nel.onepiece.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Main configuration model for One Piece CLI
 * Stored in ~/.onepiece/config.json
 */
public class OnePieceConfig {
    
    @JsonProperty("version")
    private String version = "1.0.0";
    
    @JsonProperty("aiProvider")
    private AIProviderConfig aiProvider;
    
    @JsonProperty("vault")
    private VaultConfig vault;

    public OnePieceConfig() {}

    // Getters and setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public AIProviderConfig getAiProvider() {
        return aiProvider;
    }

    public void setAiProvider(AIProviderConfig aiProvider) {
        this.aiProvider = aiProvider;
    }

    public VaultConfig getVault() {
        return vault;
    }

    public void setVault(VaultConfig vault) {
        this.vault = vault;
    }

    /**
     * Check if AI provider is configured
     */
    public boolean hasAIProvider() {
        return aiProvider != null && aiProvider.isValid();
    }

    /**
     * Check if Vault is configured
     */
    public boolean hasVault() {
        return vault != null && vault.isConfigured();
    }
}

// Made with Bob