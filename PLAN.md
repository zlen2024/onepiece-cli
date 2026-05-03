# 🏴‍☠️ One Piece CLI: The Ultimate AI Environment Bootstrapper

## 1. Project Overview

**One Piece CLI** is a lightning-fast, lightweight terminal application built with Java Quarkus. It acts as a "Project Manager" or pre-flight orchestrator for AI coding agents (like IBM Bob, Claude Code, and Open Code).

### The Problem

Setting up the correct environment, contexts, and Model Context Protocol (MCP) integrations for AI agents is tedious. Developers waste time configuring `.agent` files and managing deployment pipelines before the AI can even start coding.

### The Solution

One Piece CLI automates the entire bootstrapping process. With a single command, it uses an LLM to generate the perfect configuration files, sets up MCPs, and prepares the workspace. Once the AI agent finishes "vibe coding," One Piece CLI takes over to automate the deployment process to the cloud.

---

## 2. Tech Stack

- **Core Framework:** Java Quarkus (Supersonic Subatomic Java)
- **CLI Library:** Picocli & JLine (for interactive menus)
- **AI Integration:** LangChain4j (for intelligent config generation)
- **Secret Management:** HashiCorp Vault (REST Client approach for Bring-Your-Own-Vault)
- **Compilation:** GraalVM (for 0ms startup time Native Executable)

---

## 3. Core Features & User Flow

The CLI features an interactive mode accessible by simply typing `onepiece-cli`. It contains three main menus:

### ⚙️ 3.1. Setup (The Bootstrapper)

Prepares the environment for the selected AI Harness.

**Options:** 
- `> pi`
- `> opencode`
- `> claudecode`
- `> bob` (POC Focus: IBM Bob)

**Action:**
- Scans the current directory (empty or existing project)
- Calls LangChain4j to analyze the requirements
- Generates configuration files (e.g., `.bob.workspace` or `.agent`)
- Registers necessary MCPs (File system, GitHub, Database)

### 🚀 3.2. Deploy (The Automator)

Automates the deployment of the AI-generated code.

**Options:**
- `> ibm cloud`
- `> fly.io` (POC Focus: IBM Cloud)

**Action:**
- Reads the deployment context
- Fetches temporary API keys/credentials securely
- Uses Java ProcessBuilder to execute cloud CLI commands in the background
- Streams deployment logs to the terminal

### 🔐 3.3. Settings (The Vault)

Handles security and credentials using a Bring Your Own Vault (BYOV) approach.

**Action:**
- Prompts user for their HashiCorp Vault URL and Token
- Saves this "master key" locally (`~/.onepiece/config.json`)
- During deployment, the CLI uses this token to fetch IBM Cloud API keys directly from the user's Vault via REST API, ensuring secrets are never hardcoded or permanently stored

---

## 4. Proof of Concept (POC) Scope

To ensure rapid development and validation, the POC will strictly focus on:

- **AI Agent:** IBM Bob
- **Deployment Target:** IBM Cloud
- **Security:** Simple local `.env` storage first, transitioning to BYOV HashiCorp Vault architecture

---

## 5. Development Phases (Roadmap)

### Phase 1: Skeleton & Interactive Menu
- [ ] Initialize Quarkus CLI project with Picocli
- [ ] Integrate JLine for the interactive menu (`> setup`, `> deploy`, `> settings`)
- [ ] Map out dummy commands to ensure routing works

### Phase 2: LangChain4j & Setup Logic
- [ ] Add langchain4j extension
- [ ] Create `AgentSetupService` with `@SystemMessage` to define the AI's role as a config generator
- [ ] Implement file writing logic to create `.bob.workspace` based on LLM structured output

### Phase 3: Deployment & Process Automation
- [ ] Create `DeploymentService`
- [ ] Implement ProcessBuilder logic to run `ibmcloud` CLI commands
- [ ] Capture and stream standard output (stdout) and errors (stderr) to the CLI UI

### Phase 4: Vault Integration (Settings)
- [ ] Create `SettingsService` to write/read local `config.json` (Vault URL & Token)
- [ ] Implement REST Client in Quarkus to fetch secrets from the specified Vault URL during Phase 3 deployment

### Phase 5: Native Compilation
- [ ] Test JVM build
- [ ] Compile to Native Executable using GraalVM (`./mvnw package -Dnative`)
- [ ] Test startup time and binary portability

---

## 6. Target Folder Architecture

```
src/main/java/com/nel/onepiece/
├── OnePieceCommand.java       # Main entry point (Menu Router)
├── commands/
│   ├── SetupCommand.java      # Handles 'setup' logic
│   ├── DeployCommand.java     # Handles 'deploy' logic
│   └── SettingsCommand.java   # Handles 'settings' logic
├── ai/
│   ├── ConfigGenerator.java   # LangChain4j AiService interface
│   └── PromptTemplates.java   # Holds the prompt structures
├── deployment/
│   └── IbmCloudExecutor.java  # ProcessBuilder logic
└── security/
    └── VaultClient.java       # REST API caller for BYOV
```

---

## Next Steps

1. Set up the Quarkus project structure
2. Implement the interactive CLI menu system
3. Integrate LangChain4j for AI-powered configuration generation
4. Build out deployment automation
5. Add HashiCorp Vault integration for secure credential management
