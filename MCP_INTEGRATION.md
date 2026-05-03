# 🔌 One Piece CLI - MCP Integration Specification

## 1. Overview

Model Context Protocol (MCP) servers provide AI agents with standardized access to external tools and data sources. One Piece CLI provides an **AI-powered conversational interface** to help users configure their development environment through intelligent dialogue.

### Key Principles
- **AI-Guided Configuration:** Conversational AI helps users decide what they need
- **User Choice:** Users decide which MCPs, agents, and skills to configure
- **Prebuilt + Custom:** Support both prebuilt configurations and custom setups
- **Interactive Chat:** Chat-like interface for configuration decisions
- **Version Management:** Track and manage MCP server versions
- **Lifecycle Management:** Install, enable, disable, and update MCPs

---


---

## 2. Interactive Setup Flow (AI-Powered Configuration)

### 2.1 Setup Menu Structure

When user selects **Setup > Bob**, they see:

```
⚙️  Setup - IBM Bob Configuration

? What would you like to configure? (Use arrow keys)
  🤖 Agent   - Configure AI agent behavior and capabilities
  🔌 MCP     - Select and configure Model Context Protocol servers
  🎯 Skills  - Add specialized skills and tools
  ✅ Review  - Review and finalize configuration
  🔙 Back    - Return to main menu
```

### 2.2 Agent Configuration (AI-Guided)

**Flow:** Setup > Bob > Agent

```
🤖 Agent Configuration

Starting AI Configuration Assistant...

┌─────────────────────────────────────────────────────────────┐
│ 🤖 Configuration Agent                                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Hi! I'm here to help you configure IBM Bob for your        │
│ project. Let me ask you a few questions to understand       │
│ what you're building.                                       │
│                                                             │
│ 🔍 I can see you're in a Java project with Quarkus.       │
│                                                             │
│ What type of application are you planning to build?        │
│                                                             │
│ 💬 Type your answer or choose from suggestions:            │
│                                                             │
└─────────────────────────────────────────────────────────────┘

Suggestions:
  🌐 REST API / Microservice
  📱 Full-stack web application  
  🔄 Data processing service
  🤖 AI/ML application
  💾 Database-driven application
  📝 Tell me more about your specific needs...

> _
```

**Conversation Example:**

```
User: REST API for e-commerce

🤖 Configuration Agent:
Great! An e-commerce REST API. Let me configure Bob optimally for this:

📋 Recommended Agent Settings:
• Code Style: Enterprise Java patterns
• Focus Areas: REST endpoints, data validation, security
• Testing: Unit + Integration tests
• Documentation: OpenAPI/Swagger generation

🎯 Specialized Capabilities:
• Database entity modeling
• JWT authentication patterns  
• Payment integration patterns
• Caching strategies

Does this sound right for your project? 
(Y/n) or tell me what to adjust: _
```

### 2.3 MCP Configuration (AI-Guided)

**Flow:** Setup > Bob > MCP

```
🔌 MCP Configuration

Starting MCP Selection Assistant...

┌─────────────────────────────────────────────────────────────┐
│ 🔌 MCP Assistant                                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ I'll help you choose the right MCP servers for your        │
│ development workflow.                                       │
│                                                             │
│ 🔍 Based on your project, I recommend these MCPs:         │
│                                                             │
│ ✅ filesystem-mcp (Essential for file operations)          │
│ ✅ github-mcp (Git repository integration)                 │
│ ✅ maven-mcp (Java build system)                           │
│ ⚠️  postgres-mcp (Database operations)                     │
│                                                             │
│ Do you need database integration for your e-commerce API?  │
│                                                             │
└─────────────────────────────────────────────────────────────┘

Options:
  ✅ Yes, add PostgreSQL MCP
  🔄 Yes, but I prefer MySQL/MariaDB  
  📊 Yes, but I need NoSQL (MongoDB)
  ❌ No database needed right now
  🤔 I'm not sure, help me decide

> _
```

**Advanced MCP Selection:**

```
User: Yes, add PostgreSQL MCP

🔌 MCP Assistant:
Perfect! Adding PostgreSQL support.

🔍 I also noticed some other MCPs that might be useful:

📦 Additional MCPs Available:
• docker-mcp (Container management) - Detected Dockerfile
• aws-mcp (Cloud services) - For deployment
• redis-mcp (Caching) - Common for e-commerce
• stripe-mcp (Payments) - E-commerce specific

Would you like me to add any of these?

Multiple selection (space to select, enter to confirm):
  [ ] docker-mcp
  [ ] aws-mcp  
  [x] redis-mcp
  [ ] stripe-mcp
  [ ] Show me more MCPs...

> _
```

### 2.4 Skills Configuration (AI-Guided)

**Flow:** Setup > Bob > Skills

```
🎯 Skills Configuration

Starting Skills Assistant...

┌─────────────────────────────────────────────────────────────┐
│ 🎯 Skills Assistant                                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Skills are specialized capabilities that enhance Bob's      │
│ performance in specific domains.                            │
│                                                             │
│ 🎯 Recommended Skills for E-commerce REST API:            │
│                                                             │
│ ✅ Java Enterprise Patterns                                │
│ ✅ REST API Design                                         │
│ ✅ Database Schema Design                                  │
│ ✅ Security Best Practices                                 │
│                                                             │
│ Would you like to add any specialized skills?              │
│                                                             │
└─────────────────────────────────────────────────────────────┘

Categories:
  🏗️  Architecture & Design Patterns
  🔐 Security & Authentication  
  📊 Data & Database Design
  🧪 Testing & Quality Assurance
  🚀 DevOps & Deployment
  💰 E-commerce Specific
  🎨 Custom Skills (Define your own)

> _
```

**Skills Selection Interface:**

```
User: E-commerce Specific

🎯 Skills Assistant:

💰 E-commerce Specialized Skills:

Prebuilt Skills:
  [x] Payment Gateway Integration (Stripe, PayPal)
  [x] Shopping Cart Logic  
  [x] Inventory Management
  [x] Order Processing Workflows
  [ ] Multi-tenant Architecture
  [ ] Recommendation Engine
  [ ] Fraud Detection Patterns

Custom Skills:
  📝 Define custom skill...

Selected Skills Preview:
• Payment Gateway Integration
• Shopping Cart Logic  
• Inventory Management
• Order Processing Workflows

💡 These skills will give Bob knowledge of:
   - Payment processing patterns
   - Cart state management
   - Stock tracking algorithms
   - Order lifecycle management


### 2.6 AI Configuration Agent Architecture

The Configuration Agent is powered by LangChain4j and uses conversational AI to guide users through setup.

#### **Configuration Agent Service**

```java
@ApplicationScoped
public class ConfigurationAgentService {
    
    @Inject
    @RestClient
    AiConfigurationAgent aiAgent;
    
    @Inject
    ProjectAnalyzer projectAnalyzer;
    
    @Inject
    McpRegistry mcpRegistry;
    
    @Inject
    SkillsLibrary skillsLibrary;
    
    public ConversationSession startAgentConfiguration(Path projectDir) {
        // Analyze project context
        ProjectContext context = projectAnalyzer.analyze(projectDir);
        
        // Create conversation session
        ConversationSession session = new ConversationSession();
        session.setContext(context);
        session.setPhase(ConfigPhase.AGENT);
        
        // Get initial AI response
        String initialMessage = aiAgent.startAgentConfiguration(context);
        session.addMessage(Role.ASSISTANT, initialMessage);
        
        return session;
    }
    
    public ConversationResponse processUserInput(
        ConversationSession session, 
        String userInput
    ) {
        // Add user message to session
        session.addMessage(Role.USER, userInput);
        
        // Get AI response based on current phase
        String aiResponse = switch (session.getPhase()) {
            case AGENT -> aiAgent.configureAgent(session, userInput);
            case MCP -> aiAgent.configureMcp(session, userInput);
            case SKILLS -> aiAgent.configureSkills(session, userInput);
            case REVIEW -> aiAgent.reviewConfiguration(session, userInput);
        };
        
        // Add AI response to session
        session.addMessage(Role.ASSISTANT, aiResponse);
        
        // Check if phase is complete
        boolean phaseComplete = aiAgent.isPhaseComplete(session);
        
        return new ConversationResponse(aiResponse, phaseComplete);
    }
}
```

#### **AI Agent Interface (LangChain4j)**

```java
@RegisterAiService
public interface AiConfigurationAgent {
    
    @SystemMessage("""
        You are a helpful configuration assistant for One Piece CLI.
        Your role is to help developers configure their AI coding environment.
        
        Guidelines:
        - Ask clear, specific questions
        - Provide intelligent recommendations based on project context
        - Offer prebuilt options but allow customization
        - Be concise but friendly
        - Use emojis sparingly for visual clarity
        - Always confirm understanding before proceeding
        
        Current Phase: Agent Configuration
        """)
    @UserMessage("""
        Project Context:
        - Language: {context.language}
        - Framework: {context.framework}
        - Project Type: {context.projectType}
        - Files: {context.fileCount}
        
        Start the agent configuration conversation.
        """)
    String startAgentConfiguration(ProjectContext context);
    
    @SystemMessage("""
        You are configuring the AI agent's behavior and capabilities.
        Based on the conversation history and user input, provide the next
        appropriate question or confirmation.
        
        When the user has provided enough information, suggest a configuration
        and ask for confirmation.
        """)
    @UserMessage("""
        Conversation History:
        {session.history}
        
        User Input: {userInput}
        
        Provide the next response in the agent configuration conversation.
        """)
    String configureAgent(ConversationSession session, String userInput);
    
    @SystemMessage("""
        You are helping select MCP (Model Context Protocol) servers.
        
        Available MCPs:
        {mcpRegistry.availableMcps}
        
        Recommend MCPs based on:
        - Project type and framework
        - User's stated needs
        - Common patterns for similar projects
        
        Explain what each MCP does in simple terms.
        """)
    @UserMessage("""
        Project Context: {session.context}
        Agent Configuration: {session.agentConfig}
        User Input: {userInput}
        
        Help the user select appropriate MCP servers.
        """)
    String configureMcp(ConversationSession session, String userInput);
    
    @SystemMessage("""
        You are helping select specialized skills for the AI agent.
        
        Available Skills:
        {skillsLibrary.availableSkills}
        
        Recommend skills based on:
        - Project domain (e-commerce, fintech, etc.)
        - Technical requirements
        - User's expertise level
        
        Explain how each skill enhances the agent's capabilities.
        """)
    @UserMessage("""
        Project Context: {session.context}
        Agent Configuration: {session.agentConfig}
        MCP Selection: {session.mcpSelection}
        User Input: {userInput}
        
        Help the user select appropriate skills.
        """)
    String configureSkills(ConversationSession session, String userInput);
    
    @SystemMessage("""
        Review the complete configuration and present it clearly.
        Ask for final confirmation or offer to make adjustments.
        """)
    @UserMessage("""
        Complete Configuration:
        {session.fullConfiguration}
        
        User Input: {userInput}
        
        Review and finalize the configuration.
        """)
    String reviewConfiguration(ConversationSession session, String userInput);
    
    @UserMessage("""
        Based on the conversation history, has the user provided enough
        information to complete the current configuration phase?
        
        Conversation: {session.history}
        
        Answer with just "true" or "false".
        """)
    boolean isPhaseComplete(ConversationSession session);
}
```

#### **Conversation Session Model**

```java
public class ConversationSession {
    private String sessionId;
    private ProjectContext context;
    private ConfigPhase phase;
    private List<Message> messages;
    private AgentConfiguration agentConfig;
    private List<String> selectedMcps;
    private List<String> selectedSkills;
    private Instant createdAt;
    private Instant lastActivity;
    
    public void addMessage(Role role, String content) {
        messages.add(new Message(role, content, Instant.now()));
        this.lastActivity = Instant.now();
    }
    
    public String getHistory() {
        return messages.stream()
            .map(m -> m.role() + ": " + m.content())
            .collect(Collectors.joining("\n"));
    }
    
    public Map<String, Object> getFullConfiguration() {
        return Map.of(
            "agent", agentConfig,
            "mcps", selectedMcps,
            "skills", selectedSkills,
            "context", context
        );
    }
}

public enum ConfigPhase {
    AGENT,
    MCP,
    SKILLS,
    REVIEW
}

public record Message(Role role, String content, Instant timestamp) {}

public enum Role {
    USER,
    ASSISTANT
}
```

### 2.7 Prebuilt vs Custom Configuration

#### **Prebuilt MCP Bundles**

```java
@ApplicationScoped
public class PrebuiltMcpBundles {
    
    public List<McpBundle> getAvailableBundles() {
        return List.of(
            new McpBundle(
                "java-enterprise",
                "Java Enterprise Development",
                List.of("filesystem", "github", "maven", "postgres", "docker"),
                "Complete setup for Java enterprise applications"
            ),
            new McpBundle(
                "nodejs-fullstack",
                "Node.js Full-Stack",
                List.of("filesystem", "github", "npm", "postgres", "redis"),
                "Full-stack JavaScript/TypeScript development"
            ),
            new McpBundle(
                "python-datascience",
                "Python Data Science",
                List.of("filesystem", "github", "pip", "jupyter", "postgres"),
                "Data science and ML development"
            ),
            new McpBundle(
                "minimal",
                "Minimal Setup",
                List.of("filesystem", "github"),
                "Basic file and git operations only"
            )
        );
    }
}
```

#### **Prebuilt Skills Library**

```java
@ApplicationScoped
public class SkillsLibrary {
    
    public Map<String, List<Skill>> getSkillsByCategory() {
        return Map.of(
            "architecture", List.of(
                new Skill("microservices", "Microservices Architecture Patterns"),
                new Skill("event-driven", "Event-Driven Architecture"),
                new Skill("clean-architecture", "Clean Architecture Principles")
            ),
            "security", List.of(
                new Skill("jwt-auth", "JWT Authentication & Authorization"),
                new Skill("oauth2", "OAuth2 & OpenID Connect"),
                new Skill("security-best-practices", "OWASP Security Guidelines")
            ),
            "database", List.of(
                new Skill("schema-design", "Database Schema Design"),
                new Skill("query-optimization", "SQL Query Optimization"),
                new Skill("migrations", "Database Migration Strategies")
            ),
            "ecommerce", List.of(
                new Skill("payment-integration", "Payment Gateway Integration"),
                new Skill("shopping-cart", "Shopping Cart Logic"),
                new Skill("inventory", "Inventory Management"),
                new Skill("order-processing", "Order Processing Workflows")
            ),
            "testing", List.of(
                new Skill("unit-testing", "Unit Testing Best Practices"),
                new Skill("integration-testing", "Integration Testing Patterns"),
                new Skill("tdd", "Test-Driven Development")
            )
        );
    }
    
    public Skill createCustomSkill(String name, String description, String knowledge) {
        return new Skill(
            name,
            description,
            knowledge,
            true // isCustom
        );
    }
}
```

#### **Custom Skills Definition**

Users can define custom skills through the chat interface:

```
User: I need a custom skill for handling cryptocurrency payments

🎯 Skills Assistant:

Great! Let's create a custom skill for cryptocurrency payments.

📝 Custom Skill Definition:

Name: cryptocurrency-payments
Description: Cryptocurrency payment processing and wallet integration

What specific knowledge should this skill include?

Suggestions:
  • Blockchain integration patterns
  • Wallet connection (MetaMask, WalletConnect)
  • Transaction signing and verification
  • Gas fee estimation
  • Multi-chain support (Ethereum, Polygon, BSC)

Type your requirements or select from suggestions: _
```

The custom skill is then saved to the configuration:

```json
{
  "customSkills": [
    {
      "name": "cryptocurrency-payments",
      "description": "Cryptocurrency payment processing and wallet integration",
      "knowledge": [
        "Blockchain integration patterns using Web3.js and Ethers.js",
        "Wallet connection protocols (MetaMask, WalletConnect)",
        "Transaction signing and verification best practices",
        "Gas fee estimation and optimization",
        "Multi-chain support for Ethereum, Polygon, and BSC",
        "Smart contract interaction patterns",
        "Security considerations for crypto transactions"
      ],
      "isCustom": true,
      "createdAt": "2026-05-02T17:30:00Z"
    }
  ]
}
```

Continue with selection? (Y/n/modify): _
```

### 2.5 Configuration Review & Finalization

**Flow:** Setup > Bob > Review

```
✅ Configuration Review

┌─────────────────────────────────────────────────────────────┐
│ 📋 IBM Bob Configuration Summary                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 🤖 Agent Configuration:                                    │
│    • Type: E-commerce REST API Developer                   │
│    • Style: Enterprise Java Patterns                       │
│    • Focus: Security, Performance, Testing                 │
│                                                             │
│ 🔌 MCP Servers (5 selected):                              │
│    • filesystem-mcp v1.2.0                                │
│    • github-mcp v2.0.1                                    │
│    • maven-mcp v1.0.0                                     │
│    • postgres-mcp v1.5.3                                  │
│    • redis-mcp v2.1.0                                     │
│                                                             │
│ 🎯 Skills (7 selected):                                   │
│    • Java Enterprise Patterns                              │
│    • REST API Design                                       │
│    • Database Schema Design                                │
│    • Security Best Practices                               │
│    • Payment Gateway Integration                           │
│    • Shopping Cart Logic                                   │
│    • Order Processing Workflows                            │
│                                                             │
│ 📁 Generated Files:                                        │
│    • .bob.workspace (Agent configuration)                  │
│    • .onepiece/mcp-registry.json (MCP servers)            │
│    • .onepiece/skills-config.json (Skills library)        │
│                                                             │
└─────────────────────────────────────────────────────────────┘

Actions:
  ✅ Generate Configuration
  ✏️  Modify Agent Settings
  🔌 Modify MCP Selection  
  🎯 Modify Skills Selection
  🔙 Back to Setup Menu

> _
```

## 2. Supported MCP Servers (POC Scope)

### 2.1 Core MCPs (Always Recommended)

#### **filesystem-mcp**
- **Purpose:** File system operations (read, write, search, list)
- **Use Cases:** Code generation, file manipulation, project scaffolding
- **Repository:** `https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem`
- **Installation:** `npm install -g @modelcontextprotocol/server-filesystem`
- **Configuration:**
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/project"],
      "env": {
        "ALLOWED_PATHS": "/path/to/project"
      }
    }
  }
}
```

#### **github-mcp**
- **Purpose:** GitHub API integration (repos, issues, PRs, commits)
- **Use Cases:** Issue tracking, PR creation, repository management
- **Repository:** `https://github.com/modelcontextprotocol/servers/tree/main/src/github`
- **Installation:** `npm install -g @modelcontextprotocol/server-github`
- **Configuration:**
```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "${GITHUB_TOKEN}"
      }
    }
  }
}
```

### 2.2 Framework-Specific MCPs

#### **maven-mcp** (Java/Quarkus/Spring)
- **Purpose:** Maven dependency management, build lifecycle
- **Use Cases:** Dependency resolution, build automation, artifact management
- **Detection Trigger:** Presence of `pom.xml`
- **Custom Implementation:** Required (not in official MCP registry yet)
- **Proposed Configuration:**
```json
{
  "mcpServers": {
    "maven": {
      "command": "java",
      "args": ["-jar", "maven-mcp-server.jar"],
      "env": {
        "MAVEN_HOME": "${MAVEN_HOME}",
        "PROJECT_DIR": "/path/to/project"
      }
    }
  }
}
```

#### **gradle-mcp** (Java/Kotlin)
- **Purpose:** Gradle build system integration
- **Detection Trigger:** Presence of `build.gradle` or `build.gradle.kts`
- **Custom Implementation:** Required

#### **npm-mcp** (Node.js/JavaScript)
- **Purpose:** NPM package management
- **Detection Trigger:** Presence of `package.json`
- **Repository:** `https://github.com/modelcontextprotocol/servers/tree/main/src/npm`

### 2.3 Database MCPs

#### **postgres-mcp**
- **Purpose:** PostgreSQL database operations
- **Use Cases:** Schema management, data queries, migrations
- **Repository:** `https://github.com/modelcontextprotocol/servers/tree/main/src/postgres`
- **Installation:** `npm install -g @modelcontextprotocol/server-postgres`
- **Configuration:**
```json
{
  "mcpServers": {
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "POSTGRES_CONNECTION_STRING": "postgresql://user:pass@localhost:5432/db"
      }
    }
  }
}
```

#### **sqlite-mcp**
- **Purpose:** SQLite database operations
- **Detection Trigger:** Presence of `*.db` or `*.sqlite` files
- **Repository:** `https://github.com/modelcontextprotocol/servers/tree/main/src/sqlite`

### 2.4 Cloud Provider MCPs

#### **aws-mcp**
- **Purpose:** AWS service integration (S3, Lambda, DynamoDB, etc.)
- **Use Cases:** Cloud resource management, deployment automation
- **Repository:** `https://github.com/modelcontextprotocol/servers/tree/main/src/aws`

#### **ibmcloud-mcp** (POC Priority)
- **Purpose:** IBM Cloud service integration
- **Custom Implementation:** Required for POC
- **Capabilities:**
  - Cloud Foundry app management
  - Code Engine deployment
  - Kubernetes cluster operations
  - Object Storage operations

### 2.5 Development Tool MCPs

#### **git-mcp**
- **Purpose:** Git operations (commit, branch, merge, diff)
- **Repository:** `https://github.com/modelcontextprotocol/servers/tree/main/src/git`
- **Installation:** `npm install -g @modelcontextprotocol/server-git`

#### **docker-mcp**
- **Purpose:** Docker container management
- **Detection Trigger:** Presence of `Dockerfile` or `docker-compose.yml`
- **Custom Implementation:** Required

---

## 3. MCP Detection & Selection Logic

### 3.1 Auto-Detection Algorithm

```
1. Scan project directory for indicator files
2. Analyze file extensions and project structure
3. Read configuration files (pom.xml, package.json, etc.)
4. Determine framework and language
5. Recommend MCPs based on detection results
6. Allow user to confirm or modify selection
```

### 3.2 Detection Rules

| MCP Server | Detection Triggers | Priority |
|------------|-------------------|----------|
| filesystem | Always | Critical |
| github | `.git/` directory exists | High |
| maven | `pom.xml` exists | High |
| gradle | `build.gradle` or `build.gradle.kts` exists | High |
| npm | `package.json` exists | High |
| postgres | `application.properties` contains postgres config | Medium |
| sqlite | `*.db` or `*.sqlite` files exist | Medium |
| docker | `Dockerfile` or `docker-compose.yml` exists | Medium |
| git | `.git/` directory exists | Medium |
| ibmcloud | User selects IBM Cloud deployment | Low |

### 3.3 Detection Implementation

```java
@ApplicationScoped
public class McpDetectionService {
    
    public List<McpRecommendation> detectMcps(Path projectDir) {
        List<McpRecommendation> recommendations = new ArrayList<>();
        
        // Always recommend filesystem
        recommendations.add(new McpRecommendation(
            "filesystem", 
            McpPriority.CRITICAL, 
            "Required for file operations"
        ));
        
        // Check for Git
        if (Files.exists(projectDir.resolve(".git"))) {
            recommendations.add(new McpRecommendation(
                "github", 
                McpPriority.HIGH, 
                "Git repository detected"
            ));
            recommendations.add(new McpRecommendation(
                "git", 
                McpPriority.MEDIUM, 
                "Git operations support"
            ));
        }
        
        // Check for Maven
        if (Files.exists(projectDir.resolve("pom.xml"))) {
            recommendations.add(new McpRecommendation(
                "maven", 
                McpPriority.HIGH, 
                "Maven project detected"
            ));
        }
        
        // Check for Node.js
        if (Files.exists(projectDir.resolve("package.json"))) {
            recommendations.add(new McpRecommendation(
                "npm", 
                McpPriority.HIGH, 
                "Node.js project detected"
            ));
        }
        
        // Check for Docker
        if (Files.exists(projectDir.resolve("Dockerfile"))) {
            recommendations.add(new McpRecommendation(
                "docker", 
                McpPriority.MEDIUM, 
                "Docker configuration found"
            ));
        }
        
        // Check for databases
        detectDatabases(projectDir, recommendations);
        
        return recommendations;
    }
    
    private void detectDatabases(Path projectDir, List<McpRecommendation> recommendations) {
        // Check application.properties for database config
        Path appProps = projectDir.resolve("src/main/resources/application.properties");
        if (Files.exists(appProps)) {
            try {
                String content = Files.readString(appProps);
                if (content.contains("postgresql")) {
                    recommendations.add(new McpRecommendation(
                        "postgres", 
                        McpPriority.MEDIUM, 
                        "PostgreSQL configuration detected"
                    ));
                }
            } catch (IOException e) {
                // Log and continue
            }
        }
        
        // Check for SQLite files
        try (Stream<Path> paths = Files.walk(projectDir, 2)) {
            boolean hasSqlite = paths.anyMatch(p -> 
                p.toString().endsWith(".db") || p.toString().endsWith(".sqlite")
            );
            if (hasSqlite) {
                recommendations.add(new McpRecommendation(
                    "sqlite", 
                    McpPriority.MEDIUM, 
                    "SQLite database files found"
                ));
            }
        } catch (IOException e) {
            // Log and continue
        }
    }
}
```

---

## 4. MCP Installation & Registration

### 4.1 Installation Process

```mermaid
graph TD
    A[Detect Required MCPs] --> B{MCP Installed?}
    B -->|No| C[Download/Install MCP]
    B -->|Yes| D{Version Compatible?}
    C --> E[Verify Installation]
    D -->|No| F[Update MCP]
    D -->|Yes| G[Register MCP]
    E --> G
    F --> G
    G --> H[Generate Configuration]
    H --> I[Test Connection]
    I --> J{Success?}
    J -->|Yes| K[Mark as Active]
    J -->|No| L[Show Error & Retry]
    L --> C
```

### 4.2 Installation Methods

#### **NPM-based MCPs**
```java
public class NpmMcpInstaller implements McpInstaller {
    
    @Override
    public InstallationResult install(McpDefinition mcp) {
        String packageName = mcp.getNpmPackage();
        
        // Check if already installed
        ProcessResult checkResult = executeCommand(
            "npm", "list", "-g", packageName
        );
        
        if (checkResult.exitCode() == 0) {
            return InstallationResult.alreadyInstalled(mcp);
        }
        
        // Install globally
        ProcessResult installResult = executeCommand(
            "npm", "install", "-g", packageName
        );
        
        if (installResult.exitCode() == 0) {
            return InstallationResult.success(mcp);
        } else {
            return InstallationResult.failure(mcp, installResult.stderr());
        }
    }
}
```

#### **Custom JAR-based MCPs**
```java
public class JarMcpInstaller implements McpInstaller {
    
    @Override
    public InstallationResult install(McpDefinition mcp) {
        String downloadUrl = mcp.getDownloadUrl();
        Path installDir = getInstallDirectory(mcp);
        
        // Download JAR
        Path jarPath = downloadFile(downloadUrl, installDir);
        
        // Verify JAR integrity
        if (!verifyChecksum(jarPath, mcp.getChecksum())) {
            return InstallationResult.failure(mcp, "Checksum verification failed");
        }
        
        // Make executable (if needed)
        makeExecutable(jarPath);
        
        return InstallationResult.success(mcp);
    }
    
    private Path getInstallDirectory(McpDefinition mcp) {
        return Path.of(System.getProperty("user.home"))
            .resolve(".onepiece")
            .resolve("mcp-servers")
            .resolve(mcp.getName());
    }
}
```

### 4.3 MCP Registry Structure

**Location:** `~/.onepiece/mcp-registry.json`

```json
{
  "version": "1.0.0",
  "last_updated": "2026-05-02T17:18:00Z",
  "servers": [
    {
      "name": "filesystem",
      "type": "npm",
      "package": "@modelcontextprotocol/server-filesystem",
      "version": "1.2.0",
      "installed": true,
      "install_path": "/usr/local/lib/node_modules/@modelcontextprotocol/server-filesystem",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem"],
      "status": "active",
      "last_verified": "2026-05-02T17:15:00Z"
    },
    {
      "name": "github",
      "type": "npm",
      "package": "@modelcontextprotocol/server-github",
      "version": "2.0.1",
      "installed": true,
      "install_path": "/usr/local/lib/node_modules/@modelcontextprotocol/server-github",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "requires_env": ["GITHUB_PERSONAL_ACCESS_TOKEN"],
      "status": "active",
      "last_verified": "2026-05-02T17:15:00Z"
    },
    {
      "name": "maven",
      "type": "jar",
      "version": "1.0.0",
      "installed": true,
      "install_path": "/home/user/.onepiece/mcp-servers/maven/maven-mcp-server.jar",
      "command": "java",
      "args": ["-jar", "/home/user/.onepiece/mcp-servers/maven/maven-mcp-server.jar"],
      "status": "active",
      "last_verified": "2026-05-02T17:15:00Z"
    },
    {
      "name": "postgres",
      "type": "npm",
      "package": "@modelcontextprotocol/server-postgres",
      "version": "1.5.3",
      "installed": false,
      "status": "available"
    }
  ]
}
```

---

## 5. Agent-Specific Configuration Generation

### 5.1 IBM Bob Configuration (.bob.workspace)

```json
{
  "version": "1.0",
  "workspace": {
    "name": "my-quarkus-app",
    "root": "/path/to/project",
    "language": "java",
    "framework": "quarkus"
  },
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/project"],
      "env": {
        "ALLOWED_PATHS": "/path/to/project"
      }
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "${GITHUB_TOKEN}"
      }
    },
    "maven": {
      "command": "java",
      "args": ["-jar", "/home/user/.onepiece/mcp-servers/maven/maven-mcp-server.jar"],
      "env": {
        "MAVEN_HOME": "${MAVEN_HOME}",
        "PROJECT_DIR": "/path/to/project"
      }
    }
  },
  "settings": {
    "autoSave": true,
    "formatOnSave": true,
    "linting": true
  }
}
```

### 5.2 Claude Code Configuration (.claude/config.json)

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/project"]
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "${GITHUB_TOKEN}"
      }
    }
  }
}
```

### 5.3 Configuration Generator Service

```java
@ApplicationScoped
public class ConfigGeneratorService {
    
    @Inject
    McpRegistryService mcpRegistry;
    
    public void generateBobWorkspace(Path projectDir, List<String> selectedMcps) {
        BobWorkspaceConfig config = new BobWorkspaceConfig();
        
        // Set workspace metadata
        config.setVersion("1.0");
        config.setWorkspace(detectWorkspaceInfo(projectDir));
        
        // Add MCP configurations
        Map<String, McpServerConfig> mcpConfigs = new HashMap<>();
        for (String mcpName : selectedMcps) {
            McpDefinition mcp = mcpRegistry.getMcp(mcpName);
            McpServerConfig serverConfig = createMcpConfig(mcp, projectDir);
            mcpConfigs.put(mcpName, serverConfig);
        }
        config.setMcpServers(mcpConfigs);
        
        // Write to file
        Path configPath = projectDir.resolve(".bob.workspace");
        writeJson(configPath, config);
    }
    
    private McpServerConfig createMcpConfig(McpDefinition mcp, Path projectDir) {
        McpServerConfig config = new McpServerConfig();
        config.setCommand(mcp.getCommand());
        
        // Substitute project-specific paths
        List<String> args = new ArrayList<>(mcp.getArgs());
        args.replaceAll(arg -> arg.replace("${PROJECT_DIR}", projectDir.toString()));
        config.setArgs(args);
        
        // Add environment variables
        if (mcp.getRequiredEnv() != null) {
            Map<String, String> env = new HashMap<>();
            for (String envVar : mcp.getRequiredEnv()) {
                env.put(envVar, "${" + envVar + "}");
            }
            config.setEnv(env);
        }
        
        return config;
    }
}
```

---

## 6. MCP Lifecycle Management

### 6.1 Health Checks

```java
@ApplicationScoped
public class McpHealthService {
    
    @Scheduled(every = "5m")
    public void checkMcpHealth() {
        List<McpDefinition> activeMcps = mcpRegistry.getActiveMcps();
        
        for (McpDefinition mcp : activeMcps) {
            HealthStatus status = pingMcp(mcp);
            
            if (status == HealthStatus.UNHEALTHY) {
                logger.warn("MCP {} is unhealthy, attempting restart", mcp.getName());
                restartMcp(mcp);
            }
            
            mcpRegistry.updateHealthStatus(mcp.getName(), status);
        }
    }
    
    private HealthStatus pingMcp(McpDefinition mcp) {
        // Send health check request to MCP server
        // Implementation depends on MCP protocol
        return HealthStatus.HEALTHY;
    }
}
```

### 6.2 Version Updates

```java
@ApplicationScoped
public class McpUpdateService {
    
    public UpdateResult checkForUpdates(String mcpName) {
        McpDefinition current = mcpRegistry.getMcp(mcpName);
        McpDefinition latest = fetchLatestVersion(mcpName);
        
        if (isNewerVersion(latest.getVersion(), current.getVersion())) {
            return UpdateResult.available(current, latest);
        }
        
        return UpdateResult.upToDate(current);
    }
    
    public void updateMcp(String mcpName) {
        McpDefinition mcp = mcpRegistry.getMcp(mcpName);
        
        // Backup current version
        backupMcp(mcp);
        
        // Install new version
        InstallationResult result = mcpInstaller.install(mcp);
        
        if (result.isSuccess()) {
            mcpRegistry.updateVersion(mcpName, result.getVersion());
        } else {
            // Rollback to backup
            restoreBackup(mcp);
            throw new McpUpdateException("Failed to update " + mcpName);
        }
    }
}
```

---

## 7. Error Handling & Recovery

### 7.1 Common MCP Errors

| Error Type | Cause | Recovery Strategy |
|------------|-------|-------------------|
| Installation Failed | Network issue, missing dependencies | Retry with exponential backoff |
| Connection Timeout | MCP server not responding | Restart MCP server |
| Authentication Failed | Invalid credentials | Prompt user for credentials |
| Version Incompatible | MCP version too old/new | Update or downgrade MCP |
| Permission Denied | Insufficient file/system permissions | Show permission requirements |

### 7.2 Error Recovery Implementation

```java
@ApplicationScoped
public class McpErrorRecovery {
    
    public RecoveryResult recover(McpError error) {
        return switch (error.getType()) {
            case INSTALLATION_FAILED -> retryInstallation(error);
            case CONNECTION_TIMEOUT -> restartMcpServer(error);
            case AUTH_FAILED -> promptForCredentials(error);
            case VERSION_INCOMPATIBLE -> updateMcpVersion(error);
            case PERMISSION_DENIED -> showPermissionHelp(error);
            default -> RecoveryResult.unrecoverable(error);
        };
    }
    
    private RecoveryResult retryInstallation(McpError error) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                InstallationResult result = mcpInstaller.install(error.getMcp());
                
                if (result.isSuccess()) {
                    return RecoveryResult.success();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            retryCount++;
        }
        
        return RecoveryResult.failed("Max retries exceeded");
    }
}
```

---

## 8. Security Considerations

### 8.1 Credential Management

- **Never store credentials in MCP configurations**
- Use environment variable placeholders: `${GITHUB_TOKEN}`
- Fetch actual credentials from Vault at runtime
- Support credential rotation without config changes

### 8.2 MCP Sandboxing

```json
{
  "filesystem": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/project"],
    "env": {
      "ALLOWED_PATHS": "/path/to/project",
      "DENIED_PATHS": "/etc,/root,/home/user/.ssh"
    },
    "permissions": {
      "read": true,
      "write": true,
      "execute": false,
      "network": false
    }
  }
}
```

### 8.3 Verification & Trust

- Verify MCP checksums before installation
- Use signed packages when available
- Maintain allowlist of trusted MCP sources
- Warn users about custom/unverified MCPs

---

## 9. Testing Strategy

### 9.1 Unit Tests

```java
@QuarkusTest
class McpDetectionServiceTest {
    
    @Inject
    McpDetectionService detectionService;
    
    @Test
    void shouldDetectMavenProject() {
        Path projectDir = createTempProject("pom.xml");
        
        List<McpRecommendation> mcps = detectionService.detectMcps(projectDir);
        
        assertTrue(mcps.stream().anyMatch(m -> m.getName().equals("maven")));
    }
    
    @Test
    void shouldDetectGitRepository() {
        Path projectDir = createTempProject(".git/config");
        
        List<McpRecommendation> mcps = detectionService.detectMcps(projectDir);
        
        assertTrue(mcps.stream().anyMatch(m -> m.getName().equals("github")));
    }
}
```

### 9.2 Integration Tests

```java
@QuarkusTest
class McpInstallationIntegrationTest {
    
    @Inject
    McpInstaller mcpInstaller;
    
    @Test
    void shouldInstallFilesystemMcp() {
        McpDefinition mcp = new McpDefinition(
            "filesystem",
            "@modelcontextprotocol/server-filesystem",
            "1.2.0"
        );
        
        InstallationResult result = mcpInstaller.install(mcp);
        
        assertTrue(result.isSuccess());
        assertTrue(Files.exists(result.getInstallPath()));
    }
}
```

---

## 10. Future Enhancements

### 10.1 MCP Marketplace
- Browse available MCPs
- User ratings and reviews
- Community-contributed MCPs
- One-click installation

### 10.2 Custom MCP Development
- MCP SDK for Java
- Template generator for new MCPs
- Testing framework for MCP servers
- Publishing workflow

### 10.3 Advanced Features
- MCP composition (chain multiple MCPs)
- Conditional MCP loading (based on context)
- MCP performance monitoring
- Cost tracking for paid MCPs

---

## 11. Implementation Checklist

- [ ] Implement `McpDetectionService` with auto-detection logic
- [ ] Create `McpInstaller` interface with NPM and JAR implementations
- [ ] Build `McpRegistryService` for tracking installed MCPs
- [ ] Develop `ConfigGeneratorService` for agent-specific configs
- [ ] Implement `McpHealthService` for monitoring
- [ ] Create `McpUpdateService` for version management
- [ ] Build `McpErrorRecovery` for handling failures
- [ ] Add security validation and sandboxing
- [ ] Write comprehensive unit and integration tests
- [ ] Document MCP development guidelines