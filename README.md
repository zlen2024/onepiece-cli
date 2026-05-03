# 🏴‍☠️ One Piece CLI

The Ultimate AI Environment Bootstrapper - A lightning-fast CLI tool for bootstrapping AI agent environments and automating cloud deployments.

## 🚀 Features

- **⚙️ Setup**: Bootstrap AI agent environments (IBM Bob, Claude Code, Open Code, Pi)
- **🚀 Deploy**: Automate cloud deployments (IBM Cloud, Fly.io)
- **🔐 Settings**: Global credential management in `~/.onepiece/config.json`
- **🎨 Beautiful TUI**: Interactive terminal UI with colored output and progress indicators
- **⚡ Fast**: Built with Quarkus for supersonic startup times
- **🔒 Secure**: Local config permissions (`600` on Unix-like systems) + optional Vault integration

## 📋 Prerequisites

### Required
- **Java 21 or higher** - [Download from Adoptium](https://adoptium.net/)
- **AI Provider API Key** - Required for AI features (see [AI Provider Setup](#-ai-provider-configuration))

### Optional
- Maven 3.8+ (Maven wrapper `mvnw` is included)
- HashiCorp Vault instance (optional)
- IBM Cloud CLI (for deployment features)

### For Native Builds (Advanced Users Only)
- **GraalVM 21+** with native-image tool
- **Visual Studio Build Tools** (Windows) or **GCC** (Linux/Mac)
- **Additional 5-10 minutes** build time

## 🎓 Beginner's Guide

### Step 1: Verify Java Installation

First, ensure you have Java 21 or higher installed:

```bash
# Check Java version
java -version

# You should see output like:
# openjdk version "21.0.x" or higher
```

If Java is not installed or the version is lower than 21:
- **Windows**: Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
- **macOS**: `brew install openjdk@21`
- **Linux**: `sudo apt install openjdk-21-jdk` (Ubuntu/Debian) or `sudo yum install java-21-openjdk` (RHEL/CentOS)

### Step 2: Clone the Repository

```bash
# Clone the repository
git clone https://github.com/nel/onepiece-cli.git

# Navigate to the project directory
cd onepiece-cli
```

### Step 3: Build the Application (JVM Mode - Recommended)

The project includes a Maven wrapper (`mvnw`), so you don't need to install Maven separately.

**Windows:**
```cmd
mvnw.cmd clean package
```

**Linux/macOS:**
```bash
./mvnw clean package
```

This will:
1. Download all dependencies (first time only)
2. Compile the Java code
3. Run tests
4. Create an executable JAR file in `target/quarkus-app/`

**Build time**: 2-5 minutes (first time), 30-60 seconds (subsequent builds)

✅ **JVM mode is the recommended approach** - it's easier to set up, faster to build, and works on all platforms without additional tools.

### Step 4: Run the Application

After building, run the application:

**Windows:**
```cmd
java -jar target\quarkus-app\quarkus-run.jar
```

**Linux/macOS:**
```bash
java -jar target/quarkus-app/quarkus-run.jar
```

**Startup time**: ~500ms (very fast thanks to Quarkus!)

### Step 5: Configure AI Provider

The application supports multiple AI providers. Configure your preferred provider through the settings menu:

```bash
# After building, run settings
java -jar target/quarkus-app/quarkus-run.jar settings
```

Select **"🤖 AI Provider Configuration"** and choose from:
- **OpenAI** - Direct access to GPT-4 ([Get API key](https://platform.openai.com/api-keys))
- **OpenRouter** - Access multiple models ([Get API key](https://openrouter.ai/keys))
- **Custom Provider** - Use any OpenAI-compatible API

Configuration is stored in `~/.onepiece/config.json` and persists across sessions.

📖 **Detailed Guide**: See [AI_PROVIDER_SETUP_GUIDE.md](AI_PROVIDER_SETUP_GUIDE.md) for complete setup instructions.

### Step 6: First Run - Interactive Mode

When you run the application without arguments, you'll see the interactive menu:

```
🏴‍☠️ One Piece CLI v1.0.0
The Ultimate AI Environment Bootstrapper

? What would you like to do?

  ⚙️  Setup    - Bootstrap AI agent environment
  🚀 Deploy   - Deploy your project to the cloud
  🔐 Settings - Configure credentials and preferences
  ❌ Exit
```

Use arrow keys to navigate and Enter to select.

### Step 7: Try Your First Command

Let's try the setup command to analyze a project:

**Windows:**
```cmd
java -jar target\quarkus-app\quarkus-run.jar setup --project-dir . --auto-detect
```

**Linux/macOS:**
```bash
java -jar target/quarkus-app/quarkus-run.jar setup --project-dir . --auto-detect
```

Or use the interactive mode and select "Setup" from the menu.

### Step 7: Running Tests

To ensure everything is working correctly:

**Windows:**
```cmd
mvnw.cmd test
```

**Linux/macOS:**
```bash
./mvnw test
```

**Expected output**: All tests should pass (green checkmarks)

### Step 8: Development Mode (Hot Reload)

For development, use Quarkus dev mode with hot reload:

**Windows:**
```cmd
mvnw.cmd quarkus:dev
```

**Linux/macOS:**
```bash
./mvnw quarkus:dev
```

**Dev mode features**:
- Automatic code reload on save
- Dev UI available at http://localhost:8080/q/dev
- Continuous testing (press 'r' to run tests)
- Press 'h' for help, 'q' to quit

---

## 🔧 Troubleshooting

### Common Issues and Solutions

#### Issue 1: "AI provider not configured" or AI features fail
**Problem**: The application starts but AI features don't work.

**Solution**: Configure an AI provider through the settings menu:
```bash
java -jar target/quarkus-app/quarkus-run.jar settings
```

Select "AI Provider Configuration" and follow the prompts. See [AI_PROVIDER_SETUP_GUIDE.md](AI_PROVIDER_SETUP_GUIDE.md) for detailed instructions.

#### Issue 2: "Java version not supported"
**Problem**: Build fails with Java version error.

**Solution**: Upgrade to Java 21 or higher:
- Download from [Adoptium](https://adoptium.net/)
- Verify: `java -version` should show 21.0.x or higher

#### Issue 3: "mvnw: command not found" (Windows)
**Problem**: Maven wrapper doesn't run.

**Solution**: Use `mvnw.cmd` instead of `mvnw`:
```cmd
mvnw.cmd clean package
```

#### Issue 4: "mvnw: Permission denied" (Linux/macOS)
**Problem**: Maven wrapper is not executable.

**Solution**: Make it executable:
```bash
chmod +x mvnw
./mvnw clean package
```

#### Issue 5: Build fails with "Out of memory"
**Problem**: Maven runs out of memory during build.

**Solution**: Increase Maven memory:

**Windows (PowerShell):**
```powershell
$env:MAVEN_OPTS="-Xmx2048m"
```

**Windows (CMD):**
```cmd
set MAVEN_OPTS=-Xmx2048m
```

**Linux/macOS:**
```bash
export MAVEN_OPTS="-Xmx2048m"
```

#### Issue 6: Tests fail
**Problem**: Unit tests fail during build.

**Solution**:
1. Ensure you're in the project root directory
2. Clean and rebuild:
   ```bash
   ./mvnw clean test
   ```
3. Check if ports 8080-8081 are available
4. Verify OpenAI API key is configured

#### Issue 7: "Cannot find native-image.cmd" (Native Build)
**Problem**: Attempting native build without GraalVM.

**Solution**: Native builds are optional and require additional setup. For most users, **JVM mode is recommended**. If you need native builds, see the "Advanced: Native Compilation" section below.

---

## 🚀 Advanced: Native Compilation (Optional)

Native compilation with GraalVM produces a standalone executable with faster startup (~50ms vs ~500ms) and lower memory footprint. However, it requires additional setup and significantly longer build times.

### When to Use Native Builds

✅ **Use native builds if you need:**
- Extremely fast startup times (<50ms)
- Minimal memory footprint
- Standalone executables without JVM dependency
- Container images with minimal size

❌ **Stick with JVM mode if:**
- You're just getting started
- Build time matters (native builds take 5-10 minutes)
- You don't have GraalVM installed
- You're developing/testing (JVM mode has hot reload)

### Prerequisites for Native Builds

1. **GraalVM 21+** with native-image tool
2. **C/C++ Compiler**:
   - **Windows**: Visual Studio 2022 Build Tools or Visual Studio Community
   - **Linux**: GCC (usually pre-installed)
   - **macOS**: Xcode Command Line Tools

### Step-by-Step: Installing GraalVM (Windows)

1. **Download GraalVM**:
   - Visit https://www.graalvm.org/downloads/
   - Download GraalVM Community Edition 21 for Windows
   - Extract to `C:\graalvm` (or your preferred location)

2. **Set Environment Variables**:
   ```powershell
   # Set GRAALVM_HOME
   [System.Environment]::SetEnvironmentVariable('GRAALVM_HOME', 'C:\graalvm', 'User')
   
   # Add to PATH
   $path = [System.Environment]::GetEnvironmentVariable('Path', 'User')
   [System.Environment]::SetEnvironmentVariable('Path', "$path;C:\graalvm\bin", 'User')
   ```

3. **Install native-image tool**:
   ```cmd
   cd C:\graalvm\bin
   gu.cmd install native-image
   ```

4. **Install Visual Studio Build Tools**:
   - Download from https://visualstudio.microsoft.com/downloads/
   - Install "Desktop development with C++" workload
   - Restart your terminal

5. **Verify Installation**:
   ```cmd
   native-image --version
   ```

### Step-by-Step: Installing GraalVM (Linux/macOS)

**Linux (Ubuntu/Debian):**
```bash
# Install GraalVM using SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21-graalce

# Install native-image
gu install native-image

# Install build tools (if not already installed)
sudo apt-get install build-essential zlib1g-dev
```

**macOS:**
```bash
# Install GraalVM using SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21-graalce

# Install native-image
gu install native-image

# Install Xcode Command Line Tools (if not already installed)
xcode-select --install
```

### Building Native Executable

Once GraalVM is installed and configured:

**Windows:**
```cmd
# Must run from "x64 Native Tools Command Prompt for VS 2022"
mvnw.cmd package -Dnative
```

**Linux/macOS:**
```bash
./mvnw package -Dnative
```

**Build time**: 5-10 minutes (first time), 3-5 minutes (subsequent builds)

The native executable will be created at:
- **Windows**: `target\onepiece-cli-1.0.0-SNAPSHOT-runner.exe`
- **Linux/macOS**: `target/onepiece-cli-1.0.0-SNAPSHOT-runner`

### Running Native Executable

**Windows:**
```cmd
target\onepiece-cli-1.0.0-SNAPSHOT-runner.exe
```

**Linux/macOS:**
```bash
./target/onepiece-cli-1.0.0-SNAPSHOT-runner
```

**Startup time**: ~50ms (10x faster than JVM mode!)

### Native Build Troubleshooting

#### "Cannot find native-image.cmd"
- Verify GRAALVM_HOME is set: `echo %GRAALVM_HOME%`
- Verify native-image is installed: `gu list`
- Install if missing: `gu install native-image`

#### "cl.exe not found" (Windows)
- Install Visual Studio Build Tools
- Run build from "x64 Native Tools Command Prompt for VS 2022"

#### Build fails with "Out of memory"
- Native builds require significant memory (4GB+)
- Close other applications
- Increase system memory if possible

---

## 📚 Quick Reference

### Essential Commands (JVM Mode)

**Windows:**
```cmd
# Build
mvnw.cmd clean package

# Run
java -jar target\quarkus-app\quarkus-run.jar

# Dev mode (hot reload)
mvnw.cmd quarkus:dev

# Run tests
mvnw.cmd test

# Get help
java -jar target\quarkus-app\quarkus-run.jar --help
```

**Linux/macOS:**
```bash
# Build
./mvnw clean package

# Run
java -jar target/quarkus-app/quarkus-run.jar

# Dev mode (hot reload)
./mvnw quarkus:dev

# Run tests
./mvnw test

# Get help
java -jar target/quarkus-app/quarkus-run.jar --help
```

### Command Examples

```bash
# Setup commands
java -jar target/quarkus-app/quarkus-run.jar setup --agent bob --project-dir . --auto-detect
java -jar target/quarkus-app/quarkus-run.jar setup --agent bob --no-interactive

# Deploy commands
java -jar target/quarkus-app/quarkus-run.jar deploy --target ibmcloud --region us-south
java -jar target/quarkus-app/quarkus-run.jar deploy --target ibmcloud --no-interactive

# Settings commands
java -jar target/quarkus-app/quarkus-run.jar settings --show
java -jar target/quarkus-app/quarkus-run.jar settings --reset
```

### Next Steps

1. **Configure AI Provider**: Run `onepiece settings` and configure your AI provider
2. **Try Setup**: Use the setup command to bootstrap an AI agent environment
3. **Explore Commands**: Run with `--help` to see all available options
4. **Read Documentation**: Check out the detailed documentation files in the repository

---

## 🎯 Usage

### Interactive Mode (Default)

Run the application without arguments to launch the interactive menu:

**Windows:**
```cmd
java -jar target\quarkus-app\quarkus-run.jar
```

**Linux/macOS:**
```bash
java -jar target/quarkus-app/quarkus-run.jar
```

This will present you with a beautiful menu:

```
🏴‍☠️ One Piece CLI v1.0.0
The Ultimate AI Environment Bootstrapper

? What would you like to do?

  ⚙️  Setup    - Bootstrap AI agent environment
  🚀 Deploy   - Deploy your project to the cloud
  🔐 Settings - Configure credentials and preferences
  ❌ Exit
```

### Non-Interactive Mode

Use specific commands for scripting and CI/CD:

**Windows:**
```cmd
# Setup commands
java -jar target\quarkus-app\quarkus-run.jar setup --agent bob --project-dir . --auto-detect
java -jar target\quarkus-app\quarkus-run.jar setup --agent bob --no-interactive

# Deploy commands
java -jar target\quarkus-app\quarkus-run.jar deploy --target ibmcloud --region us-south --app-name my-app
java -jar target\quarkus-app\quarkus-run.jar deploy --target ibmcloud --no-interactive
```

**Linux/macOS:**
```bash
# Setup commands
java -jar target/quarkus-app/quarkus-run.jar setup --agent bob --project-dir . --auto-detect
java -jar target/quarkus-app/quarkus-run.jar setup --agent bob --no-interactive

# Deploy commands
java -jar target/quarkus-app/quarkus-run.jar deploy --target ibmcloud --region us-south --app-name my-app
java -jar target/quarkus-app/quarkus-run.jar deploy --target ibmcloud --no-interactive

# Settings commands
java -jar target/quarkus-app/quarkus-run.jar settings --vault-url https://vault.example.com --vault-token hvs.xxx
java -jar target/quarkus-app/quarkus-run.jar settings --show
java -jar target/quarkus-app/quarkus-run.jar settings --reset

# Help
java -jar target/quarkus-app/quarkus-run.jar --help
java -jar target/quarkus-app/quarkus-run.jar setup --help
```

---

## 📖 Commands

### Setup Command

Bootstrap AI agent environment with intelligent configuration generation:

**Windows:**
```cmd
java -jar target\quarkus-app\quarkus-run.jar setup [OPTIONS]
```

**Linux/macOS:**
```bash
java -jar target/quarkus-app/quarkus-run.jar setup [OPTIONS]
```

**Options:**
- `--agent <AGENT>` - AI agent to configure [bob, claudecode, opencode, pi]
- `--project-dir <PATH>` - Project directory (default: current directory)
- `--auto-detect` - Auto-detect project settings (default: true)
- `--no-interactive` - Skip interactive prompts

**What it does:**
1. Analyzes your project structure
2. Uses AI to generate optimal configuration
3. Creates `.bob.workspace` or equivalent config files
4. Registers necessary MCP servers
5. Sets up the environment for your AI agent

### Deploy Command

Automate cloud deployment with a single command:

**Windows:**
```cmd
java -jar target\quarkus-app\quarkus-run.jar deploy [OPTIONS]
```

**Linux/macOS:**
```bash
java -jar target/quarkus-app/quarkus-run.jar deploy [OPTIONS]
```

**Options:**
- `--target <TARGET>` - Deployment target [ibmcloud, flyio]
- `--region <REGION>` - Cloud region (default: us-south)
- `--app-name <NAME>` - Application name
- `--no-interactive` - Skip interactive prompts

**What it does:**
1. Loads reusable credentials from `~/.onepiece/config.json`
2. Builds your application
3. Deploys to the selected cloud provider
4. Streams deployment logs in real-time
5. Provides the live application URL

### Settings Command

Configure AI providers, credentials, and preferences:

**Windows:**
```cmd
java -jar target\quarkus-app\quarkus-run.jar settings [OPTIONS]
```

**Linux/macOS:**
```bash
java -jar target/quarkus-app/quarkus-run.jar settings [OPTIONS]
```

**Options:**
- `--vault-url <URL>` - HashiCorp Vault URL
- `--vault-token <TOKEN>` - HashiCorp Vault token
- `--show` - Show current configuration
- `--reset` - Reset configuration
- `--no-interactive` - Skip interactive prompts

**What it does:**
1. **AI Provider Configuration** - Configure OpenAI, OpenRouter, or custom providers
2. **Vault Configuration** - Configure HashiCorp Vault connection
3. **Credential Management** - Test and manage credentials
4. **User Preferences** - Manage application settings

📖 **AI Provider Setup**: See [AI_PROVIDER_SETUP_GUIDE.md](AI_PROVIDER_SETUP_GUIDE.md) for detailed configuration guide.

---

## 🏗️ Architecture

```
src/main/java/com/nel/onepiece/
├── OnePieceCommand.java       # Main entry point
├── commands/
│   ├── SetupCommand.java      # Setup logic
│   ├── DeployCommand.java     # Deploy logic
│   └── SettingsCommand.java   # Settings logic
├── ai/
│   ├── ConfigGenerator.java   # LangChain4j AI service
│   └── PromptTemplates.java   # AI prompt structures
├── deployment/
│   └── IbmCloudExecutor.java  # Cloud deployment logic
├── security/
│   └── VaultClient.java       # Vault REST client
├── ui/
│   ├── InteractiveMenu.java   # Interactive menu system
│   ├── ColorFormatter.java    # Colored output
│   └── ProgressIndicator.java # Progress indicators
├── config/
│   └── ConfigurationManager.java
└── model/
    └── [Data models]
```

## 🔐 Security

One Piece CLI stores reusable credentials in `~/.onepiece/config.json`:

1. Credentials are stored locally (plain text)
2. File permissions are set to user read/write only (`600`) on Unix-like systems
3. No `.env` / `.env.example` files are generated by the CLI

HashiCorp Vault configuration is supported as an optional advanced feature.

## 🎨 UI Features

- **Colored Output**: Success (green), errors (red), warnings (yellow), info (cyan)
- **Progress Indicators**: Spinners for short operations, progress bars for long ones
- **Interactive Menus**: Arrow key navigation (coming soon)
- **Real-time Logs**: Streaming deployment logs with formatting
- **Multi-step Progress**: Visual tracking of complex operations

## 🚀 Performance

### JVM Mode (Recommended)
- **Startup time**: ~500ms (very fast thanks to Quarkus)
- **Memory footprint**: Moderate (~100-200MB)
- **Build time**: 30-60 seconds
- **Hot reload**: Yes (dev mode)
- **Platform support**: All platforms with Java 21+

### Native Mode (Optional)
- **Startup time**: ~50ms (10x faster)
- **Memory footprint**: Minimal (~50-100MB)
- **Build time**: 5-10 minutes
- **Hot reload**: No
- **Platform support**: Requires GraalVM and C compiler

**Recommendation**: Use JVM mode for development and most production scenarios. Native mode is only needed for specific use cases requiring minimal startup time or memory footprint.

---

## 🧪 Development

### Running in Dev Mode (Hot Reload)

**Windows:**
```cmd
mvnw.cmd quarkus:dev
```

**Linux/macOS:**
```bash
./mvnw quarkus:dev
```

Dev mode features:
- Automatic code reload on save
- Dev UI at http://localhost:8080/q/dev
- Continuous testing
- Press 'h' for help, 'q' to quit

### Running Tests

**Windows:**
```cmd
mvnw.cmd test
```

**Linux/macOS:**
```bash
./mvnw test
```

### Building for Production

See the "Beginner's Guide" section above for detailed build instructions.

---

## 📝 Configuration Files

The CLI generates and manages several configuration files:

- `.bob.workspace` - IBM Bob agent configuration
- `.onepiece/mcp-registry.json` - MCP server registry
- `.onepiece/project.json` - Project metadata
- `~/.onepiece/config.json` - User configuration (AI provider, Vault settings)

### AI Provider Configuration

Configuration is stored in `~/.onepiece/config.json`:

```json
{
  "version": "1.0.0",
  "aiProvider": {
    "type": "OPENAI",
    "apiKey": "sk-...",
    "baseUrl": "https://api.openai.com/v1",
    "modelName": "gpt-4",
    "temperature": 0.7,
    "maxTokens": 2000
  },
  "vault": {
    "url": "https://vault.example.com",
    "token": "hvs...."
  }
}
```

**Supported Providers:**
- **OpenAI** - GPT-4, GPT-3.5, and other OpenAI models
- **OpenRouter** - Access to multiple AI models through a single API
- **Custom** - Any OpenAI-compatible API endpoint

📖 **Full Documentation**: [AI_PROVIDER_SETUP_GUIDE.md](AI_PROVIDER_SETUP_GUIDE.md)

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting PRs.

## 📄 License

MIT License - see LICENSE file for details

## 🙏 Acknowledgments

- Built with [Quarkus](https://quarkus.io/)
- CLI framework: [Picocli](https://picocli.info/)
- Terminal UI: [JLine](https://github.com/jline/jline3)
- AI Integration: [LangChain4j](https://github.com/langchain4j/langchain4j)
- Colored output: [Jansi](https://github.com/fusesource/jansi)

## 📞 Support

For issues and questions, please open an issue on GitHub.

---

Made with ❤️ by the One Piece CLI team
