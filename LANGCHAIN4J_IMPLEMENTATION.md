# 🤖 One Piece CLI - LangChain4j Implementation Specification

## 1. Overview

LangChain4j powers the AI-driven configuration experience in One Piece CLI. This document defines the detailed prompt engineering, structured outputs, and implementation patterns for all AI-powered features.

### Key Components
- **Configuration Agent**: Conversational AI for setup guidance
- **Project Analyzer**: AI-powered project understanding
- **Config Generator**: Structured configuration file generation
- **Recommendation Engine**: Intelligent MCP and skill suggestions

---

## 2. LangChain4j Architecture

### 2.1 Dependency Configuration

```xml
<dependencies>
    <!-- LangChain4j Core -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j</artifactId>
        <version>0.35.0</version>
    </dependency>
    
    <!-- Quarkus LangChain4j Extension -->
    <dependency>
        <groupId>io.quarkiverse.langchain4j</groupId>
        <artifactId>quarkus-langchain4j-openai</artifactId>
        <version>0.19.0</version>
    </dependency>
    
    <!-- For structured outputs -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-core</artifactId>
        <version>0.35.0</version>
    </dependency>
    
    <!-- Memory for conversation context -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-memory</artifactId>
        <version>0.35.0</version>
    </dependency>
</dependencies>
```

### 2.2 Application Configuration

```properties
# application.properties

# OpenAI Configuration (default provider)
quarkus.langchain4j.openai.api-key=${OPENAI_API_KEY}
quarkus.langchain4j.openai.chat-model.model-name=gpt-4o
quarkus.langchain4j.openai.chat-model.temperature=0.7
quarkus.langchain4j.openai.chat-model.max-tokens=2000
quarkus.langchain4j.openai.timeout=60s

# Alternative: IBM watsonx.ai
quarkus.langchain4j.watsonx.api-key=${WATSONX_API_KEY}
quarkus.langchain4j.watsonx.project-id=${WATSONX_PROJECT_ID}
quarkus.langchain4j.watsonx.chat-model.model-id=ibm/granite-13b-chat-v2

# Memory Configuration
quarkus.langchain4j.chat-memory.type=message-window
quarkus.langchain4j.chat-memory.max-messages=20

# Logging
quarkus.log.category."dev.langchain4j".level=DEBUG
```

---

## 3. Project Analyzer AI Service

### 3.1 Project Analysis Interface

```java
package com.nel.onepiece.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface ProjectAnalyzerAi {
    
    @SystemMessage("""
        You are an expert software architect analyzing project structures.
        
        Your task is to analyze the provided project information and determine:
        1. Primary programming language
        2. Framework/platform being used
        3. Project type (web app, API, library, etc.)
        4. Key technologies and dependencies
        5. Development patterns and architecture style
        
        Be precise and confident in your analysis. Base conclusions on concrete evidence
        from file structures, configuration files, and dependencies.
        
        Output your analysis as a structured JSON object.
        """)
    @UserMessage("""
        Analyze this project:
        
        Project Directory: {projectPath}
        
        File Structure:
        {fileStructure}
        
        Configuration Files Found:
        {configFiles}
        
        Dependencies (if available):
        {dependencies}
        
        Provide a comprehensive project analysis.
        """)
    ProjectAnalysis analyzeProject(
        @V("projectPath") String projectPath,
        @V("fileStructure") String fileStructure,
        @V("configFiles") String configFiles,
        @V("dependencies") String dependencies
    );
}
```

### 3.2 Project Analysis Output Schema

```java
package com.nel.onepiece.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;

public class ProjectAnalysis {
    
    @Description("Primary programming language (e.g., Java, JavaScript, Python)")
    @JsonProperty("language")
    private String language;
    
    @Description("Framework or platform (e.g., Quarkus, Spring Boot, React, Express)")
    @JsonProperty("framework")
    private String framework;
    
    @Description("Project type (e.g., rest-api, web-app, microservice, library)")
    @JsonProperty("projectType")
    private String projectType;
    
    @Description("List of key technologies detected")
    @JsonProperty("technologies")
    private List<String> technologies;
    
    @Description("Architecture style (e.g., monolithic, microservices, serverless)")
    @JsonProperty("architectureStyle")
    private String architectureStyle;
    
    @Description("Build tool (e.g., Maven, Gradle, npm, pip)")
    @JsonProperty("buildTool")
    private String buildTool;
    
    @Description("Database type if detected (e.g., PostgreSQL, MongoDB, MySQL)")
    @JsonProperty("database")
    private String database;
    
    @Description("Confidence score 0-100")
    @JsonProperty("confidence")
    private int confidence;
    
    @Description("Additional observations or notes")
    @JsonProperty("notes")
    private String notes;
    
    // Getters and setters
}
```

### 3.3 Project Analyzer Service Implementation

```java
package com.nel.onepiece.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectAnalyzerService {
    
    @Inject
    ProjectAnalyzerAi analyzerAi;
    
    public ProjectAnalysis analyze(Path projectDir) {
        // Gather project information
        String fileStructure = buildFileStructure(projectDir);
        String configFiles = findConfigFiles(projectDir);
        String dependencies = extractDependencies(projectDir);
        
        // Call AI for analysis
        return analyzerAi.analyzeProject(
            projectDir.toString(),
            fileStructure,
            configFiles,
            dependencies
        );
    }
    
    private String buildFileStructure(Path projectDir) {
        try (var paths = Files.walk(projectDir, 3)) {
            return paths
                .filter(Files::isRegularFile)
                .map(p -> projectDir.relativize(p).toString())
                .limit(100) // Limit to avoid token overflow
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Error reading file structure";
        }
    }
    
    private String findConfigFiles(Path projectDir) {
        List<String> configPatterns = List.of(
            "pom.xml", "build.gradle", "package.json", "requirements.txt",
            "application.properties", "application.yml", "Dockerfile",
            "docker-compose.yml", ".env.example"
        );
        
        return configPatterns.stream()
            .map(projectDir::resolve)
            .filter(Files::exists)
            .map(p -> {
                try {
                    String content = Files.readString(p);
                    return p.getFileName() + ":\n" + 
                           content.substring(0, Math.min(content.length(), 500));
                } catch (Exception e) {
                    return p.getFileName() + ": (unreadable)";
                }
            })
            .collect(Collectors.joining("\n\n"));
    }
    
    private String extractDependencies(Path projectDir) {
        // Extract from pom.xml, package.json, etc.
        // Implementation depends on project type
        return ""; // Simplified for brevity
    }
}
```

---

## 4. Configuration Agent AI Service

### 4.1 Agent Configuration Interface

```java
package com.nel.onepiece.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface ConfigurationAgentAi {
    
    @SystemMessage("""
        You are a friendly and knowledgeable AI assistant helping developers configure
        their development environment for AI coding agents.
        
        Your personality:
        - Helpful and patient
        - Clear and concise
        - Professional but approachable
        - Use emojis sparingly (only for visual clarity)
        
        Your approach:
        - Ask one clear question at a time
        - Provide 3-4 specific suggestions when appropriate
        - Explain technical terms in simple language
        - Confirm understanding before proceeding
        - Adapt to user's expertise level
        
        Current Phase: {phase}
        
        Guidelines for this phase:
        {phaseGuidelines}
        """)
    @UserMessage("""
        Project Context:
        {projectContext}
        
        Conversation History:
        {conversationHistory}
        
        User's Latest Input:
        {userInput}
        
        Provide the next appropriate response in the conversation.
        Keep responses under 200 words.
        """)
    String chat(
        @MemoryId String sessionId,
        @V("phase") String phase,
        @V("phaseGuidelines") String phaseGuidelines,
        @V("projectContext") String projectContext,
        @V("conversationHistory") String conversationHistory,
        @V("userInput") String userInput
    );
    
    @SystemMessage("""
        Based on the conversation history, determine if the user has provided
        enough information to complete the current configuration phase.
        
        A phase is complete when:
        - User has answered all essential questions
        - User has confirmed their choices
        - No critical information is missing
        
        Answer with ONLY "COMPLETE" or "INCOMPLETE" followed by a brief reason.
        """)
    @UserMessage("""
        Phase: {phase}
        Conversation History:
        {conversationHistory}
        
        Is this phase complete?
        """)
    PhaseCompletionStatus checkPhaseCompletion(
        @V("phase") String phase,
        @V("conversationHistory") String conversationHistory
    );
}
```

### 4.2 Phase Guidelines Templates

```java
package com.nel.onepiece.ai;

public class PhaseGuidelines {
    
    public static final String AGENT_PHASE = """
        You are helping configure the AI agent's behavior and capabilities.
        
        Key questions to ask:
        1. What type of application are they building?
        2. What are their main development goals?
        3. What coding style do they prefer?
        4. What level of assistance do they need?
        
        Provide recommendations based on:
        - Project type (API, web app, library, etc.)
        - Framework detected
        - Common patterns for similar projects
        
        Suggest specific agent configurations like:
        - Code style preferences (enterprise, modern, minimal)
        - Focus areas (performance, security, testing)
        - Documentation level (minimal, standard, comprehensive)
        """;
    
    public static final String MCP_PHASE = """
        You are helping select Model Context Protocol (MCP) servers.
        
        Explain MCPs in simple terms:
        - MCPs are tools that give the AI agent special capabilities
        - Each MCP provides access to specific services (files, git, databases, etc.)
        - More MCPs = more capabilities, but also more complexity
        
        Recommend MCPs based on:
        - Project requirements
        - Technologies detected
        - User's stated needs
        
        Always recommend:
        - filesystem-mcp (essential for file operations)
        - github-mcp (if git repository detected)
        
        Suggest additional MCPs like:
        - Database MCPs (postgres, mysql, mongodb)
        - Build tool MCPs (maven, gradle, npm)
        - Cloud provider MCPs (aws, ibmcloud, azure)
        - Development tool MCPs (docker, kubernetes)
        
        Offer prebuilt bundles for common scenarios.
        """;
    
    public static final String SKILLS_PHASE = """
        You are helping select specialized skills for the AI agent.
        
        Explain skills in simple terms:
        - Skills are specialized knowledge areas
        - They enhance the agent's expertise in specific domains
        - Skills help the agent write better, more appropriate code
        
        Recommend skills based on:
        - Project domain (e-commerce, fintech, healthcare, etc.)
        - Technical requirements
        - Architecture patterns needed
        
        Skill categories:
        - Architecture & Design Patterns
        - Security & Authentication
        - Data & Database Design
        - Testing & Quality Assurance
        - DevOps & Deployment
        - Domain-specific (e-commerce, fintech, etc.)
        
        Allow users to:
        - Select from prebuilt skills
        - Define custom skills
        - Mix and match as needed
        """;
    
    public static final String REVIEW_PHASE = """
        You are reviewing the complete configuration with the user.
        
        Present a clear summary of:
        - Agent configuration choices
        - Selected MCP servers
        - Selected skills
        
        Ask for final confirmation or offer to make adjustments.
        
        If user wants changes:
        - Ask which section to modify
        - Guide them back to the appropriate phase
        
        If user confirms:
        - Congratulate them
        - Explain what will be generated
        - Mention next steps
        """;
}
```

### 4.3 Phase Completion Status Schema

```java
package com.nel.onepiece.model;

public class PhaseCompletionStatus {
    private boolean complete;
    private String reason;
    private List<String> missingInformation;
    
    public static PhaseCompletionStatus parse(String aiResponse) {
        boolean complete = aiResponse.toUpperCase().startsWith("COMPLETE");
        String reason = aiResponse.substring(aiResponse.indexOf(" ") + 1);
        
        return new PhaseCompletionStatus(complete, reason);
    }
    
    // Constructor, getters, setters
}
```

---

## 5. Structured Configuration Generator

### 5.1 Configuration Generator Interface

```java
package com.nel.onepiece.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface ConfigGeneratorAi {
    
    @SystemMessage("""
        You are a configuration file generator for AI coding agents.
        
        Your task is to generate a complete, valid configuration file based on
        the user's choices and project context.
        
        Requirements:
        - Output must be valid JSON
        - Include all necessary fields
        - Use appropriate default values
        - Follow the schema exactly
        - Include helpful comments where appropriate
        
        Be precise and thorough. The configuration must work correctly.
        """)
    @UserMessage("""
        Generate a configuration file for IBM Bob.
        
        Project Analysis:
        {projectAnalysis}
        
        Agent Configuration:
        {agentConfig}
        
        Selected MCPs:
        {selectedMcps}
        
        Selected Skills:
        {selectedSkills}
        
        Generate a complete .bob.workspace configuration file.
        """)
    BobWorkspaceConfig generateBobWorkspace(
        @V("projectAnalysis") ProjectAnalysis projectAnalysis,
        @V("agentConfig") AgentConfiguration agentConfig,
        @V("selectedMcps") List<String> selectedMcps,
        @V("selectedSkills") List<String> selectedSkills
    );
    
    @SystemMessage("""
        Generate an MCP registry configuration that tracks all installed
        MCP servers and their settings.
        
        Include:
        - Server name and version
        - Installation path
        - Command and arguments
        - Required environment variables
        - Status (active/inactive)
        """)
    @UserMessage("""
        Selected MCPs:
        {selectedMcps}
        
        Project Directory:
        {projectDir}
        
        Generate the MCP registry configuration.
        """)
    McpRegistryConfig generateMcpRegistry(
        @V("selectedMcps") List<McpDefinition> selectedMcps,
        @V("projectDir") String projectDir
    );
    
    @SystemMessage("""
        Generate a skills configuration file that defines all selected skills
        and their knowledge bases.
        
        For each skill, include:
        - Name and description
        - Knowledge areas
        - Best practices
        - Code examples (if applicable)
        - Related patterns
        """)
    @UserMessage("""
        Selected Skills:
        {selectedSkills}
        
        Project Context:
        {projectContext}
        
        Generate the skills configuration.
        """)
    SkillsConfig generateSkillsConfig(
        @V("selectedSkills") List<Skill> selectedSkills,
        @V("projectContext") String projectContext
    );
}
```

### 5.2 Bob Workspace Configuration Schema

```java
package com.nel.onepiece.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;

public class BobWorkspaceConfig {
    
    @Description("Configuration version")
    @JsonProperty("version")
    private String version = "1.0";
    
    @Description("Workspace metadata")
    @JsonProperty("workspace")
    private WorkspaceMetadata workspace;
    
    @Description("MCP server configurations")
    @JsonProperty("mcpServers")
    private Map<String, McpServerConfig> mcpServers;
    
    @Description("Agent behavior settings")
    @JsonProperty("agent")
    private AgentSettings agent;
    
    @Description("Skills configuration")
    @JsonProperty("skills")
    private SkillsReference skills;
    
    @Description("Editor settings")
    @JsonProperty("settings")
    private EditorSettings settings;
    
    // Nested classes
    
    public static class WorkspaceMetadata {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("root")
        private String root;
        
        @JsonProperty("language")
        private String language;
        
        @JsonProperty("framework")
        private String framework;
        
        @JsonProperty("projectType")
        private String projectType;
    }
    
    public static class McpServerConfig {
        @JsonProperty("command")
        private String command;
        
        @JsonProperty("args")
        private List<String> args;
        
        @JsonProperty("env")
        private Map<String, String> env;
        
        @JsonProperty("enabled")
        private boolean enabled = true;
    }
    
    public static class AgentSettings {
        @JsonProperty("codeStyle")
        private String codeStyle;
        
        @JsonProperty("focusAreas")
        private List<String> focusAreas;
        
        @JsonProperty("testingLevel")
        private String testingLevel;
        
        @JsonProperty("documentationLevel")
        private String documentationLevel;
        
        @JsonProperty("assistanceLevel")
        private String assistanceLevel;
    }
    
    public static class SkillsReference {
        @JsonProperty("configPath")
        private String configPath = ".onepiece/skills-config.json";
        
        @JsonProperty("enabled")
        private List<String> enabled;
    }
    
    public static class EditorSettings {
        @JsonProperty("autoSave")
        private boolean autoSave = true;
        
        @JsonProperty("formatOnSave")
        private boolean formatOnSave = true;
        
        @JsonProperty("linting")
        private boolean linting = true;
    }
}
```

### 5.3 Example Generated Configuration

```json
{
  "version": "1.0",
  "workspace": {
    "name": "my-ecommerce-api",
    "root": "/home/user/projects/my-ecommerce-api",
    "language": "java",
    "framework": "quarkus",
    "projectType": "rest-api"
  },
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/home/user/projects/my-ecommerce-api"],
      "env": {
        "ALLOWED_PATHS": "/home/user/projects/my-ecommerce-api"
      },
      "enabled": true
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "${GITHUB_TOKEN}"
      },
      "enabled": true
    },
    "maven": {
      "command": "java",
      "args": ["-jar", "/home/user/.onepiece/mcp-servers/maven/maven-mcp-server.jar"],
      "env": {
        "MAVEN_HOME": "${MAVEN_HOME}",
        "PROJECT_DIR": "/home/user/projects/my-ecommerce-api"
      },
      "enabled": true
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "POSTGRES_CONNECTION_STRING": "${DATABASE_URL}"
      },
      "enabled": true
    }
  },
  "agent": {
    "codeStyle": "enterprise-java",
    "focusAreas": ["security", "performance", "testing", "documentation"],
    "testingLevel": "comprehensive",
    "documentationLevel": "standard",
    "assistanceLevel": "balanced"
  },
  "skills": {
    "configPath": ".onepiece/skills-config.json",
    "enabled": [
      "java-enterprise-patterns",
      "rest-api-design",
      "database-schema-design",
      "security-best-practices",
      "payment-gateway-integration",
      "shopping-cart-logic",
      "order-processing-workflows"
    ]
  },
  "settings": {
    "autoSave": true,
    "formatOnSave": true,
    "linting": true
  }
}
```

---

## 6. Recommendation Engine

### 6.1 MCP Recommendation Interface

```java
package com.nel.onepiece.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface McpRecommendationAi {
    
    @SystemMessage("""
        You are an expert in Model Context Protocol (MCP) servers and their applications.
        
        Your task is to recommend the most appropriate MCP servers based on:
        - Project type and framework
        - User's stated requirements
        - Common patterns for similar projects
        - Best practices
        
        Prioritize MCPs by:
        1. Essential (required for basic functionality)
        2. Highly Recommended (commonly needed)
        3. Optional (nice to have)
        4. Advanced (for specific use cases)
        
        Provide clear explanations for each recommendation.
        """)
    @UserMessage("""
        Project Analysis:
        {projectAnalysis}
        
        User Requirements:
        {userRequirements}
        
        Available MCPs:
        {availableMcps}
        
        Recommend appropriate MCP servers with priorities and explanations.
        """)
    McpRecommendations recommendMcps(
        @V("projectAnalysis") ProjectAnalysis projectAnalysis,
        @V("userRequirements") String userRequirements,
        @V("availableMcps") List<McpDefinition> availableMcps
    );
}
```

### 6.2 MCP Recommendations Schema

```java
package com.nel.onepiece.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;

public class McpRecommendations {
    
    @Description("Essential MCPs that should always be included")
    @JsonProperty("essential")
    private List<McpRecommendation> essential;
    
    @Description("Highly recommended MCPs for this project type")
    @JsonProperty("highlyRecommended")
    private List<McpRecommendation> highlyRecommended;
    
    @Description("Optional MCPs that might be useful")
    @JsonProperty("optional")
    private List<McpRecommendation> optional;
    
    @Description("Advanced MCPs for specific use cases")
    @JsonProperty("advanced")
    private List<McpRecommendation> advanced;
    
    public static class McpRecommendation {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("reason")
        private String reason;
        
        @JsonProperty("benefits")
        private List<String> benefits;
        
        @JsonProperty("useCases")
        private List<String> useCases;
    }
}
```

### 6.3 Skills Recommendation Interface

```java
package com.nel.onepiece.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface SkillsRecommendationAi {
    
    @SystemMessage("""
        You are an expert in software development domains and specialized skills.
        
        Your task is to recommend skills that will enhance the AI agent's
        capabilities for the specific project.
        
        Consider:
        - Project domain (e-commerce, fintech, healthcare, etc.)
        - Technical requirements
        - Architecture patterns
        - Security needs
        - Testing requirements
        - Deployment strategy
        
        Recommend skills that provide:
        - Domain-specific knowledge
        - Best practices
        - Common patterns
        - Security guidelines
        - Performance optimization
        """)
    @UserMessage("""
        Project Analysis:
        {projectAnalysis}
        
        Agent Configuration:
        {agentConfig}
        
        User Requirements:
        {userRequirements}
        
        Available Skills:
        {availableSkills}
        
        Recommend appropriate skills with explanations.
        """)
    SkillsRecommendations recommendSkills(
        @V("projectAnalysis") ProjectAnalysis projectAnalysis,
        @V("agentConfig") AgentConfiguration agentConfig,
        @V("userRequirements") String userRequirements,
        @V("availableSkills") List<Skill> availableSkills
    );
}
```

---

## 7. Conversation Memory Management

### 7.1 Memory Configuration

```java
package com.nel.onepiece.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class LangChain4jConfig {
    
    @Produces
    @ApplicationScoped
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }
    
    @Produces
    public ChatMemory chatMemory(ChatMemoryStore store) {
        return MessageWindowChatMemory.builder()
            .id("default")
            .maxMessages(20)
            .chatMemoryStore(store)
            .build();
    }
}
```

### 7.2 Session Management

```java
package com.nel.onepiece.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ConversationSessionManager {
    
    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();
    
    @Inject
    ChatMemory chatMemory;
    
    public String createSession(ProjectContext context) {
        String sessionId = UUID.randomUUID().toString();
        ConversationSession session = new ConversationSession(sessionId, context);
        sessions.put(sessionId, session);
        return sessionId;
    }
    
    public ConversationSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    public void addMessage(String sessionId, ChatMessage message) {
        chatMemory.add(sessionId, message);
        ConversationSession session = sessions.get(sessionId);
        if (session != null) {
            session.addMessage(message);
        }
    }
    
    public List<ChatMessage> getHistory(String sessionId) {
        return chatMemory.messages(sessionId);
    }
    
    public void closeSession(String sessionId) {
        sessions.remove(sessionId);
        chatMemory.clear(sessionId);
    }
}
```

---

## 8. Error Handling & Fallbacks

### 8.1 AI Service Error Handler

```java
package com.nel.onepiece.service;

import dev.langchain4j.exception.LangChain4jException;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AiErrorHandler {
    
    private static final Logger LOG = Logger.getLogger(AiErrorHandler.class);
    
    public <T> T handleWithFallback(
        Supplier<T> aiOperation,
        Supplier<T> fallback,
        String operationName
    ) {
        try {
            return aiOperation.get();
        } catch (LangChain4jException e) {
            LOG.errorf("AI operation failed: %s - %s", operationName, e.getMessage());
            
            if (isRetryable(e)) {
                LOG.info("Retrying AI operation...");
                try {
                    Thread.sleep(1000);
                    return aiOperation.get();
                } catch (Exception retryException) {
                    LOG.error("Retry failed, using fallback");
                }
            }
            
            return fallback.get();
        }
    }
    
    private boolean isRetryable(Exception e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("timeout") || 
               message.contains("rate limit") ||
               message.contains("connection");
    }
}
```

### 8.2 Fallback Responses

```java
package com.nel.onepiece.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FallbackResponseProvider {
    
    public String getAgentConfigurationFallback() {
        return """
            I'm having trouble connecting to the AI service right now.
            
            Let me provide some standard recommendations:
            
            For your project type, I recommend:
            • Code Style: Enterprise patterns
            • Focus Areas: Security, Testing, Documentation
            • Testing Level: Comprehensive
            
            Would you like to proceed with these defaults? (Y/n)
            """;
    }
    
    public McpRecommendations getMcpRecommendationsFallback(ProjectAnalysis analysis) {
        // Provide rule-based recommendations
        McpRecommendations recommendations = new McpRecommendations();
        
        // Always recommend filesystem and github
        recommendations.addEssential("filesystem", "Required for file operations");
        recommendations.addEssential("github", "Git repository integration");
        
        // Framework-specific recommendations
        if ("quarkus".equalsIgnoreCase(analysis.getFramework())) {
            recommendations.addHighlyRecommended("maven", "Java build system");
        }
        
        return recommendations;
    }
}
```

---

## 9. Testing Strategy

### 9.1 Unit Tests for AI Services

```java
package com.nel.onepiece.ai;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ProjectAnalyzerAiTest {
    
    @Inject
    ProjectAnalyzerAi analyzerAi;
    
    @Test
    void shouldAnalyzeQuarkusProject() {
        String fileStructure = """
            pom.xml
            src/main/java/com/example/App.java
            src/main/resources/application.properties
            """;
        
        String configFiles = """
            pom.xml:
            <project>
              <groupId>com.example</groupId>
              <artifactId>my-app</artifactId>
              <dependencies>
                <dependency>
                  <groupId>io.quarkus</groupId>
                  <artifactId>quarkus-resteasy</artifactId>
                </dependency>
              </dependencies>
            </project>
            """;
        
        ProjectAnalysis analysis = analyzerAi.analyzeProject(
            "/test/project",
            fileStructure,
            configFiles,
            ""
        );
        
        assertEquals("Java", analysis.getLanguage());
        assertEquals("Quarkus", analysis.getFramework());
        assertTrue(analysis.getConfidence() > 70);
    }
}
```

### 9.2 Integration Tests

```java
package com.nel.onepiece.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;

@QuarkusTest
class ConfigurationFlowIntegrationTest {
    
    @Inject
    ConfigurationAgentService agentService;
    
    @Test
    void shouldCompleteFullConfigurationFlow() {
        // Create test project
        Path testProject = createTestProject();
        
        // Start agent configuration
        ConversationSession session = agentService.startAgentConfiguration(testProject);
        assertNotNull(session);
        
        // Simulate user responses
        ConversationResponse response1 = agentService.processUserInput(
            session,
            "REST API for e-commerce"
        );
        assertNotNull(response1.getMessage());
        
        // Continue conversation...
        // Verify final configuration
    }
}
```

---

## 10. Performance Optimization

### 10.1 Caching Strategies

```java
package com.nel.onepiece.service;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CachedAiService {
    
    @CacheResult(cacheName = "project-analysis")
    public ProjectAnalysis analyzeProject(String projectPath) {
        // Expensive AI operation
        return analyzerAi.analyzeProject(projectPath, ...);
    }
    
    @CacheResult(cacheName = "mcp-recommendations")
    public McpRecommendations recommendMcps(ProjectAnalysis analysis) {
        // Expensive AI operation
        return recommendationAi.recommendMcps(analysis, ...);
    }
}
```

### 10.2 Token Usage Optimization

```java
package com.nel.onepiece.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenOptimizer {
    
    public String summarizeFileStructure(List<String> files) {
        // Limit to most relevant files
        return files.stream()
            .filter(this::isRelevantFile)
            .limit(50)
            .collect(Collectors.joining("\n"));
    }
    
    private boolean isRelevantFile(String path) {
        // Filter out generated files, dependencies, etc.
        return !path.contains("node_modules") &&
               !path.contains("target") &&
               !path.contains(".git");
    }
    
    public String truncateContent(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "\n... (truncated)";
    }
}
```

---

## 11. Implementation Checklist

- [ ] Set up LangChain4j dependencies and configuration
- [ ] Implement `ProjectAnalyzerAi` with structured output
- [ ] Create `ConfigurationAgentAi` with conversation memory
- [ ] Build `ConfigGeneratorAi` for file generation
- [ ] Implement `McpRecommendationAi` and `SkillsRecommendationAi`
- [ ] Set up conversation session management
- [ ] Add error handling and fallback mechanisms
- [ ] Implement caching for expensive AI operations
- [ ] Write comprehensive unit and integration tests
- [ ] Optimize token usage and response times
- [ ] Add logging and monitoring for AI operations
- [ ] Document prompt engineering best practices

---

## 12. Future Enhancements

- **Multi-Model Support**: Allow users to choose between OpenAI, IBM watsonx, Anthropic
- **Fine-Tuned Models**: Train custom models on configuration patterns
- **Prompt Versioning**: Track and version prompt templates
- **A/B Testing**: Test different prompts for better results
- **User Feedback Loop**: Learn from user corrections and preferences
- **Offline Mode**: Provide rule-based fallbacks when AI is unavailable