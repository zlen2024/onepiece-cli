package com.nel.onepiece.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for local configuration stored in ~/.onepiece/config.json
 */
@ApplicationScoped
public class LocalConfigManager {

    private final ObjectMapper objectMapper;
    private final Path configDir;
    private final Path configFile;

    public LocalConfigManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        String userHome = System.getProperty("user.home");
        this.configDir = Paths.get(userHome, ".onepiece");
        this.configFile = configDir.resolve("config.json");
    }

    /**
     * Initialize configuration directory
     */
    public void initializeConfigDir() throws IOException {
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }
    }

    /**
     * Check if configuration exists
     */
    public boolean configExists() {
        return Files.exists(configFile);
    }

    /**
     * Load configuration
     */
    @SuppressWarnings("unchecked")
    public UserConfig loadConfig() throws IOException {
        if (!configExists()) {
            return new UserConfig();
        }

        Map<String, Object> data = objectMapper.readValue(configFile.toFile(), Map.class);
        return mapToUserConfig(data);
    }

    /**
     * Save configuration
     */
    public void saveConfig(UserConfig config) throws IOException {
        initializeConfigDir();
        
        Map<String, Object> data = userConfigToMap(config);
        objectMapper.writeValue(configFile.toFile(), data);
    }

    /**
     * Update vault configuration
     */
    public void updateVaultConfig(String vaultUrl, String vaultToken) throws IOException {
        UserConfig config = loadConfig();
        config.vaultUrl = vaultUrl;
        config.vaultToken = hashToken(vaultToken); // Store hashed token
        config.lastVerified = System.currentTimeMillis();
        saveConfig(config);
    }

    /**
     * Get vault configuration
     */
    public VaultConfig getVaultConfig() throws IOException {
        UserConfig config = loadConfig();
        if (config.vaultUrl == null) {
            return null;
        }
        return new VaultConfig(config.vaultUrl, config.vaultToken, config.lastVerified);
    }

    /**
     * Update preferences
     */
    public void updatePreferences(String defaultAgent, String defaultRegion, boolean verbose, boolean autoUpdate) throws IOException {
        UserConfig config = loadConfig();
        config.defaultAgent = defaultAgent;
        config.defaultRegion = defaultRegion;
        config.verbose = verbose;
        config.autoUpdate = autoUpdate;
        saveConfig(config);
    }

    /**
     * Get preferences
     */
    public Preferences getPreferences() throws IOException {
        UserConfig config = loadConfig();
        return new Preferences(
            config.defaultAgent,
            config.defaultRegion,
            config.verbose,
            config.autoUpdate
        );
    }

    /**
     * Record setup history
     */
    public void recordSetup() throws IOException {
        UserConfig config = loadConfig();
        config.lastSetup = System.currentTimeMillis();
        saveConfig(config);
    }

    /**
     * Record deployment history
     */
    public void recordDeployment() throws IOException {
        UserConfig config = loadConfig();
        config.lastDeploy = System.currentTimeMillis();
        saveConfig(config);
    }

    /**
     * Reset configuration
     */
    public void resetConfig() throws IOException {
        if (Files.exists(configFile)) {
            Files.delete(configFile);
        }
    }

    /**
     * Hash token for storage (simple SHA-256)
     */
    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return "sha256:" + hexString.toString();
        } catch (Exception e) {
            return token; // Fallback to plain text if hashing fails
        }
    }

    /**
     * Convert map to UserConfig
     */
    @SuppressWarnings("unchecked")
    private UserConfig mapToUserConfig(Map<String, Object> data) {
        UserConfig config = new UserConfig();
        config.version = (String) data.get("version");
        
        Map<String, Object> vault = (Map<String, Object>) data.get("vault");
        if (vault != null) {
            config.vaultUrl = (String) vault.get("url");
            config.vaultToken = (String) vault.get("token_hash");
            Object lastVerified = vault.get("last_verified");
            config.lastVerified = lastVerified != null ? ((Number) lastVerified).longValue() : 0;
        }
        
        Map<String, Object> prefs = (Map<String, Object>) data.get("preferences");
        if (prefs != null) {
            config.defaultAgent = (String) prefs.get("default_agent");
            config.defaultRegion = (String) prefs.get("default_region");
            config.verbose = Boolean.TRUE.equals(prefs.get("verbose"));
            config.autoUpdate = Boolean.TRUE.equals(prefs.get("auto_update"));
        }
        
        Map<String, Object> history = (Map<String, Object>) data.get("history");
        if (history != null) {
            Object lastSetup = history.get("last_setup");
            Object lastDeploy = history.get("last_deploy");
            config.lastSetup = lastSetup != null ? ((Number) lastSetup).longValue() : 0;
            config.lastDeploy = lastDeploy != null ? ((Number) lastDeploy).longValue() : 0;
        }
        
        return config;
    }

    /**
     * Convert UserConfig to map
     */
    private Map<String, Object> userConfigToMap(UserConfig config) {
        Map<String, Object> data = new HashMap<>();
        data.put("version", config.version != null ? config.version : "1.0.0");
        
        Map<String, Object> vault = new HashMap<>();
        vault.put("url", config.vaultUrl);
        vault.put("token_hash", config.vaultToken);
        vault.put("last_verified", config.lastVerified);
        data.put("vault", vault);
        
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("default_agent", config.defaultAgent);
        prefs.put("default_region", config.defaultRegion);
        prefs.put("verbose", config.verbose);
        prefs.put("auto_update", config.autoUpdate);
        data.put("preferences", prefs);
        
        Map<String, Object> history = new HashMap<>();
        history.put("last_setup", config.lastSetup);
        history.put("last_deploy", config.lastDeploy);
        data.put("history", history);
        
        return data;
    }

    /**
     * Internal configuration model
     */
    private static class UserConfig {
        String version = "1.0.0";
        String vaultUrl;
        String vaultToken;
        long lastVerified;
        String defaultAgent = "bob";
        String defaultRegion = "us-south";
        boolean verbose = false;
        boolean autoUpdate = true;
        long lastSetup;
        long lastDeploy;
    }

    /**
     * Vault configuration record
     */
    public record VaultConfig(String url, String tokenHash, long lastVerified) {}

    /**
     * User preferences record
     */
    public record Preferences(String defaultAgent, String defaultRegion, boolean verbose, boolean autoUpdate) {}
}

// Made with Bob
