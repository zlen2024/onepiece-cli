# AI Provider Configuration - Implementation Summary

## What We're Building

A flexible AI provider configuration system that allows users to:
- Choose between **OpenAI**, **OpenRouter**, or **Custom Provider**
- Configure provider settings through the `onepiece settings` command
- Switch providers without restarting the application
- Store configuration in `~/.onepiece/config.json`

## Key Features

### 1. Provider Selection
Users can select from three provider types:
- **OpenAI**: Direct OpenAI API access (requires OpenAI API key)
- **OpenRouter**: Access to multiple models through OpenRouter (requires OpenRouter API key)
- **Custom Provider**: Use any OpenAI-compatible API endpoint

### 2. Configuration Options

**OpenAI**:
- API Key

**OpenRouter**:
- API Key
- Optional custom headers (HTTP-Referer, X-Title)

**Custom Provider**:
- Base URL
- API Key
- Model Name
- Optional custom headers

### 3. Settings Menu Integration

New menu option in `onepiece settings`:
```
🤖 AI Provider Configuration
  ├── Change Provider
  ├── Update Current Configuration
  ├── Show Configuration
  └── Back
```

## Technical Architecture

### New Components

1. **Model Classes** (`com.nel.onepiece.model.config`)
   - `AIProviderType.java` - Enum for provider types
   - `AIProviderConfig.java` - Provider configuration model
   - `OnePieceConfig.java` - Main configuration model

2. **Services**
   - `ConfigManager.java` - Handles config file I/O
   - `AIProviderService.java` - Creates LangChain4j models dynamically

3. **Updated Components**
   - `SettingsCommand.java` - Add AI provider menu
   - `ProjectAnalyzerService.java` - Use dynamic provider
   - `application.properties` - Support dynamic configuration

### Configuration File Structure

`~/.onepiece/config.json`:
```json
{
  "version": "1.0.0",
  "aiProvider": {
    "type": "OPENAI",
    "apiKey": "sk-...",
    "baseUrl": "https://api.openai.com/v1",
    "modelName": "gpt-4",
    "temperature": 0.7,
    "maxTokens": 2000,
    "headers": {}
  },
  "vault": {
    "url": "https://vault.example.com",
    "token": "hvs...."
  }
}
```

## User Experience Flow

### Configuring a Provider

1. User runs: `onepiece settings`
2. Selects: "🤖 AI Provider Configuration"
3. Selects: "Change Provider"
4. Chooses provider type (OpenAI/OpenRouter/Custom)
5. Enters required configuration:
   - API Key (all providers)
   - Base URL (custom only)
   - Model Name (custom only)
   - Headers (optional, custom/OpenRouter)
6. Configuration saved to `~/.onepiece/config.json`
7. Success message displayed

### Using AI Features

1. User runs any AI-powered command (e.g., `onepiece setup`)
2. System loads provider configuration from `~/.onepiece/config.json`
3. `AIProviderService` creates appropriate LangChain4j model
4. AI features work with configured provider
5. If configuration missing/invalid, clear error message shown

## Implementation Phases

### Phase 1: Foundation (Models & Config)
- Create enum and model classes
- Implement ConfigManager for file I/O
- Set up configuration persistence

### Phase 2: Settings UI
- Update SettingsCommand with new menu
- Implement provider selection flow
- Add configuration input forms for each provider

### Phase 3: Dynamic Provider Integration
- Create AIProviderService
- Update AI services to use dynamic provider
- Modify application.properties

### Phase 4: Testing & Polish
- Test all three provider types
- Test provider switching
- Add error handling and validation

## Benefits

1. **Flexibility**: Users can choose their preferred AI provider
2. **Cost Control**: Use OpenRouter for access to cheaper models
3. **Privacy**: Use custom/local providers for sensitive projects
4. **No Restart Required**: Switch providers on the fly
5. **Simple Configuration**: Guided setup through interactive menu

## Security Notes

- API keys stored in plain text in `~/.onepiece/config.json`
- File permissions set to 600 (user read/write only)
- Users warned about security implications
- Vault integration recommended for production

## Next Steps

Ready to implement? The plan includes:
- ✅ Detailed architecture diagrams
- ✅ Complete class structure
- ✅ User flow documentation
- ✅ Implementation checklist

**Recommendation**: Switch to Code mode to begin implementation.

---

**Estimated Implementation Time**: 10-14 hours
**Complexity**: Medium
**Risk Level**: Low (non-breaking changes, additive features)