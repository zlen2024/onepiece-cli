# AI Provider Configuration Guide

## Overview

One Piece CLI now supports multiple AI providers, giving you flexibility in choosing your preferred AI service. You can configure:

- **OpenAI** - Direct access to GPT-4 and other OpenAI models
- **OpenRouter** - Access to multiple AI models through a single API
- **Custom Provider** - Use any OpenAI-compatible API endpoint

## Quick Start

### 1. Configure Your AI Provider

Run the settings command:

```bash
onepiece settings
```

Select **"🤖 AI Provider Configuration"** from the menu.

### 2. Choose Your Provider

You'll see three options:

1. **OpenAI** - Requires an OpenAI API key
2. **OpenRouter** - Requires an OpenRouter API key
3. **Custom Provider** - Requires base URL, API key, and model name

### 3. Enter Configuration

Follow the prompts to enter your API credentials and configuration.

## Provider-Specific Setup

### OpenAI

**Requirements:**
- OpenAI API key from https://platform.openai.com/api-keys

**Configuration Steps:**
1. Select "OpenAI" from the provider menu
2. Enter your API key when prompted
3. Configuration is saved automatically

**Default Settings:**
- Base URL: `https://api.openai.com/v1`
- Model: `gpt-4`
- Temperature: `0.7`
- Max Tokens: `2000`

**Example:**
```
? Enter your OpenAI API key: sk-proj-abc123...
✅ AI Provider configured successfully!
Provider: OpenAI
Model: gpt-4
```

### OpenRouter

**Requirements:**
- OpenRouter API key from https://openrouter.ai/keys

**Configuration Steps:**
1. Select "OpenRouter" from the provider menu
2. Enter your API key when prompted
3. Optionally add custom headers (HTTP-Referer, X-Title)
4. Configuration is saved automatically

**Default Settings:**
- Base URL: `https://openrouter.ai/api/v1`
- Model: `openai/gpt-4`
- Temperature: `0.7`
- Max Tokens: `2000`

**Optional Headers:**
- `HTTP-Referer`: Your application URL (for OpenRouter analytics)
- `X-Title`: Your application name

**Example:**
```
? Enter your OpenRouter API key: sk-or-v1-abc123...
? Add custom headers? (optional): Yes
? HTTP-Referer (press Enter to skip): https://myapp.com
? X-Title (press Enter to skip): My Application
✅ AI Provider configured successfully!
Provider: OpenRouter
Model: openai/gpt-4
```

### Custom Provider

**Requirements:**
- OpenAI-compatible API endpoint
- API key for authentication
- Model name

**Configuration Steps:**
1. Select "Custom Provider" from the provider menu
2. Enter base URL (e.g., `https://api.example.com/v1`)
3. Enter API key
4. Enter model name
5. Optionally add custom headers
6. Configuration is saved automatically

**Example:**
```
? Enter base URL: https://my-llm-api.example.com/v1
? Enter API key: custom-key-123
? Enter model name: my-custom-model-v1
? Add custom headers? (optional): Yes
? Header name (press Enter to finish): Authorization
? Header value for Authorization: Bearer my-token
? Add another header?: No
✅ AI Provider configured successfully!
Provider: Custom Provider
Model: my-custom-model-v1
```

## Configuration File

Your AI provider configuration is stored in:

```
~/.onepiece/config.json
```

**File Structure:**
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
  },
  "vault": {
    "url": "https://vault.example.com",
    "token": "hvs...."
  }
}
```

**Security:**
- File permissions are set to `600` (user read/write only) on Unix-like systems
- API keys are stored in plain text
- For production use, consider using HashiCorp Vault

## Managing Your Configuration

### View Current Configuration

```bash
onepiece settings
```

Select **"📋 Show stored secrets (masked)"** to see your current configuration with masked API keys.

### Change Provider

1. Run `onepiece settings`
2. Select **"🤖 AI Provider Configuration"**
3. Select **"🔄 Change Provider"**
4. Choose a new provider and configure it

### Update Current Provider

1. Run `onepiece settings`
2. Select **"🤖 AI Provider Configuration"**
3. Select **"⚙️ Update Current Configuration"**
4. Enter new credentials

### Reset Configuration

```bash
onepiece settings --reset
```

Or through the interactive menu:
1. Run `onepiece settings`
2. Select **"🗑️ Reset configuration"**
3. Confirm the reset

## Using AI Features

Once configured, AI features will automatically use your selected provider:

### Project Analysis
```bash
onepiece setup /path/to/project
```

The AI will analyze your project structure and recommend:
- Framework and language detection
- MCP server recommendations
- Project-specific configurations

### Deployment
```bash
onepiece deploy
```

AI-powered deployment assistance with intelligent suggestions.

## Troubleshooting

### "AI provider not configured" Error

**Solution:** Run `onepiece settings` and configure an AI provider.

### API Key Invalid

**Symptoms:** Errors when using AI features
**Solution:** 
1. Verify your API key is correct
2. Check if the key has expired
3. Update configuration: `onepiece settings` → AI Provider Configuration → Update

### Connection Timeout

**Symptoms:** Requests taking too long or timing out
**Solution:**
- Check your internet connection
- Verify the base URL is correct (for custom providers)
- Check if the AI service is experiencing downtime

### Custom Provider Not Working

**Checklist:**
- ✅ Base URL is correct and includes `/v1` if required
- ✅ API key is valid
- ✅ Model name matches what the provider expects
- ✅ Custom headers are correctly formatted
- ✅ Provider is OpenAI-compatible

## Advanced Configuration

### Custom Headers

Custom headers are useful for:
- Additional authentication
- Request tracking
- Provider-specific features

**Example Headers:**
```json
{
  "Authorization": "Bearer additional-token",
  "X-Organization": "my-org-id",
  "X-Request-ID": "unique-id"
}
```

### Temperature and Max Tokens

These settings control AI behavior:

**Temperature** (0.0 - 2.0):
- `0.0` - Deterministic, focused responses
- `0.7` - Balanced (default)
- `1.5+` - Creative, varied responses

**Max Tokens**:
- Controls response length
- Default: `2000`
- Increase for longer responses
- Decrease to save costs

**Note:** These are currently set to defaults. Future versions will allow customization through the settings menu.

## Provider Comparison

| Feature | OpenAI | OpenRouter | Custom |
|---------|--------|------------|--------|
| Setup Difficulty | Easy | Easy | Medium |
| Model Selection | GPT-4, GPT-3.5 | Multiple models | Your choice |
| Cost | OpenAI pricing | Varies by model | Your pricing |
| Reliability | High | High | Depends |
| Custom Headers | No | Yes | Yes |
| Best For | Standard use | Model variety | Self-hosted |

## Security Best Practices

1. **Never commit config.json to version control**
   - Add `~/.onepiece/` to your global `.gitignore`

2. **Use environment-specific keys**
   - Development keys for testing
   - Production keys for deployment

3. **Rotate keys regularly**
   - Update keys every 90 days
   - Immediately rotate if compromised

4. **Use Vault for production**
   - Configure HashiCorp Vault through settings
   - Store sensitive credentials securely

5. **Monitor API usage**
   - Check your provider's dashboard regularly
   - Set up usage alerts

## FAQ

**Q: Can I use multiple providers simultaneously?**
A: No, only one provider can be active at a time. You can switch providers through the settings menu.

**Q: Will my configuration be lost if I reset?**
A: Yes, reset will delete all configuration. Make a backup of `~/.onepiece/config.json` if needed.

**Q: Can I edit the config file directly?**
A: Yes, but use the settings menu for safety. The file is JSON and can be edited with any text editor.

**Q: Does this work with Azure OpenAI?**
A: Yes! Use the Custom Provider option with your Azure OpenAI endpoint.

**Q: What happens if my API key expires?**
A: AI features will fail with authentication errors. Update your key through the settings menu.

**Q: Can I use local LLM models?**
A: Yes! Use the Custom Provider option with your local API endpoint (e.g., Ollama, LM Studio).

## Support

For issues or questions:
1. Check this guide first
2. Review error messages carefully
3. Verify your configuration with `onepiece settings`
4. Check provider status pages
5. Open an issue on GitHub

---

**Version:** 1.0.0  
**Last Updated:** 2026-05-03