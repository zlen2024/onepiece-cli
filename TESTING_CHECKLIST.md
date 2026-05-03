# AI Provider Configuration - Testing Checklist

## Pre-Build Checks

- [x] All model classes created
- [x] ConfigManager service implemented
- [x] AIProviderService created
- [x] SettingsCommand updated with AI provider menu
- [x] ProjectAnalyzerService updated to use dynamic provider
- [x] application.properties updated

## Build Verification

### 1. Compile the Project

```bash
mvn clean compile
```

**Expected:** No compilation errors

### 2. Package the Application

```bash
mvn clean package
```

**Expected:** 
- Build SUCCESS
- JAR file created in `target/`

### 3. Run the Application

```bash
java -jar target/quarkus-app/quarkus-run.jar --help
```

**Expected:** Help message displays correctly

## Functional Testing

### Test 1: Initial Configuration (No Provider)

**Steps:**
1. Delete `~/.onepiece/config.json` if it exists
2. Run `onepiece settings`
3. Select "AI Provider Configuration"

**Expected:**
- Shows "No AI provider configured"
- Displays menu with Change Provider option

### Test 2: Configure OpenAI Provider

**Steps:**
1. Run `onepiece settings`
2. Select "AI Provider Configuration"
3. Select "Change Provider"
4. Select "OpenAI"
5. Enter API key: `sk-test-key-123`

**Expected:**
- Configuration saved successfully
- File created at `~/.onepiece/config.json`
- Shows success message with provider details

**Verify:**
```bash
cat ~/.onepiece/config.json
```
Should contain:
```json
{
  "version": "1.0.0",
  "aiProvider": {
    "type": "OPENAI",
    "apiKey": "sk-test-key-123",
    "baseUrl": "https://api.openai.com/v1",
    "modelName": "gpt-4",
    ...
  }
}
```

### Test 3: View Configuration

**Steps:**
1. Run `onepiece settings`
2. Select "Show stored secrets (masked)"

**Expected:**
- Shows AI Provider configuration
- API key is masked (e.g., `sk-test...123`)
- Shows provider type and model

### Test 4: Configure OpenRouter Provider

**Steps:**
1. Run `onepiece settings`
2. Select "AI Provider Configuration"
3. Select "Change Provider"
4. Select "OpenRouter"
5. Enter API key: `sk-or-v1-test-key`
6. Add custom headers: Yes
7. HTTP-Referer: `https://test.com`
8. X-Title: `Test App`

**Expected:**
- Configuration saved successfully
- Headers included in config file

**Verify:**
```bash
cat ~/.onepiece/config.json
```
Should show OpenRouter configuration with headers.

### Test 5: Configure Custom Provider

**Steps:**
1. Run `onepiece settings`
2. Select "AI Provider Configuration"
3. Select "Change Provider"
4. Select "Custom Provider"
5. Base URL: `https://custom-api.example.com/v1`
6. API key: `custom-key-123`
7. Model name: `custom-model-v1`
8. Add headers: Yes
9. Header name: `X-Custom-Auth`
10. Header value: `Bearer token123`

**Expected:**
- Configuration saved successfully
- All custom values stored correctly

**Verify:**
```bash
cat ~/.onepiece/config.json
```
Should show custom provider configuration.

### Test 6: Update Current Provider

**Steps:**
1. Configure OpenAI provider first
2. Run `onepiece settings`
3. Select "AI Provider Configuration"
4. Select "Update Current Configuration"
5. Enter new API key

**Expected:**
- Configuration updated
- New API key saved
- Provider type remains the same

### Test 7: Switch Between Providers

**Steps:**
1. Configure OpenAI provider
2. Run `onepiece settings` → Show configuration (note OpenAI)
3. Change to OpenRouter provider
4. Run `onepiece settings` → Show configuration (note OpenRouter)
5. Change back to OpenAI
6. Verify configuration persists

**Expected:**
- Each provider switch saves correctly
- Previous configuration is overwritten
- No errors during switches

### Test 8: Configuration Persistence

**Steps:**
1. Configure a provider
2. Exit the application
3. Run `onepiece settings` again
4. View configuration

**Expected:**
- Configuration loads from file
- All settings preserved
- No errors on load

### Test 9: Invalid Input Handling

**Test 9a: Empty API Key**
1. Try to configure provider with empty API key
2. **Expected:** Error message, configuration not saved

**Test 9b: Empty Base URL (Custom Provider)**
1. Try to configure custom provider with empty base URL
2. **Expected:** Error message, configuration not saved

**Test 9c: Empty Model Name (Custom Provider)**
1. Try to configure custom provider with empty model name
2. **Expected:** Error message, configuration not saved

### Test 10: File Permissions (Unix/Linux/Mac)

**Steps:**
1. Configure a provider
2. Check file permissions:
```bash
ls -la ~/.onepiece/config.json
```

**Expected:**
- Permissions: `-rw-------` (600)
- Only user can read/write

### Test 11: AI Features with Configured Provider

**Steps:**
1. Configure OpenAI provider with valid API key
2. Run `onepiece setup /path/to/project`

**Expected:**
- AI analysis works
- Uses configured provider
- No errors about missing configuration

### Test 12: AI Features without Configured Provider

**Steps:**
1. Delete `~/.onepiece/config.json`
2. Run `onepiece setup /path/to/project`

**Expected:**
- Warning message about no AI provider
- Falls back to manual analysis
- Suggests running `onepiece settings`

### Test 13: Reset Configuration

**Steps:**
1. Configure a provider
2. Run `onepiece settings`
3. Select "Reset configuration"
4. Confirm reset
5. Check `~/.onepiece/config.json`

**Expected:**
- Configuration reset to defaults
- AI provider removed
- File still exists but with default values

### Test 14: Configuration Menu Navigation

**Steps:**
1. Navigate through all menu options
2. Use "Back" buttons
3. Test all paths through the menu

**Expected:**
- No crashes
- Smooth navigation
- Back buttons work correctly

### Test 15: Masked API Key Display

**Steps:**
1. Configure provider with API key `sk-proj-1234567890abcdefghijklmnop`
2. View configuration

**Expected:**
- Shows: `sk-proj...mnop` (first 7 chars + last 4 chars)
- Full key never displayed in UI

## Integration Testing

### Test 16: Provider Service Integration

**Steps:**
1. Configure OpenAI provider
2. Verify `AIProviderService.getChatModel()` returns valid model
3. Switch to OpenRouter
4. Verify model is recreated with new configuration

**Expected:**
- Model creation succeeds
- Correct base URL and API key used
- Headers applied correctly

### Test 17: ProjectAnalyzerService Integration

**Steps:**
1. Configure provider
2. Call `ProjectAnalyzerService.analyzeProject()`
3. Verify it uses configured provider

**Expected:**
- Uses dynamic provider
- Falls back gracefully if provider fails
- Error messages are helpful

## Edge Cases

### Test 18: Corrupted Config File

**Steps:**
1. Manually corrupt `~/.onepiece/config.json`
2. Run `onepiece settings`

**Expected:**
- Warning about corrupted file
- Creates new default configuration
- Application doesn't crash

### Test 19: Missing Config Directory

**Steps:**
1. Delete `~/.onepiece/` directory
2. Run `onepiece settings`
3. Configure a provider

**Expected:**
- Directory created automatically
- Configuration saved successfully

### Test 20: Concurrent Access (if applicable)

**Steps:**
1. Open two terminal windows
2. Run `onepiece settings` in both
3. Configure different providers simultaneously

**Expected:**
- Last write wins
- No file corruption
- No crashes

## Performance Testing

### Test 21: Configuration Load Time

**Steps:**
1. Measure time to load configuration
2. Test with various config file sizes

**Expected:**
- Load time < 100ms
- No noticeable delay

### Test 22: Provider Switch Performance

**Steps:**
1. Switch between providers multiple times
2. Measure response time

**Expected:**
- Switch completes < 1 second
- No memory leaks

## Documentation Verification

- [x] AI_PROVIDER_SETUP_GUIDE.md created
- [x] All provider types documented
- [x] Configuration examples provided
- [x] Troubleshooting section included
- [ ] README.md updated with AI provider info

## Regression Testing

### Test 23: Existing Features Still Work

**Verify:**
- [ ] `onepiece setup` works
- [ ] `onepiece deploy` works
- [ ] Vault configuration still works
- [ ] Other settings menu options work
- [ ] Help commands work

## Sign-Off

### Developer Testing
- [ ] All unit tests pass
- [ ] All functional tests pass
- [ ] No compilation errors
- [ ] No runtime errors
- [ ] Code reviewed

### User Acceptance Testing
- [ ] UI is intuitive
- [ ] Error messages are clear
- [ ] Documentation is complete
- [ ] Feature works as expected

### Ready for Release
- [ ] All tests passed
- [ ] Documentation complete
- [ ] No known critical bugs
- [ ] Performance acceptable

---

**Test Date:** _____________  
**Tester:** _____________  
**Version:** 1.0.0  
**Status:** ⬜ PASS | ⬜ FAIL | ⬜ NEEDS REVIEW