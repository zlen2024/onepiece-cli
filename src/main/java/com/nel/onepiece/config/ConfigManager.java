package com.nel.onepiece.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nel.onepiece.model.config.AIProviderConfig;
import com.nel.onepiece.model.config.IbmCloudConfig;
import com.nel.onepiece.model.config.OnePieceConfig;
import com.nel.onepiece.model.config.VaultConfig;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * Service for managing One Piece CLI configuration
 * Handles reading/writing ~/.onepiece/config.json
 */
@ApplicationScoped
public class ConfigManager {

    private final ObjectMapper objectMapper;
    private final Path configDir;
    private final Path configFile;
    private OnePieceConfig cachedConfig;

    public ConfigManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        // Get user home directory
        String userHome = System.getProperty("user.home");
        this.configDir = Paths.get(userHome, ".onepiece");
        this.configFile = configDir.resolve("config.json");
    }

    /**
     * Load configuration from file
     * Creates default config if file doesn't exist
     */
    public OnePieceConfig loadConfig() {
        try {
            if (Files.exists(configFile)) {
                cachedConfig = objectMapper.readValue(configFile.toFile(), OnePieceConfig.class);
                return cachedConfig;
            }
        } catch (IOException e) {
            System.err.println("Warning: Failed to load config file: " + e.getMessage());
            System.err.println("Creating new configuration...");
        }
        
        // Return default config if file doesn't exist or can't be read
        cachedConfig = createDefaultConfig();
        return cachedConfig;
    }

    /**
     * Save configuration to file
     */
    public void saveConfig(OnePieceConfig config) throws IOException {
        // Ensure config directory exists
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        // Write config file
        objectMapper.writeValue(configFile.toFile(), config);
        
        // Set file permissions to user-only (600) on Unix-like systems
        try {
            if (configFile.getFileSystem().supportedFileAttributeViews().contains("posix")) {
                Set<PosixFilePermission> perms = Set.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE
                );
                Files.setPosixFilePermissions(configFile, perms);
            }
        } catch (Exception e) {
            // Ignore permission errors on Windows
        }

        // Update cache
        cachedConfig = config;
    }

    /**
     * Get AI provider configuration
     */
    public AIProviderConfig getAIProviderConfig() {
        OnePieceConfig config = getCachedOrLoadConfig();
        return config.getAiProvider();
    }

    /**
     * Update AI provider configuration
     */
    public void updateAIProviderConfig(AIProviderConfig aiProviderConfig) throws IOException {
        OnePieceConfig config = getCachedOrLoadConfig();
        config.setAiProvider(aiProviderConfig);
        saveConfig(config);
    }

    /**
     * Get Vault configuration
     */
    public VaultConfig getVaultConfig() {
        OnePieceConfig config = getCachedOrLoadConfig();
        return config.getVault();
    }

    /**
     * Update Vault configuration
     */
    public void updateVaultConfig(VaultConfig vaultConfig) throws IOException {
        OnePieceConfig config = getCachedOrLoadConfig();
        config.setVault(vaultConfig);
        saveConfig(config);
    }

    public IbmCloudConfig getIbmCloudConfig() {
        OnePieceConfig config = getCachedOrLoadConfig();
        return config.getIbmCloud();
    }

    public void updateIbmCloudConfig(IbmCloudConfig ibmCloudConfig) throws IOException {
        OnePieceConfig config = getCachedOrLoadConfig();
        config.setIbmCloud(ibmCloudConfig);
        saveConfig(config);
    }

    /**
     * Check if AI provider is configured
     */
    public boolean hasAIProvider() {
        OnePieceConfig config = getCachedOrLoadConfig();
        return config.hasAIProvider();
    }

    /**
     * Check if Vault is configured
     */
    public boolean hasVault() {
        OnePieceConfig config = getCachedOrLoadConfig();
        return config.hasVault();
    }

    public boolean hasIbmCloud() {
        OnePieceConfig config = getCachedOrLoadConfig();
        return config.hasIbmCloud();
    }

    /**
     * Get config directory path
     */
    public Path getConfigDir() {
        return configDir;
    }

    /**
     * Get config file path
     */
    public Path getConfigFile() {
        return configFile;
    }

    /**
     * Reset configuration to defaults
     */
    public void resetConfig() throws IOException {
        OnePieceConfig config = createDefaultConfig();
        saveConfig(config);
    }

    /**
     * Get cached config or load from file
     */
    private OnePieceConfig getCachedOrLoadConfig() {
        if (cachedConfig == null) {
            return loadConfig();
        }
        return cachedConfig;
    }

    /**
     * Create default configuration
     */
    private OnePieceConfig createDefaultConfig() {
        OnePieceConfig config = new OnePieceConfig();
        config.setVersion("1.0.0");
        // AI provider and vault will be null by default
        return config;
    }

    /**
     * Check if config file exists
     */
    public boolean configFileExists() {
        return Files.exists(configFile);
    }
}

// Made with Bob
