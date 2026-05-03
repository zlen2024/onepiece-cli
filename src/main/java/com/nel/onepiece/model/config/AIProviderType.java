package com.nel.onepiece.model.config;

/**
 * Enum representing supported AI provider types
 */
public enum AIProviderType {
    OPENAI("OpenAI", "https://api.openai.com/v1", "gpt-4"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1", "openai/gpt-4"),
    CUSTOM("Custom Provider", null, null);

    private final String displayName;
    private final String defaultBaseUrl;
    private final String defaultModel;

    AIProviderType(String displayName, String defaultBaseUrl, String defaultModel) {
        this.displayName = displayName;
        this.defaultBaseUrl = defaultBaseUrl;
        this.defaultModel = defaultModel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    /**
     * Get provider type from string (case-insensitive)
     */
    public static AIProviderType fromString(String type) {
        if (type == null) {
            return null;
        }
        for (AIProviderType providerType : values()) {
            if (providerType.name().equalsIgnoreCase(type)) {
                return providerType;
            }
        }
        return null;
    }
}

// Made with Bob