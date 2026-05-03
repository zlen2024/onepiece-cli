package com.nel.onepiece.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * REST client for HashiCorp Vault
 * Implements Bring Your Own Vault (BYOV) approach
 */
@ApplicationScoped
public class VaultClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public VaultClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Test connection to Vault
     */
    public boolean testConnection(String vaultUrl, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(vaultUrl + "/v1/sys/health"))
                .header("X-Vault-Token", token)
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 429; // 429 = sealed but reachable
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Read a secret from Vault
     */
    public Map<String, String> readSecret(String vaultUrl, String token, String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(vaultUrl + "/v1/" + path))
            .header("X-Vault-Token", token)
            .header("Content-Type", "application/json")
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data");
            
            Map<String, String> secrets = new HashMap<>();
            data.fields().forEachRemaining(entry -> 
                secrets.put(entry.getKey(), entry.getValue().asText())
            );
            
            return secrets;
        } else if (response.statusCode() == 404) {
            throw new IOException("Secret not found at path: " + path);
        } else if (response.statusCode() == 403) {
            throw new IOException("Access denied. Check your Vault token permissions.");
        } else {
            throw new IOException("Failed to read secret. Status: " + response.statusCode());
        }
    }

    /**
     * Write a secret to Vault
     */
    public void writeSecret(String vaultUrl, String token, String path, Map<String, String> data) throws IOException, InterruptedException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", data);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(vaultUrl + "/v1/" + path))
            .header("X-Vault-Token", token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .timeout(Duration.ofSeconds(10))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new IOException("Failed to write secret. Status: " + response.statusCode());
        }
    }

    /**
     * Delete a secret from Vault
     */
    public void deleteSecret(String vaultUrl, String token, String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(vaultUrl + "/v1/" + path))
            .header("X-Vault-Token", token)
            .DELETE()
            .timeout(Duration.ofSeconds(10))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new IOException("Failed to delete secret. Status: " + response.statusCode());
        }
    }

    /**
     * List secrets at a path
     */
    public String[] listSecrets(String vaultUrl, String token, String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(vaultUrl + "/v1/" + path + "?list=true"))
            .header("X-Vault-Token", token)
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode keys = root.path("data").path("keys");
            
            if (keys.isArray()) {
                String[] result = new String[keys.size()];
                for (int i = 0; i < keys.size(); i++) {
                    result[i] = keys.get(i).asText();
                }
                return result;
            }
        }
        
        return new String[0];
    }

    /**
     * Get IBM Cloud credentials from Vault
     */
    public IbmCloudCredentials getIbmCloudCredentials(String vaultUrl, String token) throws IOException, InterruptedException {
        Map<String, String> secrets = readSecret(vaultUrl, token, "secret/ibmcloud");
        
        return new IbmCloudCredentials(
            secrets.get("api-key"),
            secrets.getOrDefault("region", "us-south"),
            secrets.get("org"),
            secrets.get("space")
        );
    }

    /**
     * Store IBM Cloud credentials in Vault
     */
    public void storeIbmCloudCredentials(String vaultUrl, String token, IbmCloudCredentials credentials) throws IOException, InterruptedException {
        Map<String, String> data = new HashMap<>();
        data.put("api-key", credentials.apiKey());
        data.put("region", credentials.region());
        
        if (credentials.org() != null) {
            data.put("org", credentials.org());
        }
        if (credentials.space() != null) {
            data.put("space", credentials.space());
        }
        
        writeSecret(vaultUrl, token, "secret/ibmcloud", data);
    }

    /**
     * IBM Cloud credentials record
     */
    public record IbmCloudCredentials(
        String apiKey,
        String region,
        String org,
        String space
    ) {}
}

// Made with Bob
