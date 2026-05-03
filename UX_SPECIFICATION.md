# 🎨 One Piece CLI - User Experience Specification

## 1. Design Philosophy

**Hybrid Interaction Model:**
- **Interactive Mode (Default):** Beautiful TUI with arrow key navigation, spinners, and colored output
- **Non-Interactive Mode:** Flag-based commands for scripting and CI/CD pipelines
- **Principle:** "Easy for humans, scriptable for machines"

---

## 2. CLI Command Structure

### 2.1 Interactive Mode (No Arguments)

```bash
$ onepiece
```

**Output:**
```
🏴‍☠️ One Piece CLI v1.0.0
The Ultimate AI Environment Bootstrapper

? What would you like to do? (Use arrow keys)
  ⚙️  Setup    - Bootstrap AI agent environment
  🚀 Deploy   - Deploy your project to the cloud
  🔐 Settings - Configure credentials and preferences
  ❌ Exit
```

### 2.2 Non-Interactive Mode (With Flags)

```bash
# Setup commands
$ onepiece setup --agent bob --project-dir . --auto-detect
$ onepiece setup --agent bob --framework quarkus --mcps filesystem,github,database

# Deploy commands
$ onepiece deploy --target ibmcloud --region us-south --app-name my-app
$ onepiece deploy --target ibmcloud --config deploy.json --verbose

# Settings commands
$ onepiece settings --vault-url https://vault.example.com --vault-token hvs.xxx
$ onepiece settings --show
$ onepiece settings --reset

# Utility commands
$ onepiece --version
$ onepiece --help
$ onepiece status  # Show current configuration
```

---

## 3. Interactive Flow Details

### 3.1 Setup Flow

**Step 1: Agent Selection**
```
⚙️  Setup - Bootstrap AI Agent Environment

? Select your AI agent: (Use arrow keys)
❯ 🤖 IBM Bob (Recommended for POC)
  🔮 Claude Code
  🌟 Open Code
  🥧 Pi
```

**Step 2: Project Analysis**
```
🔍 Analyzing project directory...
   📁 Detected: Java Quarkus project
   📦 Found: pom.xml, src/main/java
   🎯 Recommended MCPs: filesystem, github, maven

⏳ Generating configuration with AI... (this may take 10-20 seconds)
```

**Step 3: Progress Indicators**
```
✓ Project structure analyzed
✓ Dependencies detected
⏳ Generating .bob.workspace configuration...
⏳ Registering MCP servers...
  ├─ ✓ filesystem-mcp (v1.2.0)
  ├─ ✓ github-mcp (v2.0.1)
  └─ ⏳ database-mcp (v1.5.3)
```

**Step 4: Confirmation**
```
✅ Setup Complete!

📝 Generated files:
   • .bob.workspace (AI agent configuration)
   • .onepiece/mcp-registry.json (MCP server list)

🚀 Next steps:
   1. Review the generated configuration
   2. Run your AI agent: bob start
   3. When ready to deploy: onepiece deploy

? Would you like to start IBM Bob now? (Y/n)
```

### 3.2 Deploy Flow

**Step 1: Target Selection**
```
🚀 Deploy - Automate Cloud Deployment

? Select deployment target: (Use arrow keys)
❯ ☁️  IBM Cloud (Recommended for POC)
  🪰 Fly.io
  🔙 Back to main menu
```

**Step 2: Configuration**
```
☁️  IBM Cloud Deployment

? Enter your app name: my-quarkus-app
? Select region: (Use arrow keys)
❯ us-south (Dallas)
  us-east (Washington DC)
  eu-gb (London)
  eu-de (Frankfurt)

? Select runtime: (Auto-detected: Java 21)
❯ ✓ Java 21 (Recommended)
  Java 17
  Java 11
```

**Step 3: Credential Retrieval**
```
🔐 Fetching credentials from Vault...
   Vault URL: https://vault.example.com
   ✓ IBM Cloud API Key retrieved
   ✓ Region configuration loaded
```

**Step 4: Deployment Progress**
```
📦 Building application...
   ⏳ Running: mvn clean package -DskipTests
   ✓ Build successful (23.4s)

☁️  Deploying to IBM Cloud...
   ⏳ Authenticating with IBM Cloud
   ✓ Logged in as user@example.com
   ⏳ Pushing application (my-quarkus-app)
   ⏳ Starting application...
   
   Deployment Logs:
   ────────────────────────────────────────
   [2026-05-02 17:15:00] Creating app...
   [2026-05-02 17:15:05] Uploading files...
   [2026-05-02 17:15:30] Starting instances...
   [2026-05-02 17:15:45] App started successfully
   ────────────────────────────────────────

✅ Deployment Complete!

🌐 Your app is live at:
   https://my-quarkus-app.us-south.cf.appdomain.cloud

📊 App Details:
   • Status: Running
   • Instances: 1
   • Memory: 512MB
   • Disk: 1GB
```

### 3.3 Settings Flow

**Initial Setup (No Vault Configured)**
```
🔐 Settings - Configure Credentials

⚠️  No Vault configuration found

One Piece CLI uses HashiCorp Vault to securely manage your cloud credentials.
This follows a "Bring Your Own Vault" (BYOV) approach.

? Do you have a HashiCorp Vault instance? (Y/n)
```

**If Yes:**
```
? Enter your Vault URL: https://vault.example.com
? Enter your Vault token: hvs.***************************

🔍 Validating connection...
   ✓ Connected to Vault successfully
   ✓ Token is valid

💾 Saving configuration to ~/.onepiece/config.json

✅ Vault configured successfully!

? Would you like to test credential retrieval? (Y/n)
```

**If No:**
```
ℹ️  Alternative: Local .env file (Not recommended for production)

For POC purposes, you can store credentials locally.
⚠️  Warning: Credentials will be stored in plain text

? Use local .env file instead? (y/N)
```

**Settings Menu (Vault Already Configured)**
```
🔐 Settings

Current Configuration:
   Vault URL: https://vault.example.com
   Status: ✓ Connected
   Last verified: 2 minutes ago

? What would you like to do? (Use arrow keys)
  🔄 Update Vault configuration
  🧪 Test connection
  📋 Show stored secrets (masked)
  🗑️  Reset configuration
  🔙 Back to main menu
```

---

## 4. Visual Design System

### 4.1 Color Palette (ANSI Colors)

```
✓ Success:  Green (32)
⏳ Progress: Yellow (33)
❌ Error:    Red (31)
ℹ️  Info:     Cyan (36)
⚠️  Warning:  Yellow (33)
🎯 Highlight: Bright Blue (94)
📝 Muted:     Gray (90)
```

### 4.2 Icons & Symbols

```
Status Indicators:
✓ - Success/Complete
✗ - Failed
⏳ - In Progress
⚠️ - Warning
ℹ️ - Information
🎯 - Important/Focus

Categories:
⚙️ - Setup/Configuration
🚀 - Deployment
🔐 - Security/Settings
📁 - Files/Directories
📦 - Packages/Dependencies
🌐 - Network/URLs
☁️ - Cloud Services
```

### 4.3 Progress Indicators

**Spinner (for short operations < 5s):**
```
⠋ Loading...
⠙ Loading...
⠹ Loading...
⠸ Loading...
⠼ Loading...
⠴ Loading...
⠦ Loading...
⠧ Loading...
⠇ Loading...
⠏ Loading...
```

**Progress Bar (for long operations > 5s):**
```
Uploading files... [████████████░░░░░░░░] 60% (12.3 MB / 20.5 MB)
```

**Multi-step Progress:**
```
Setup Progress:
├─ ✓ Analyze project (2.1s)
├─ ✓ Generate configuration (15.3s)
├─ ⏳ Register MCPs (3/5 complete)
└─ ⏳ Validate setup
```

---

## 5. Error Handling & Messages

### 5.1 Error Message Format

```
❌ Error: Failed to connect to Vault

Reason: Connection timeout after 30 seconds
Vault URL: https://vault.example.com

Possible solutions:
  1. Check your network connection
  2. Verify the Vault URL is correct
  3. Ensure Vault is running and accessible
  4. Check firewall settings

? Would you like to retry? (Y/n)
```

### 5.2 Common Error Scenarios

**Missing Dependencies:**
```
❌ Error: IBM Cloud CLI not found

One Piece CLI requires the IBM Cloud CLI to deploy applications.

📥 Install it with:
   curl -fsSL https://clis.cloud.ibm.com/install/linux | sh

Or visit: https://cloud.ibm.com/docs/cli

? Open installation guide in browser? (Y/n)
```

**Invalid Configuration:**
```
⚠️  Warning: Invalid .bob.workspace file

The configuration file appears to be corrupted or incomplete.

? Would you like to regenerate it? (Y/n)
  ❯ Yes, regenerate configuration
    No, let me fix it manually
    Show me what's wrong
```

**Authentication Failure:**
```
❌ Error: IBM Cloud authentication failed

Reason: Invalid API key or expired token

🔐 Credential source: HashiCorp Vault
   Path: secret/ibmcloud/api-key

Troubleshooting:
  1. Verify the API key in your Vault
  2. Check if the key has expired
  3. Ensure proper Vault permissions

? What would you like to do?
  ❯ Update credentials in Vault
    Retry with current credentials
    Use local .env file instead
    Cancel deployment
```

### 5.3 Validation Messages

**Pre-flight Checks:**
```
🔍 Running pre-deployment checks...

✓ Project structure valid
✓ Dependencies resolved
✓ IBM Cloud CLI installed (v2.15.0)
✓ Vault connection active
⚠️  Warning: No tests found (recommended to add tests)
✓ Build configuration valid

All critical checks passed. Ready to deploy!
```

---

## 6. Help & Documentation

### 6.1 Inline Help

```bash
$ onepiece --help
```

**Output:**
```
🏴‍☠️ One Piece CLI v1.0.0
The Ultimate AI Environment Bootstrapper

USAGE:
    onepiece [COMMAND] [OPTIONS]

COMMANDS:
    setup       Bootstrap AI agent environment
    deploy      Deploy your project to the cloud
    settings    Configure credentials and preferences
    status      Show current configuration
    version     Display version information
    help        Show this help message

INTERACTIVE MODE:
    onepiece    Launch interactive menu (no arguments)

EXAMPLES:
    # Interactive mode
    onepiece

    # Setup IBM Bob for current directory
    onepiece setup --agent bob --project-dir .

    # Deploy to IBM Cloud
    onepiece deploy --target ibmcloud --region us-south

    # Configure Vault
    onepiece settings --vault-url https://vault.example.com

For more information, visit: https://github.com/nel/onepiece-cli
```

### 6.2 Command-Specific Help

```bash
$ onepiece setup --help
```

**Output:**
```
⚙️  Setup - Bootstrap AI Agent Environment

USAGE:
    onepiece setup [OPTIONS]

OPTIONS:
    --agent <AGENT>           AI agent to configure [bob, claudecode, opencode, pi]
    --project-dir <PATH>      Project directory (default: current directory)
    --framework <FRAMEWORK>   Force framework detection [quarkus, spring, nodejs, python]
    --mcps <LIST>            Comma-separated MCP list [filesystem,github,database,...]
    --auto-detect            Auto-detect project settings (default: true)
    --no-interactive         Skip interactive prompts
    --output <PATH>          Custom output path for config files

EXAMPLES:
    # Interactive setup
    onepiece setup

    # Setup IBM Bob with auto-detection
    onepiece setup --agent bob --auto-detect

    # Setup with specific MCPs
    onepiece setup --agent bob --mcps filesystem,github,maven

    # Non-interactive setup for CI/CD
    onepiece setup --agent bob --no-interactive --framework quarkus
```

---

## 7. Accessibility & Usability

### 7.1 Keyboard Navigation

- **Arrow Keys:** Navigate menu options
- **Enter:** Select/Confirm
- **Esc:** Go back/Cancel
- **Ctrl+C:** Exit gracefully (with confirmation)
- **Tab:** Auto-complete (where applicable)

### 7.2 Graceful Exit

```
^C
⚠️  Interrupt received

? Are you sure you want to exit? (y/N)
  ❯ No, continue
    Yes, exit now
    Yes, and save current state
```

### 7.3 Verbose Mode

```bash
$ onepiece deploy --verbose
```

**Additional Output:**
```
[DEBUG] Loading configuration from ~/.onepiece/config.json
[DEBUG] Vault URL: https://vault.example.com
[DEBUG] Fetching secret from path: secret/ibmcloud/api-key
[DEBUG] Executing: ibmcloud login --apikey ****** --region us-south
[DEBUG] Command output: Logged in successfully
[DEBUG] Executing: ibmcloud cf push my-app
...
```

---

## 8. Performance Expectations

### 8.1 Startup Time

- **Native Executable:** < 50ms (GraalVM target)
- **JVM Mode:** < 500ms (Quarkus fast startup)

### 8.2 Operation Timeouts

| Operation | Expected Time | Timeout |
|-----------|---------------|---------|
| Project analysis | 1-3s | 10s |
| AI config generation | 10-20s | 60s |
| MCP registration | 2-5s per MCP | 30s |
| Vault connection | 1-2s | 10s |
| Cloud authentication | 3-5s | 30s |
| Application deployment | 30-120s | 300s |

### 8.3 Feedback Timing

- **Immediate (<100ms):** Key presses, menu navigation
- **Quick (<1s):** File operations, validation
- **Show spinner (>1s):** Network calls, AI generation
- **Show progress bar (>5s):** Uploads, builds, deployments

---

## 9. Configuration Files

### 9.1 User Config (~/.onepiece/config.json)

```json
{
  "version": "1.0.0",
  "vault": {
    "url": "https://vault.example.com",
    "token_hash": "sha256:...",
    "last_verified": "2026-05-02T17:15:00Z"
  },
  "preferences": {
    "default_agent": "bob",
    "default_region": "us-south",
    "verbose": false,
    "auto_update": true
  },
  "history": {
    "last_setup": "2026-05-02T16:30:00Z",
    "last_deploy": "2026-05-02T17:00:00Z"
  }
}
```

### 9.2 Project Config (.onepiece/project.json)

```json
{
  "agent": "bob",
  "framework": "quarkus",
  "mcps": [
    {
      "name": "filesystem-mcp",
      "version": "1.2.0",
      "enabled": true
    },
    {
      "name": "github-mcp",
      "version": "2.0.1",
      "enabled": true
    }
  ],
  "deployment": {
    "target": "ibmcloud",
    "region": "us-south",
    "app_name": "my-quarkus-app"
  }
}
```

---

## 10. Success Metrics

### 10.1 User Experience Goals

- **Time to First Setup:** < 2 minutes (including AI generation)
- **Time to First Deploy:** < 5 minutes (from setup to live app)
- **Error Recovery Rate:** > 90% (users can resolve errors with inline help)
- **Command Discoverability:** > 95% (users find commands without docs)

### 10.2 Performance Goals

- **CLI Startup:** < 50ms (native) / < 500ms (JVM)
- **Interactive Response:** < 100ms for all menu actions
- **AI Generation:** < 30s for configuration generation
- **Deployment:** < 3 minutes for typical Quarkus app

---

## 11. Implementation Notes

### 11.1 Required Libraries

```xml
<!-- Picocli for CLI framework -->
<dependency>
    <groupId>info.picocli</groupId>
    <artifactId>picocli</artifactId>
</dependency>

<!-- JLine for interactive terminal -->
<dependency>
    <groupId>org.jline</groupId>
    <artifactId>jline</artifactId>
</dependency>

<!-- Jansi for colored output -->
<dependency>
    <groupId>org.fusesource.jansi</groupId>
    <artifactId>jansi</artifactId>
</dependency>
```

### 11.2 Key Classes to Implement

```java
// Interactive menu system
com.nel.onepiece.ui.InteractiveMenu
com.nel.onepiece.ui.ProgressIndicator
com.nel.onepiece.ui.ColorFormatter

// Error handling
com.nel.onepiece.error.ErrorHandler
com.nel.onepiece.error.ErrorRecovery

// Validation
com.nel.onepiece.validation.PreflightChecker
com.nel.onepiece.validation.ConfigValidator
```

---

## 12. Future Enhancements (Post-POC)

- **AI-Powered Help:** Natural language command suggestions
- **Project Templates:** Quick-start templates for common frameworks
- **Deployment Rollback:** One-command rollback to previous version
- **Multi-Cloud Support:** Deploy to multiple clouds simultaneously
- **Team Collaboration:** Share configurations across team members
- **Telemetry Dashboard:** Web UI for deployment monitoring