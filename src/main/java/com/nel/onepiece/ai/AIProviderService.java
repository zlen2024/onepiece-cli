package com.nel.onepiece.ai;

import com.nel.onepiece.config.ConfigManager;
import com.nel.onepiece.model.config.AIProviderConfig;
import com.nel.onepiece.model.config.AIProviderType;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.Objects;

/**
 * Service for managing AI provider configuration and creating chat models dynamically
 */
@ApplicationScoped
public class AIProviderService {

    @Inject
    ConfigManager configManager;

    private ChatLanguageModel cachedModel;
    private AIProviderConfig cachedConfig;

    /**
     * Get or create a chat language model based on current configuration
     */
    public ChatLanguageModel getChatModel() {
        AIProviderConfig config = configManager.getAIProviderConfig();
        
        if (config == null || !config.isValid()) {
            throw new IllegalStateException(
                "AI provider not configured. Please run 'onepiece settings' to configure an AI provider."
            );
        }

        // Return cached model if configuration hasn't changed
        if (cachedModel != null && isSameConfig(config, cachedConfig)) {
            return cachedModel;
        }

        // Create new model based on provider type
        cachedModel = createChatModel(config);
        cachedConfig = config;
        
        return cachedModel;
    }

    /**
     * Create a chat model based on provider configuration
     */
    private ChatLanguageModel createChatModel(AIProviderConfig config) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
            .apiKey(config.getApiKey())
            .modelName(config.getModelName())
            .temperature(config.getTemperature())
            .maxTokens(config.getMaxTokens())
            .timeout(Duration.ofSeconds(60));

        // Set base URL if provided
        if (config.getBaseUrl() != null && !config.getBaseUrl().trim().isEmpty()) {
            builder.baseUrl(config.getBaseUrl());
        }

        // Note: Custom headers are not supported in LangChain4j 0.27.1
        // The OpenAiChatModel.OpenAiChatModelBuilder doesn't have a method to set custom headers
        // To add custom headers support, you would need to:
        // 1. Upgrade to a newer version of LangChain4j that supports custom headers
        // 2. Or implement a custom HTTP client with header support
        // For now, custom headers configuration is ignored
        
        // Provider-specific configuration
        if (config.getType() == AIProviderType.OPENROUTER) {
            // OpenRouter-specific settings can be added here when custom headers are supported
            // Required headers: HTTP-Referer and X-Title
        }

        return builder.build();
    }

    /**
     * Check if two configurations are the same
     */
    private boolean isSameConfig(AIProviderConfig config1, AIProviderConfig config2) {
        if (config1 == null || config2 == null) {
            return false;
        }
        
        return config1.getType() == config2.getType()
            && Objects.equals(config1.getApiKey(), config2.getApiKey())
            && Objects.equals(config1.getModelName(), config2.getModelName())
            && Objects.equals(config1.getBaseUrl(), config2.getBaseUrl());
    }

    /**
     * Clear cached model (force recreation on next request)
     */
    public void clearCache() {
        cachedModel = null;
        cachedConfig = null;
    }

    /**
     * Get current provider type
     */
    public AIProviderType getCurrentProviderType() {
        AIProviderConfig config = configManager.getAIProviderConfig();
        return config != null ? config.getType() : null;
    }

    /**
     * Check if AI provider is configured
     */
    public boolean isConfigured() {
        return configManager.hasAIProvider();
    }
}

// Made with Bob
