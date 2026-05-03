# AI Provider Configuration Implementation Plan

## Overview
Implement a flexible AI provider configuration system that allows users to choose between OpenAI, OpenRouter, or a custom provider through the settings menu. The configuration will be persisted in `~/.onepiece/config.json` and dynamically applied at runtime.

## Architecture Design

### 1. Configuration Model Structure

```
~/.onepiece/
├── config.json          # Main configuration file
├── mcp-registry.json    # MCP servers registry
└── project.json         # Project metadata
```

#### config.json Structure
```json
{
  "version": "1.0.0",
  "aiProvider": {
    "type": "OPENAI",  // or "OPENROUTER" or "CUSTOM"
    "config": {
      // Provider-specific configuration
    }
  },
  "vault": {
    "url": "https://vault.example.com",
    "token": "hvs.***"
  }
}
```

### 2. Class Structure

#### Model Classes (com.nel.onepiece.model.config)

**AIProviderType.java** - Enum
```java
public enum AIProviderType {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1"),
    CUSTOM("Custom Provider", null);
    
    private final String displayName;
    private final String defaultBaseUrl;
}
```

**AIProviderConfig.java** - Base class
```java
public class AIProviderConfig {
    private AIProviderType type;
    private String apiKey;
    private String baseUrl;
    private String modelName;
    private Map<String, String> headers;
    private double temperature = 0.7;
    private int maxTokens = 2000;
}
```

**OnePieceConfig.java** - Main config
```java
public class OnePieceConfig {
    private String version = "1.0.0";
    private AIProviderConfig aiProvider;
    private VaultConfig vault;
}
```

#### Service Classes

**ConfigManager.java** (com.nel.onepiece.config)
- Responsibilities:
  - Read/write `~/.onepiece/config.json`
  - Validate configuration
  - Provide default configurations
  - Handle config file creation

**AIProviderService.java** (com.nel.onepiece.ai)
- Responsibilities:
  - Load active provider configuration
  - Create LangChain4j chat model instances dynamically
  - Support provider switching at runtime
  - Handle provider-specific initialization

### 3. Settings Menu Flow

```
Settings Menu
├── 🔐 Vault Configuration (existing)
├── 🤖 AI Provider Configuration (NEW)
│   ├── Select Provider
│   │   ├── 1. OpenAI
│   │   ├── 2. OpenRouter
│   │   └── 3. Custom Provider
│   ├── Configure Selected Provider
│   │   ├── Enter API Key
│   │   ├── [Custom only] Enter Base URL
│   │   ├── [Custom only] Enter Model Name
│   │   └── [Custom only] Add Headers (optional)
│   ├── Test Connection (optional)
│   └── Save Configuration
├── 📋 Show Configuration
└── 🔙 Back to Main Menu
```

### 4. Implementation Steps

#### Phase 1: Model & Configuration Infrastructure
1. Create `AIProviderType` enum
2. Create `AIProviderConfig` model class
3. Create `OnePieceConfig` model class
4. Create `ConfigManager` service for config persistence

#### Phase 2: Settings Menu Integration
5. Update `SettingsCommand` to add AI Provider menu option
6. Implement provider selection UI flow
7. Implement OpenAI configuration flow
8. Implement OpenRouter configuration flow
9. Implement Custom Provider configuration flow

#### Phase 3: Dynamic Provider Integration
10. Create `AIProviderService` for dynamic provider management
11. Update `ProjectAnalyzerAI` to use dynamic provider
12. Update `ProjectAnalyzerService` to inject provider service
13. Modify application.properties for dynamic configuration

#### Phase 4: Testing & Refinement
14. Add provider switching in settings menu
15. Test configuration persistence
16. Test provider switching
17. Add error handling and validation

### 5. Provider-Specific Configurations

#### OpenAI Configuration
```json
{
  "type": "OPENAI",
  "apiKey": "sk-...",
  "baseUrl": "https://api.openai.com/v1",
  "modelName": "gpt-4",
  "temperature": 0.7,
  "maxTokens": 2000
}
```

#### OpenRouter Configuration
```json
{
  "type": "OPENROUTER",
  "apiKey": "sk-or-...",
  "baseUrl": "https://openrouter.ai/api/v1",
  "modelName": "openai/gpt-4",
  "temperature": 0.7,
  "maxTokens": 2000,
  "headers": {
    "HTTP-Referer": "https://onepiece-cli.com",
    "X-Title": "One Piece CLI"
  }
}
```

#### Custom Provider Configuration
```json
{
  "type": "CUSTOM",
  "apiKey": "custom-key",
  "baseUrl": "https://custom-api.example.com/v1",
  "modelName": "custom-model-name",
  "temperature": 0.7,
  "maxTokens": 2000,
  "headers": {
    "X-Custom-Header": "value"
  }
}
```

### 6. LangChain4j Integration Strategy

Instead of using Quarkus LangChain4j extension's automatic configuration, we'll:
1. Programmatically create `ChatLanguageModel` instances
2. Use the provider configuration to set base URL, API key, and headers
3. Inject the dynamically created model into AI services

```java
@ApplicationScoped
public class AIProviderService {
    
    @Inject
    ConfigManager configManager;
    
    public ChatLanguageModel createChatModel() {
        AIProviderConfig config = configManager.getAIProviderConfig();
        
        return OpenAiChatModel.builder()
            .baseUrl(config.getBaseUrl())
            .apiKey(config.getApiKey())
            .modelName(config.getModelName())
            .temperature(config.getTemperature())
            .maxTokens(config.getMaxTokens())
            .customHeaders(config.getHeaders())
            .build();
    }
}
```

### 7. User Experience Flow

#### First Time Setup
1. User runs `onepiece settings`
2. System detects no AI provider configured
3. Prompts: "No AI provider configured. Would you like to configure one now?"
4. Shows provider selection menu
5. Guides through configuration
6. Saves to `~/.onepiece/config.json`

#### Switching Providers
1. User runs `onepiece settings`
2. Selects "AI Provider Configuration"
3. Shows current provider
4. Option to "Change Provider" or "Update Current Configuration"
5. Saves changes and confirms

### 8. Error Handling

- **Missing API Key**: Show clear error message with instructions
- **Invalid Base URL**: Validate URL format before saving
- **Connection Failure**: Fail at runtime with helpful error message
- **Config File Corruption**: Recreate with defaults and warn user

### 9. Security Considerations

- Store API keys in plain text in `~/.onepiece/config.json`
- Set file permissions to user-only (600)
- Warn users about security implications
- Recommend Vault for production use
- Never log API keys

### 10. Testing Strategy

1. **Unit Tests**: Test ConfigManager read/write operations
2. **Integration Tests**: Test provider switching
3. **Manual Tests**: 
   - Configure each provider type
   - Switch between providers
   - Verify AI features work with each provider
   - Test with invalid configurations

## Implementation Priority

### High Priority (MVP)
- ✅ Model classes and enums
- ✅ ConfigManager service
- ✅ Settings menu integration
- ✅ OpenAI configuration flow
- ✅ Basic provider switching

### Medium Priority
- ✅ OpenRouter configuration
- ✅ Custom provider configuration
- ✅ Dynamic LangChain4j integration
- ✅ Configuration validation

### Low Priority (Future Enhancements)
- Connection testing before save
- Multiple provider profiles
- Provider-specific model selection UI
- Advanced header configuration UI
- Configuration import/export

## Dependencies

### Existing
- LangChain4j OpenAI integration
- Jackson for JSON serialization
- Picocli for CLI
- Quarkus Arc for DI

### New (None Required)
All functionality can be implemented with existing dependencies.

## File Changes Summary

### New Files
- `src/main/java/com/nel/onepiece/model/config/AIProviderType.java`
- `src/main/java/com/nel/onepiece/model/config/AIProviderConfig.java`
- `src/main/java/com/nel/onepiece/model/config/OnePieceConfig.java`
- `src/main/java/com/nel/onepiece/config/ConfigManager.java`
- `src/main/java/com/nel/onepiece/ai/AIProviderService.java`

### Modified Files
- `src/main/java/com/nel/onepiece/commands/SettingsCommand.java`
- `src/main/java/com/nel/onepiece/ai/ProjectAnalyzerService.java`
- `src/main/resources/application.properties`

## Timeline Estimate

- Phase 1 (Models & Config): 2-3 hours
- Phase 2 (Settings UI): 3-4 hours
- Phase 3 (Provider Integration): 3-4 hours
- Phase 4 (Testing): 2-3 hours

**Total: 10-14 hours**

## Success Criteria

1. ✅ Users can select between OpenAI, OpenRouter, and Custom providers
2. ✅ Configuration persists in `~/.onepiece/config.json`
3. ✅ AI features work with all three provider types
4. ✅ Users can switch providers without restarting the app
5. ✅ Clear error messages for configuration issues
6. ✅ Settings menu is intuitive and easy to use

---

**Next Steps**: Review this plan and proceed to implementation in Code mode.