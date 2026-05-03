package com.nel.onepiece.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nel.onepiece.model.BobWorkspaceConfig;
import com.nel.onepiece.model.ProjectAnalysis;
import com.nel.onepiece.model.presets.AgentPreset;
import com.nel.onepiece.model.presets.SkillPreset;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Service for generating configuration files for AI agents
 */
@ApplicationScoped
public class ConfigurationGenerator {

    private final ObjectMapper objectMapper;

    public ConfigurationGenerator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Generate .bob.workspace configuration file
     */
    public void generateBobWorkspace(String projectPath, ProjectAnalysis analysis, String systemPrompt, List<String> skills, String modelName, double temperature, int maxTokens) throws IOException {
        BobWorkspaceConfig config = new BobWorkspaceConfig();
        
        // Workspace info
        BobWorkspaceConfig.WorkspaceInfo workspace = new BobWorkspaceConfig.WorkspaceInfo();
        workspace.setName(getProjectName(projectPath));
        workspace.setDescription("AI-generated workspace for " + analysis.getFramework() + " project");
        workspace.setVersion("1.0.0");
        config.setWorkspace(workspace);
        
        // MCP Servers
        Map<String, BobWorkspaceConfig.McpServer> mcpServers = new LinkedHashMap<>();
        for (String mcpName : analysis.getRecommendedMcps()) {
            mcpServers.put(mcpName, createMcpServer(mcpName, projectPath));
        }
        config.setMcpServers(mcpServers);
        
        // Agent configuration
        BobWorkspaceConfig.AgentConfig agent = new BobWorkspaceConfig.AgentConfig();
        agent.setName("IBM Bob");
        agent.setModel(modelName);
        agent.setTemperature(temperature);
        agent.setMaxTokens(maxTokens);
        agent.setSystemPrompt(systemPrompt);
        config.setAgent(agent);
        
        // Skills
        config.setSkills(skills);
        
        // Settings
        BobWorkspaceConfig.Settings settings = new BobWorkspaceConfig.Settings();
        config.setSettings(settings);
        
        // Exclude patterns
        config.setExcludePatterns(getDefaultExcludePatterns(analysis));
        
        // Write to file
        Path outputPath = Paths.get(projectPath, ".bob.workspace");
        objectMapper.writeValue(outputPath.toFile(), config);
    }

    /**
     * Create MCP server configuration based on type
     */
    private BobWorkspaceConfig.McpServer createMcpServer(String mcpName, String projectPath) {
        BobWorkspaceConfig.McpServer server = new BobWorkspaceConfig.McpServer();
        
        switch (mcpName) {
            case "filesystem-mcp":
                server.setCommand("npx");
                server.setArgs(List.of("-y", "@modelcontextprotocol/server-filesystem", projectPath));
                break;
                
            case "github-mcp":
                server.setCommand("npx");
                server.setArgs(List.of("-y", "@modelcontextprotocol/server-github"));
                Map<String, String> githubEnv = new HashMap<>();
                githubEnv.put("GITHUB_PERSONAL_ACCESS_TOKEN", "${GITHUB_TOKEN}");
                server.setEnv(githubEnv);
                break;
                
            case "maven-mcp":
                server.setCommand("npx");
                server.setArgs(List.of("-y", "@modelcontextprotocol/server-maven", projectPath));
                break;
                
            case "gradle-mcp":
                server.setCommand("npx");
                server.setArgs(List.of("-y", "@modelcontextprotocol/server-gradle", projectPath));
                break;
                
            case "npm-mcp":
                server.setCommand("npx");
                server.setArgs(List.of("-y", "@modelcontextprotocol/server-npm", projectPath));
                break;
                
            case "postgres-mcp":
                server.setCommand("npx");
                server.setArgs(List.of("-y", "@modelcontextprotocol/server-postgres"));
                Map<String, String> pgEnv = new HashMap<>();
                pgEnv.put("POSTGRES_CONNECTION_STRING", "${DATABASE_URL}");
                server.setEnv(pgEnv);
                break;
                
            case "docker-mcp":
                server.setCommand("npx");
                server.setArgs(List.of("-y", "@modelcontextprotocol/server-docker"));
                break;
                
            default:
                server.setCommand("npx");
                server.setArgs(List.of("-y", mcpName));
        }
        
        return server;
    }

    /**
     * Get project name from path
     */
    private String getProjectName(String projectPath) {
        Path path = Paths.get(projectPath);
        return path.getFileName().toString();
    }

    /**
     * Get default exclude patterns based on project type
     */
    private List<String> getDefaultExcludePatterns(ProjectAnalysis analysis) {
        List<String> patterns = new ArrayList<>(List.of(
            "node_modules/**",
            ".git/**",
            ".idea/**",
            ".vscode/**",
            "*.log"
        ));
        
        if ("Maven".equals(analysis.getBuildTool())) {
            patterns.add("target/**");
        } else if ("Gradle".equals(analysis.getBuildTool())) {
            patterns.add("build/**");
            patterns.add(".gradle/**");
        } else if ("npm".equals(analysis.getBuildTool())) {
            patterns.add("dist/**");
            patterns.add("build/**");
        }
        
        return patterns;
    }

    /**
     * Generate MCP registry file
     */
    public void generateMcpRegistry(String projectPath, ProjectAnalysis analysis) throws IOException {
        Map<String, Object> registry = new LinkedHashMap<>();
        registry.put("version", "1.0.0");
        registry.put("generated", new Date().toString());
        
        List<Map<String, Object>> servers = new ArrayList<>();
        for (String mcpName : analysis.getRecommendedMcps()) {
            Map<String, Object> server = new LinkedHashMap<>();
            server.put("name", mcpName);
            server.put("version", "latest");
            server.put("enabled", true);
            server.put("installedAt", new Date().toString());
            servers.add(server);
        }
        registry.put("servers", servers);
        
        // Create .onepiece directory if it doesn't exist
        Path onepieceDir = Paths.get(projectPath, ".onepiece");
        Files.createDirectories(onepieceDir);
        
        // Write registry file
        Path registryPath = onepieceDir.resolve("mcp-registry.json");
        objectMapper.writeValue(registryPath.toFile(), registry);
    }

    /**
     * Generate project metadata file
     */
    public void generateProjectMetadata(String projectPath, ProjectAnalysis analysis, String agentType) throws IOException {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("agent", agentType);
        metadata.put("framework", analysis.getFramework());
        metadata.put("language", analysis.getLanguage());
        metadata.put("buildTool", analysis.getBuildTool());
        metadata.put("projectType", analysis.getProjectType());
        metadata.put("mcps", analysis.getRecommendedMcps());
        metadata.put("createdAt", new Date().toString());
        metadata.put("lastModified", new Date().toString());
        
        Map<String, Object> deployment = new LinkedHashMap<>();
        deployment.put("target", "ibmcloud");
        deployment.put("region", "us-south");
        deployment.put("appName", getProjectName(projectPath));
        metadata.put("deployment", deployment);
        
        // Create .onepiece directory if it doesn't exist
        Path onepieceDir = Paths.get(projectPath, ".onepiece");
        Files.createDirectories(onepieceDir);
        
        // Write metadata file
        Path metadataPath = onepieceDir.resolve("project.json");
        objectMapper.writeValue(metadataPath.toFile(), metadata);
    }

    /**
     * Generate .env.example file
     */
    public void generateEnvExample(String projectPath, ProjectAnalysis analysis) throws IOException {
        StringBuilder envContent = new StringBuilder();
        envContent.append("# One Piece CLI Environment Variables\n");
        envContent.append("# Copy this file to .env and fill in your values\n\n");
        
        envContent.append("# OpenAI API Key (for AI features)\n");
        envContent.append("OPENAI_API_KEY=your-openai-api-key-here\n\n");
        
        if (analysis.getRecommendedMcps().contains("github-mcp")) {
            envContent.append("# GitHub Personal Access Token\n");
            envContent.append("GITHUB_TOKEN=your-github-token-here\n\n");
        }
        
        if (analysis.getRecommendedMcps().contains("postgres-mcp")) {
            envContent.append("# Database Connection\n");
            envContent.append("DATABASE_URL=postgresql://user:password@localhost:5432/dbname\n\n");
        }
        
        envContent.append("# IBM Cloud Credentials (for deployment)\n");
        envContent.append("IBM_CLOUD_API_KEY=your-ibm-cloud-api-key\n");
        envContent.append("IBM_CLOUD_REGION=us-south\n\n");
        
        envContent.append("# HashiCorp Vault (optional - for production)\n");
        envContent.append("VAULT_URL=https://vault.example.com\n");
        envContent.append("VAULT_TOKEN=your-vault-token\n");
        
        Path envPath = Paths.get(projectPath, ".env.example");
        Files.writeString(envPath, envContent.toString());
    }

    public void generateBobProjectMcpConfig(String projectPath, Map<String, Map<String, Object>> mcpServers) throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("mcpServers", mcpServers);

        Path bobDir = Paths.get(projectPath, ".bob");
        Files.createDirectories(bobDir);
        Path mcpPath = bobDir.resolve("mcp.json");
        objectMapper.writeValue(mcpPath.toFile(), root);
    }

    public void generateBobProjectMcpConfig(String projectPath, List<String> mcpNames, Map<String, String> envVarNames) throws IOException {
        Map<String, Map<String, Object>> servers = new LinkedHashMap<>();

        for (String mcpName : mcpNames) {
            Map<String, Object> server = new LinkedHashMap<>();
            server.put("disabled", false);
            server.put("cwd", projectPath);

            switch (mcpName) {
                case "filesystem-mcp" -> {
                    server.put("command", "npx");
                    server.put("args", List.of("-y", "@modelcontextprotocol/server-filesystem", projectPath));
                }
                case "github-mcp" -> {
                    server.put("command", "npx");
                    server.put("args", List.of("-y", "@modelcontextprotocol/server-github"));
                    server.put("env", Map.of(
                        "GITHUB_PERSONAL_ACCESS_TOKEN",
                        "${" + envVarNames.getOrDefault("GITHUB_PERSONAL_ACCESS_TOKEN", "GITHUB_TOKEN") + "}"
                    ));
                }
                case "maven-mcp" -> {
                    server.put("command", "npx");
                    server.put("args", List.of("-y", "@modelcontextprotocol/server-maven", projectPath));
                }
                case "gradle-mcp" -> {
                    server.put("command", "npx");
                    server.put("args", List.of("-y", "@modelcontextprotocol/server-gradle", projectPath));
                }
                case "npm-mcp" -> {
                    server.put("command", "npx");
                    server.put("args", List.of("-y", "@modelcontextprotocol/server-npm", projectPath));
                }
                case "postgres-mcp" -> {
                    server.put("command", "npx");
                    server.put("args", List.of("-y", "@modelcontextprotocol/server-postgres"));
                    server.put("env", Map.of(
                        "POSTGRES_CONNECTION_STRING",
                        "${" + envVarNames.getOrDefault("POSTGRES_CONNECTION_STRING", "DATABASE_URL") + "}"
                    ));
                }
                case "docker-mcp" -> {
                    server.put("command", "npx");
                    server.put("args", List.of("-y", "@modelcontextprotocol/server-docker"));
                }
                default -> {
                    server.put("command", "npx");
                    server.put("args", List.of("-y", mcpName));
                }
            }

            servers.put(mcpName, server);
        }

        generateBobProjectMcpConfig(projectPath, servers);
    }

    public void generateBobCustomModes(String projectPath, String workflow) throws IOException {
        Path bobDir = Paths.get(projectPath, ".bob");
        Files.createDirectories(bobDir);

        String yaml = """
customModes:
  - slug: poc-architect
    name: "🧭 PoC Architect"
    roleDefinition: "You are a pragmatic software architect. Focus on proof-of-concept delivery, clear decisions, and minimal risk."
    whenToUse: "Use for architecture decisions, repo analysis, and planning execution steps."
    customInstructions: "Follow the project workflow: %s. Prefer safe, incremental changes and verify each step."
    groups:
      - read
      - browser
      - mcp

  - slug: deploy-engineer
    name: "☁️ Deploy Engineer"
    roleDefinition: "You are a DevOps engineer specialized in IBM Cloud deployments and CLI automation."
    whenToUse: "Use for deployment, CI/CD, and troubleshooting IBM Cloud issues."
    customInstructions: "Follow the project workflow: %s. Do not print or persist secrets. Prefer reproducible commands."
    groups:
      - read
      - edit
      - command
      - mcp

  - slug: docs-writer
    name: "📝 Documentation Writer"
    roleDefinition: "You are a technical writer specializing in concise, accurate documentation."
    whenToUse: "Use for writing README, setup guides, and runbooks."
    customInstructions: "Write clear steps and expected outcomes. Keep docs consistent with the PoC scope."
    groups:
      - read
      - edit
      - mcp
""".formatted(workflow, workflow);

        Files.writeString(bobDir.resolve("custom_modes.yaml"), yaml);
    }

    public void generateBobCustomModesFromPresets(String projectPath, List<AgentPreset.CustomMode> modes) throws IOException {
        Path bobDir = Paths.get(projectPath, ".bob");
        Files.createDirectories(bobDir);

        StringBuilder yaml = new StringBuilder();
        yaml.append("customModes:\n");

        for (AgentPreset.CustomMode mode : modes) {
            if (mode == null || mode.getSlug() == null || mode.getSlug().isBlank()) {
                continue;
            }

            yaml.append("  - slug: ").append(mode.getSlug().trim()).append("\n");
            yaml.append("    name: ").append(yamlQuote(mode.getName())).append("\n");
            yaml.append("    roleDefinition: ").append(yamlQuote(mode.getRoleDefinition())).append("\n");
            yaml.append("    whenToUse: ").append(yamlQuote(mode.getWhenToUse())).append("\n");
            yaml.append("    customInstructions: ").append(yamlQuote(mode.getCustomInstructions())).append("\n");
            yaml.append("    groups:\n");

            List<String> groups = mode.getGroups() != null ? mode.getGroups() : List.of();
            for (String g : groups) {
                if (g == null || g.isBlank()) {
                    continue;
                }
                yaml.append("      - ").append(g.trim()).append("\n");
            }
            if (groups.isEmpty()) {
                yaml.append("      - read\n");
            }
            yaml.append("\n");
        }

        Files.writeString(bobDir.resolve("custom_modes.yaml"), yaml.toString());
    }

    public void generateBobRules(String projectPath, String workflow, List<String> skills, List<String> mcps) throws IOException {
        Path rulesDir = Paths.get(projectPath, ".bob", "rules");
        Files.createDirectories(rulesDir);

        String rules = """
# One Piece CLI - Bob Workspace Rules

## Workflow
- Project workflow: %s
- Focus on proof-of-concept functionality first.
- Verify outputs (generated files, command results) after every change.

## Tools
- Use MCP servers only when needed; disable unused servers/tools to reduce context.
- Do not store secrets in repository files. Use environment variables or Vault.

## Skills
- Enabled skills: %s

## MCP Servers
- Enabled MCP servers: %s
""".formatted(
            workflow,
            String.join(", ", skills),
            String.join(", ", mcps)
        );

        Files.writeString(rulesDir.resolve("01-workspace.md"), rules);
    }

    public void generateBobSkillsFromPresets(String projectPath, List<SkillPreset> skills) throws IOException {
        Path skillsDir = Paths.get(projectPath, ".bob", "skills");
        Files.createDirectories(skillsDir);

        for (SkillPreset skill : skills) {
            if (skill == null || skill.getSlug() == null || skill.getSlug().isBlank()) {
                continue;
            }
            Path dir = skillsDir.resolve(skill.getSlug().trim());
            Files.createDirectories(dir);
            String content = skill.getSkillMarkdown() != null ? skill.getSkillMarkdown() : "";
            Files.writeString(dir.resolve("SKILL.md"), content);
        }
    }

    public void generateBobSkills(String projectPath, List<String> skills) throws IOException {
        Path skillsDir = Paths.get(projectPath, ".bob", "skills");
        Files.createDirectories(skillsDir);

        for (String skill : skills) {
            String normalized = skill.trim();
            if (normalized.isEmpty()) {
                continue;
            }

            if (!List.of("bug-hunter", "context7-auto-research").contains(normalized)) {
                continue;
            }

            Path dir = skillsDir.resolve(normalized);
            Files.createDirectories(dir);

            String content;
            if ("bug-hunter".equals(normalized)) {
                content = """
---
name: bug-hunter
description: "Systematically finds and fixes bugs using proven debugging techniques. Traces from symptoms to root cause, implements fixes, and prevents regression."
category: development
risk: safe
source: community
date_added: "2026-03-05"
---

# Bug Hunter

Systematically hunt down and fix bugs using proven debugging techniques. No guessing—follow the evidence.

## When to Use This Skill

- User reports a bug or error
- Something isn't working as expected
- User says "fix the bug" or "debug this"
- Intermittent failures or weird behavior
- Production issues need investigation

## The Debugging Process

### 1. Reproduce the Bug

First, make it happen consistently:

```
1. Get exact steps to reproduce
2. Try to reproduce locally
3. Note what triggers it
4. Document the error message/behavior
5. Check if it happens every time or randomly
```

If you can't reproduce it, gather more info:
- What environment? (dev, staging, prod)
- What browser/device?
- What user actions preceded it?
- Any error logs?

### 2. Gather Evidence

Collect all available information:

**Check logs:**
```bash
# Application logs
tail -f logs/app.log

# System logs
journalctl -u myapp -f

# Browser console
# Open DevTools → Console tab
```

**Check error messages:**
- Full stack trace
- Error type and message
- Line numbers
- Timestamp

**Check state:**
- What data was being processed?
- What was the user trying to do?
- What's in the database?
- What's in local storage/cookies?

### 3. Form a Hypothesis

Based on evidence, guess what's wrong:

```
"The login times out because the session cookie 
expires before the auth check completes"

"The form fails because email validation regex 
doesn't handle plus signs"

"The API returns 500 because the database query 
has a syntax error with special characters"
```

### 4. Test the Hypothesis

Prove or disprove your guess:

**Add logging:**
```javascript
console.log('Before API call:', userData);
const response = await api.login(userData);
console.log('After API call:', response);
```

**Use debugger:**
```javascript
debugger; // Execution pauses here
const result = processData(input);
```

**Isolate the problem:**
```javascript
// Comment out code to narrow down
// const result = complexFunction();
const result = { mock: 'data' }; // Use mock data
```

### 5. Find Root Cause

Trace back to the actual problem:

**Common root causes:**
- Null/undefined values
- Wrong data types
- Race conditions
- Missing error handling
- Incorrect logic
- Off-by-one errors
- Async/await issues
- Missing validation

**Example trace:**
```
Symptom: "Cannot read property 'name' of undefined"
↓
Where: user.profile.name
↓
Why: user.profile is undefined
↓
Why: API didn't return profile
↓
Why: User ID was null
↓
Root cause: Login didn't set user ID in session
```

### 6. Implement Fix

Fix the root cause, not the symptom:

**Bad fix (symptom):**
```javascript
// Just hide the error
const name = user?.profile?.name || 'Unknown';
```

**Good fix (root cause):**
```javascript
// Ensure user ID is set on login
const login = async (credentials) => {
  const user = await authenticate(credentials);
  if (user) {
    session.userId = user.id; // Fix: Set user ID
    return user;
  }
  throw new Error('Invalid credentials');
};
```

### 7. Test the Fix

Verify it actually works:

```
1. Reproduce the original bug
2. Apply the fix
3. Try to reproduce again (should fail)
4. Test edge cases
5. Test related functionality
6. Run existing tests
```

### 8. Prevent Regression

Add a test so it doesn't come back:

```javascript
test('login sets user ID in session', async () => {
  const user = await login({ email: 'test@example.com', password: 'pass' });
  
  expect(session.userId).toBe(user.id);
  expect(session.userId).not.toBeNull();
});
```

## Debugging Techniques

### Binary Search

Cut the problem space in half repeatedly:

```javascript
// Does the bug happen before or after this line?
console.log('CHECKPOINT 1');
// ... code ...
console.log('CHECKPOINT 2');
// ... code ...
console.log('CHECKPOINT 3');
```

### Rubber Duck Debugging

Explain the code line by line out loud. Often you'll spot the issue while explaining.

### Print Debugging

Strategic console.logs:

```javascript
console.log('Input:', input);
console.log('After transform:', transformed);
console.log('Before save:', data);
console.log('Result:', result);
```

### Diff Debugging

Compare working vs broken:
- What changed recently?
- What's different between environments?
- What's different in the data?

### Time Travel Debugging

Use git to find when it broke:

```bash
git bisect start
git bisect bad  # Current commit is broken
git bisect good abc123  # This old commit worked
# Git will check out commits for you to test
```

## Common Bug Patterns

### Null/Undefined

```javascript
// Bug
const name = user.profile.name;

// Fix
const name = user?.profile?.name || 'Unknown';

// Better fix
if (!user || !user.profile) {
  throw new Error('User profile required');
}
const name = user.profile.name;
```

### Race Condition

```javascript
// Bug
let data = null;
fetchData().then(result => data = result);
console.log(data); // null - not loaded yet

// Fix
const data = await fetchData();
console.log(data); // correct value
```

### Off-by-One

```javascript
// Bug
for (let i = 0; i <= array.length; i++) {
  console.log(array[i]); // undefined on last iteration
}

// Fix
for (let i = 0; i < array.length; i++) {
  console.log(array[i]);
}
```

### Type Coercion

```javascript
// Bug
if (count == 0) { // true for "", [], null
  
// Fix
if (count === 0) { // only true for 0
```

### Async Without Await

```javascript
// Bug
const result = asyncFunction(); // Returns Promise
console.log(result.data); // undefined

// Fix
const result = await asyncFunction();
console.log(result.data); // correct value
```

## Debugging Tools

### Browser DevTools

```
Console: View logs and errors
Sources: Set breakpoints, step through code
Network: Check API calls and responses
Application: View cookies, storage, cache
Performance: Find slow operations
```

### Node.js Debugging

```javascript
// Built-in debugger
node --inspect app.js

// Then open chrome://inspect in Chrome
```

### VS Code Debugging

```json
// .vscode/launch.json
{
  "type": "node",
  "request": "launch",
  "name": "Debug App",
  "program": "${workspaceFolder}/app.js"
}
```

## When You're Stuck

1. Take a break (seriously, walk away for 10 minutes)
2. Explain it to someone else (or a rubber duck)
3. Search for the exact error message
4. Check if it's a known issue (GitHub issues, Stack Overflow)
5. Simplify: Create minimal reproduction
6. Start over: Delete and rewrite the problematic code
7. Ask for help (provide context, what you've tried)

## Documentation Template

After fixing, document it:

```markdown
## Bug: Login timeout after 30 seconds

**Symptom:** Users get logged out immediately after login

**Root Cause:** Session cookie expires before auth check completes

**Fix:** Increased session timeout from 30s to 3600s in config

**Files Changed:**
- config/session.js (line 12)

**Testing:** Verified login persists for 1 hour

**Prevention:** Added test for session persistence
```

## Key Principles

- Reproduce first, fix second
- Follow the evidence, don't guess
- Fix root cause, not symptoms
- Test the fix thoroughly
- Add tests to prevent regression
- Document what you learned

## Related Skills

- `@systematic-debugging` - Advanced debugging
- `@test-driven-development` - Testing
- `@codebase-audit-pre-push` - Code review

## Limitations
- Use this skill only when the task clearly matches the scope described above.
- Do not treat the output as a substitute for environment-specific validation, testing, or expert review.
- Stop and ask for clarification if required inputs, permissions, safety boundaries, or success criteria are missing.
""";
            } else {
                content = """
---
name: context7-auto-research
description: "Automatically fetch latest library/framework documentation for Claude Code via Context7 API. Use when you need up-to-date documentation for libraries and frameworks or asking about React, Next.js, Prisma, or any other popular library."
risk: unknown
source: community
date_added: "2026-02-27"
---

# context7-auto-research

## Overview
Automatically fetch latest library/framework documentation for Claude Code via Context7 API

## When to Use
- When you need up-to-date documentation for libraries and frameworks
- When asking about React, Next.js, Prisma, or any other popular library

## Installation
```bash
npx skills add -g BenedictKing/context7-auto-research
```

## Step-by-Step Guide
1. Install the skill using the command above
2. Configure API key (optional, see GitHub repo for details)
3. Use naturally in Claude Code conversations

## Examples
See [GitHub Repository](https://github.com/BenedictKing/context7-auto-research) for examples.

## Best Practices
- Configure API keys via environment variables for higher rate limits
- Use the skill's auto-trigger feature for seamless integration

## Troubleshooting
See the GitHub repository for troubleshooting guides.

## Related Skills
- tavily-web, exa-search, firecrawl-scraper, codex-review

## Limitations
- Use this skill only when the task clearly matches the scope described above.
- Do not treat the output as a substitute for environment-specific validation, testing, or expert review.
- Stop and ask for clarification if required inputs, permissions, safety boundaries, or success criteria are missing.
""";
            }

            Files.writeString(dir.resolve("SKILL.md"), content);
        }
    }

    private String yamlQuote(String value) {
        if (value == null) {
            return "\"\"";
        }
        String v = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return "\"" + v + "\"";
    }
}

// Made with Bob
