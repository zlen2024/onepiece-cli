# AI Provider Configuration Architecture

## System Architecture Diagram

```mermaid
graph TB
    subgraph "User Interface Layer"
        CLI[CLI Entry Point<br/>onepiece settings]
        SettingsCmd[SettingsCommand]
        Menu[Interactive Menu]
    end

    subgraph "Configuration Layer"
        ConfigMgr[ConfigManager<br/>Read/Write config.json]
        OnePieceConfig[OnePieceConfig Model]
        AIProviderConfig[AIProviderConfig Model]
        AIProviderType[AIProviderType Enum]
    end

    subgraph "AI Provider Layer"
        AIProviderSvc[AIProviderService<br/>Dynamic Provider Factory]
        OpenAIProvider[OpenAI Provider]
        OpenRouterProvider[OpenRouter Provider]
        CustomProvider[Custom Provider]
    end

    subgraph "AI Services Layer"
        ProjectAnalyzerAI[ProjectAnalyzerAI Interface]
        ProjectAnalyzerSvc[ProjectAnalyzerService]
        ChatModel[LangChain4j ChatLanguageModel]
    end

    subgraph "Storage"
        ConfigFile[~/.onepiece/config.json]
    end

    CLI --> SettingsCmd
    SettingsCmd --> Menu
    Menu --> ConfigMgr
    ConfigMgr --> OnePieceConfig
    OnePieceConfig --> AIProviderConfig
    AIProviderConfig --> AIProviderType
    ConfigMgr <--> ConfigFile
    
    AIProviderSvc --> ConfigMgr
    AIProviderSvc --> OpenAIProvider
    AIProviderSvc --> OpenRouterProvider
    AIProviderSvc --> CustomProvider
    
    OpenAIProvider --> ChatModel
    OpenRouterProvider --> ChatModel
    CustomProvider --> ChatModel
    
    ProjectAnalyzerSvc --> AIProviderSvc
    ProjectAnalyzerAI --> ChatModel
    ProjectAnalyzerSvc --> ProjectAnalyzerAI
```

## Component Interaction Flow

### Configuration Flow

```mermaid
sequenceDiagram
    participant User
    participant SettingsCmd
    participant Menu
    participant ConfigMgr
    participant FileSystem

    User->>SettingsCmd: onepiece settings
    SettingsCmd->>Menu: Show AI Provider option
    User->>Menu: Select AI Provider Configuration
    Menu->>Menu: Show provider selection
    User->>Menu: Select OpenAI/OpenRouter/Custom
    Menu->>Menu: Prompt for configuration details
    User->>Menu: Enter API key, base URL, etc.
    Menu->>ConfigMgr: Save configuration
    ConfigMgr->>FileSystem: Write ~/.onepiece/config.json
    FileSystem-->>ConfigMgr: Success
    ConfigMgr-->>Menu: Configuration saved
    Menu-->>User: Show success message
```

### AI Service Initialization Flow

```mermaid
sequenceDiagram
    participant App
    participant ProjectAnalyzerSvc
    participant AIProviderSvc
    participant ConfigMgr
    participant LangChain4j

    App->>ProjectAnalyzerSvc: Analyze project
    ProjectAnalyzerSvc->>AIProviderSvc: Get chat model
    AIProviderSvc->>ConfigMgr: Load AI provider config
    ConfigMgr-->>AIProviderSvc: Return config
    
    alt OpenAI Provider
        AIProviderSvc->>LangChain4j: Create OpenAI model
    else OpenRouter Provider
        AIProviderSvc->>LangChain4j: Create OpenRouter model
    else Custom Provider
        AIProviderSvc->>LangChain4j: Create custom model
    end
    
    LangChain4j-->>AIProviderSvc: Return ChatLanguageModel
    AIProviderSvc-->>ProjectAnalyzerSvc: Return model
    ProjectAnalyzerSvc->>LangChain4j: Use model for analysis
```

## Data Model Structure

```mermaid
classDiagram
    class OnePieceConfig {
        -String version
        -AIProviderConfig aiProvider
        -VaultConfig vault
        +getAiProvider()
        +setAiProvider()
    }

    class AIProviderConfig {
        -AIProviderType type
        -String apiKey
        -String baseUrl
        -String modelName
        -Map headers
        -double temperature
        -int maxTokens
        +getType()
        +getApiKey()
        +getBaseUrl()
    }

    class AIProviderType {
        <<enumeration>>
        OPENAI
        OPENROUTER
        CUSTOM
        -String displayName
        -String defaultBaseUrl
        +getDisplayName()
        +getDefaultBaseUrl()
    }

    class ConfigManager {
        -ObjectMapper objectMapper
        -Path configPath
        +loadConfig()
        +saveConfig()
        +getAIProviderConfig()
        +updateAIProviderConfig()
    }

    class AIProviderService {
        -ConfigManager configManager
        +createChatModel()
        +getCurrentProvider()
        +switchProvider()
    }

    OnePieceConfig --> AIProviderConfig
    AIProviderConfig --> AIProviderType
    ConfigManager --> OnePieceConfig
    AIProviderService --> ConfigManager
```

## File System Structure

```
~/.onepiece/
├── config.json                 # Main configuration
│   ├── version: "1.0.0"
│   ├── aiProvider:
│   │   ├── type: "OPENAI"
│   │   ├── apiKey: "sk-..."
│   │   ├── baseUrl: "https://..."
│   │   ├── modelName: "gpt-4"
│   │   ├── temperature: 0.7
│   │   ├── maxTokens: 2000
│   │   └── headers: {}
│   └── vault:
│       ├── url: "https://..."
│       └── token: "hvs...."
├── mcp-registry.json          # MCP servers
└── project.json               # Project metadata
```

## Provider Configuration Examples

### OpenAI Configuration
```json
{
  "version": "1.0.0",
  "aiProvider": {
    "type": "OPENAI",
    "apiKey": "sk-proj-...",
    "baseUrl": "https://api.openai.com/v1",
    "modelName": "gpt-4",
    "temperature": 0.7,
    "maxTokens": 2000,
    "headers": {}
  }
}
```

### OpenRouter Configuration
```json
{
  "version": "1.0.0",
  "aiProvider": {
    "type": "OPENROUTER",
    "apiKey": "sk-or-v1-...",
    "baseUrl": "https://openrouter.ai/api/v1",
    "modelName": "openai/gpt-4",
    "temperature": 0.7,
    "maxTokens": 2000,
    "headers": {
      "HTTP-Referer": "https://onepiece-cli.com",
      "X-Title": "One Piece CLI"
    }
  }
}
```

### Custom Provider Configuration
```json
{
  "version": "1.0.0",
  "aiProvider": {
    "type": "CUSTOM",
    "apiKey": "custom-api-key",
    "baseUrl": "https://custom-llm-api.example.com/v1",
    "modelName": "custom-model-v1",
    "temperature": 0.7,
    "maxTokens": 2000,
    "headers": {
      "X-Custom-Auth": "Bearer token",
      "X-Organization": "my-org"
    }
  }
}
```

## Settings Menu Structure

```
┌─────────────────────────────────────────┐
│  🔐 Settings - Configure Credentials    │
├─────────────────────────────────────────┤
│                                         │
│  Current Configuration:                 │
│    AI Provider: OpenAI (gpt-4)         │
│    Vault: Not configured               │
│                                         │
│  ? What would you like to do?          │
│                                         │
│  1. 🤖 AI Provider Configuration       │
│  2. 🔄 Update Vault configuration      │
│  3. 🧪 Test connection                 │
│  4. 📋 Show stored secrets (masked)    │
│  5. 🗑️  Reset configuration            │
│  6. 🔙 Back to main menu               │
│                                         │
└─────────────────────────────────────────┘
```

### AI Provider Configuration Submenu

```
┌─────────────────────────────────────────┐
│  🤖 AI Provider Configuration           │
├─────────────────────────────────────────┤
│                                         │
│  Current Provider: OpenAI              │
│  Model: gpt-4                          │
│  Status: ✓ Configured                  │
│                                         │
│  ? Select an option:                   │
│                                         │
│  1. 🔄 Change Provider                 │
│  2. ⚙️  Update Current Configuration    │
│  3. 📋 Show Configuration              │
│  4. 🔙 Back                            │
│                                         │
└─────────────────────────────────────────┘
```

### Provider Selection Menu

```
┌─────────────────────────────────────────┐
│  🤖 Select AI Provider                  │
├─────────────────────────────────────────┤
│                                         │
│  Choose your AI provider:              │
│                                         │
│  1. 🟢 OpenAI                          │
│     Most popular, GPT-4 support        │
│                                         │
│  2. 🔵 OpenRouter                      │
│     Access multiple models             │
│                                         │
│  3. ⚙️  Custom Provider                │
│     Use your own API endpoint          │
│                                         │
│  4. 🔙 Back                            │
│                                         │
└─────────────────────────────────────────┘
```

## Error Handling Strategy

```mermaid
graph TD
    A[User Action] --> B{Config Exists?}
    B -->|No| C[Create Default Config]
    B -->|Yes| D{Valid JSON?}
    D -->|No| E[Show Error + Recreate]
    D -->|Yes| F{Provider Configured?}
    F -->|No| G[Prompt Configuration]
    F -->|Yes| H{Valid API Key?}
    H -->|No| I[Fail at Runtime]
    H -->|Yes| J[Success]
    
    C --> G
    E --> G
    G --> J
```

## Security Considerations

1. **File Permissions**: Set `~/.onepiece/config.json` to 600 (user read/write only)
2. **API Key Storage**: Plain text in config file (warn users)
3. **Logging**: Never log API keys or sensitive headers
4. **Vault Integration**: Recommend Vault for production environments
5. **Input Validation**: Sanitize all user inputs before saving

## Performance Considerations

1. **Lazy Loading**: Load configuration only when needed
2. **Caching**: Cache ChatLanguageModel instances
3. **Provider Switching**: Minimal overhead, recreate model on switch
4. **File I/O**: Minimize config file reads/writes

---

**Status**: Architecture design complete, ready for implementation