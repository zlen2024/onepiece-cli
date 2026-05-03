package com.nel.onepiece.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nel.onepiece.model.presets.AgentPreset;
import com.nel.onepiece.model.presets.McpPreset;
import com.nel.onepiece.model.presets.PresetLibrary;
import com.nel.onepiece.model.presets.SkillPreset;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PresetLibraryManager {

    private static final String PRESETS_FILE_NAME = "presets.json";
    private static final String TEMPLATES_DIR_NAME = "templates";

    private final ObjectMapper objectMapper;

    @Inject
    ConfigManager configManager;

    public PresetLibraryManager() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Path presetsFilePath() {
        return configManager.getConfigDir().resolve(PRESETS_FILE_NAME);
    }

    public Path templatesDirPath() {
        return configManager.getConfigDir().resolve(TEMPLATES_DIR_NAME);
    }

    public PresetLibrary loadOrCreate() {
        Path file = presetsFilePath();
        try {
            if (Files.exists(file)) {
                PresetLibrary existing = objectMapper.readValue(file.toFile(), PresetLibrary.class);
                boolean changed = mergeMissingDefaults(existing);
                if (changed) {
                    try {
                        save(existing);
                    } catch (Exception ignored) {
                    }
                }
                return existing;
            }
        } catch (Exception ignored) {
        }

        PresetLibrary defaults = defaultLibrary();
        try {
            save(defaults);
        } catch (Exception ignored) {
        }
        return defaults;
    }

    private boolean mergeMissingDefaults(PresetLibrary target) {
        PresetLibrary defaults = defaultLibrary();
        boolean changed = false;

        if (target.getAgents() == null) {
            target.setAgents(new ArrayList<>());
            changed = true;
        }
        if (target.getSkills() == null) {
            target.setSkills(new ArrayList<>());
            changed = true;
        }
        if (target.getMcps() == null) {
            target.setMcps(new ArrayList<>());
            changed = true;
        }

        for (AgentPreset preset : defaults.getAgents()) {
            if (preset == null || preset.getId() == null) {
                continue;
            }
            boolean exists = target.getAgents().stream().anyMatch(a -> a != null && preset.getId().equals(a.getId()));
            if (!exists) {
                target.getAgents().add(preset);
                changed = true;
            }
        }

        for (SkillPreset preset : defaults.getSkills()) {
            if (preset == null || preset.getSlug() == null) {
                continue;
            }
            boolean exists = target.getSkills().stream().anyMatch(s -> s != null && preset.getSlug().equals(s.getSlug()));
            if (!exists) {
                target.getSkills().add(preset);
                changed = true;
            }
        }

        for (McpPreset preset : defaults.getMcps()) {
            if (preset == null || preset.getName() == null) {
                continue;
            }
            boolean exists = target.getMcps().stream().anyMatch(m -> m != null && preset.getName().equals(m.getName()));
            if (!exists) {
                target.getMcps().add(preset);
                changed = true;
            }
        }

        return changed;
    }

    public void save(PresetLibrary library) throws IOException {
        Path configDir = configManager.getConfigDir();
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }
        objectMapper.writeValue(presetsFilePath().toFile(), library);
    }

    public void exportTemplates(PresetLibrary library) throws IOException {
        Path dir = templatesDirPath();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        Map<String, Object> index = new LinkedHashMap<>();
        index.put("version", library.getVersion());
        index.put("agents", library.getAgents());
        index.put("skills", library.getSkills());
        index.put("mcps", library.getMcps());

        objectMapper.writeValue(dir.resolve("index.json").toFile(), index);

        Path agentsDir = dir.resolve("agents");
        Path skillsDir = dir.resolve("skills");
        Path mcpsDir = dir.resolve("mcps");
        Files.createDirectories(agentsDir);
        Files.createDirectories(skillsDir);
        Files.createDirectories(mcpsDir);

        for (AgentPreset agent : library.getAgents()) {
            if (agent.getId() == null || agent.getId().isBlank()) {
                continue;
            }
            Path aDir = agentsDir.resolve(agent.getId());
            Files.createDirectories(aDir);
            if (agent.getSystemPrompt() != null) {
                Files.writeString(aDir.resolve("systemPrompt.txt"), agent.getSystemPrompt());
            }
            objectMapper.writeValue(aDir.resolve("agent.json").toFile(), agent);
        }

        for (SkillPreset skill : library.getSkills()) {
            if (skill.getSlug() == null || skill.getSlug().isBlank()) {
                continue;
            }
            Path sDir = skillsDir.resolve(skill.getSlug());
            Files.createDirectories(sDir);
            if (skill.getSkillMarkdown() != null) {
                Files.writeString(sDir.resolve("SKILL.md"), skill.getSkillMarkdown());
            }
            objectMapper.writeValue(sDir.resolve("skill.json").toFile(), skill);
        }

        for (McpPreset mcp : library.getMcps()) {
            if (mcp.getName() == null || mcp.getName().isBlank()) {
                continue;
            }
            Path mDir = mcpsDir.resolve(mcp.getName());
            Files.createDirectories(mDir);
            objectMapper.writeValue(mDir.resolve("server.json").toFile(), mcp.getServer());
            objectMapper.writeValue(mDir.resolve("mcp.json").toFile(), mcp);
        }
    }

    public PresetLibrary importTemplates() throws IOException {
        Path index = templatesDirPath().resolve("index.json");
        if (!Files.exists(index)) {
            throw new IOException("templates/index.json not found");
        }

        Map<String, Object> root = objectMapper.readValue(index.toFile(), new TypeReference<>() {});
        PresetLibrary lib = new PresetLibrary();

        Object version = root.get("version");
        if (version instanceof String v && !v.isBlank()) {
            lib.setVersion(v);
        }

        lib.setAgents(objectMapper.convertValue(root.get("agents"), new TypeReference<List<AgentPreset>>() {}));
        lib.setSkills(objectMapper.convertValue(root.get("skills"), new TypeReference<List<SkillPreset>>() {}));
        lib.setMcps(objectMapper.convertValue(root.get("mcps"), new TypeReference<List<McpPreset>>() {}));

        if (lib.getAgents() == null) {
            lib.setAgents(new ArrayList<>());
        }
        if (lib.getSkills() == null) {
            lib.setSkills(new ArrayList<>());
        }
        if (lib.getMcps() == null) {
            lib.setMcps(new ArrayList<>());
        }

        save(lib);
        return lib;
    }

    public Optional<AgentPreset> findAgentById(PresetLibrary library, String id) {
        if (id == null) {
            return Optional.empty();
        }
        return library.getAgents().stream().filter(a -> id.equals(a.getId())).findFirst();
    }

    public Optional<SkillPreset> findSkillBySlug(PresetLibrary library, String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        return library.getSkills().stream().filter(s -> slug.equals(s.getSlug())).findFirst();
    }

    public Optional<McpPreset> findMcpByName(PresetLibrary library, String name) {
        if (name == null) {
            return Optional.empty();
        }
        return library.getMcps().stream().filter(m -> name.equals(m.getName())).findFirst();
    }

    public void upsertAgent(PresetLibrary library, AgentPreset preset) {
        library.getAgents().removeIf(a -> a.getId() != null && a.getId().equals(preset.getId()));
        library.getAgents().add(preset);
    }

    public void deleteAgent(PresetLibrary library, String id) {
        library.getAgents().removeIf(a -> id.equals(a.getId()));
    }

    public void upsertSkill(PresetLibrary library, SkillPreset preset) {
        library.getSkills().removeIf(s -> s.getSlug() != null && s.getSlug().equals(preset.getSlug()));
        library.getSkills().add(preset);
    }

    public void deleteSkill(PresetLibrary library, String slug) {
        library.getSkills().removeIf(s -> slug.equals(s.getSlug()));
    }

    public void upsertMcp(PresetLibrary library, McpPreset preset) {
        library.getMcps().removeIf(m -> m.getName() != null && m.getName().equals(preset.getName()));
        library.getMcps().add(preset);
    }

    public void deleteMcp(PresetLibrary library, String name) {
        library.getMcps().removeIf(m -> name.equals(m.getName()));
    }

    public PresetLibrary defaultLibrary() {
        PresetLibrary lib = new PresetLibrary();

        AgentPreset poc = new AgentPreset();
        poc.setId("poc-architect");
        poc.setAgentType("bob");
        poc.setDisplayName("PoC Architect");
        poc.setDescription("Pragmatic architect focused on safe PoC delivery.");
        poc.setSystemPrompt("You are a pragmatic software architect. Focus on proof-of-concept delivery, clear decisions, and minimal risk.");

        AgentPreset.CustomMode pocMode = new AgentPreset.CustomMode();
        pocMode.setSlug("poc-architect");
        pocMode.setName("PoC Architect");
        pocMode.setRoleDefinition("You are a pragmatic software architect. Focus on proof-of-concept delivery, clear decisions, and minimal risk.");
        pocMode.setWhenToUse("Use for architecture decisions, repo analysis, and planning execution steps.");
        pocMode.setCustomInstructions("Prefer safe, incremental changes and verify each step.");
        pocMode.setGroups(List.of("read", "edit", "command", "mcp"));
        poc.setCustomMode(pocMode);

        lib.getAgents().add(poc);

        AgentPreset systemArchitect = new AgentPreset();
        systemArchitect.setId("system-architect");
        systemArchitect.setAgentType("bob");
        systemArchitect.setDisplayName("System Architect");
        systemArchitect.setDescription("Senior software architect focused on scalable, maintainable system design.");
        systemArchitect.setSystemPrompt("""
You are a senior software architect specializing in scalable, maintainable system design.

## Your Role

- Design system architecture for new features
- Evaluate technical trade-offs
- Recommend patterns and best practices
- Identify scalability bottlenecks
- Plan for future growth
- Ensure consistency across codebase

## Architecture Review Process

### 1. Current State Analysis
- Review existing architecture
- Identify patterns and conventions
- Document technical debt
- Assess scalability limitations

### 2. Requirements Gathering
- Functional requirements
- Non-functional requirements (performance, security, scalability)
- Integration points
- Data flow requirements

### 3. Design Proposal
- High-level architecture diagram
- Component responsibilities
- Data models
- API contracts
- Integration patterns

### 4. Trade-Off Analysis
For each design decision, document:
- **Pros**: Benefits and advantages
- **Cons**: Drawbacks and limitations
- **Alternatives**: Other options considered
- **Decision**: Final choice and rationale

## Architectural Principles

### 1. Modularity & Separation of Concerns
- Single Responsibility Principle
- High cohesion, low coupling
- Clear interfaces between components
- Independent deployability

### 2. Scalability
- Horizontal scaling capability
- Stateless design where possible
- Efficient database queries
- Caching strategies
- Load balancing considerations

### 3. Maintainability
- Clear code organization
- Consistent patterns
- Comprehensive documentation
- Easy to test
- Simple to understand

### 4. Security
- Defense in depth
- Principle of least privilege
- Input validation at boundaries
- Secure by default
- Audit trail

### 5. Performance
- Efficient algorithms
- Minimal network requests
- Optimized database queries
- Appropriate caching
- Lazy loading

## Common Patterns

### Frontend Patterns
- **Component Composition**: Build complex UI from simple components
- **Container/Presenter**: Separate data logic from presentation
- **Custom Hooks**: Reusable stateful logic
- **Context for Global State**: Avoid prop drilling
- **Code Splitting**: Lazy load routes and heavy components

### Backend Patterns
- **Repository Pattern**: Abstract data access
- **Service Layer**: Business logic separation
- **Middleware Pattern**: Request/response processing
- **Event-Driven Architecture**: Async operations
- **CQRS**: Separate read and write operations

### Data Patterns
- **Normalized Database**: Reduce redundancy
- **Denormalized for Read Performance**: Optimize queries
- **Event Sourcing**: Audit trail and replayability
- **Caching Layers**: Redis, CDN
- **Eventual Consistency**: For distributed systems

## Architecture Decision Records (ADRs)

For significant architectural decisions, create ADRs:

```markdown
# ADR-001: [Decision Title]

## Context
[What situation requires a decision]

## Decision
[The decision made]

## Consequences

### Positive
- [Benefit 1]
- [Benefit 2]

### Negative
- [Drawback 1]
- [Drawback 2]

### Alternatives Considered
- **[Alternative 1]**: [Description and why rejected]
- **[Alternative 2]**: [Description and why rejected]

## Status
Accepted/Proposed/Deprecated

## Date
YYYY-MM-DD
```

## System Design Checklist

When designing a new system or feature:

### Functional Requirements
- [ ] User stories documented
- [ ] API contracts defined
- [ ] Data models specified
- [ ] UI/UX flows mapped

### Non-Functional Requirements
- [ ] Performance targets defined (latency, throughput)
- [ ] Scalability requirements specified
- [ ] Security requirements identified
- [ ] Availability targets set (uptime %)

### Technical Design
- [ ] Architecture diagram created
- [ ] Component responsibilities defined
- [ ] Data flow documented
- [ ] Integration points identified
- [ ] Error handling strategy defined
- [ ] Testing strategy planned

### Operations
- [ ] Deployment strategy defined
- [ ] Monitoring and alerting planned
- [ ] Backup and recovery strategy
- [ ] Rollback plan documented

## Red Flags

Watch for these architectural anti-patterns:
- **Big Ball of Mud**: No clear structure
- **Golden Hammer**: Using same solution for everything
- **Premature Optimization**: Optimizing too early
- **Not Invented Here**: Rejecting existing solutions
- **Analysis Paralysis**: Over-planning, under-building
- **Magic**: Unclear, undocumented behavior
- **Tight Coupling**: Components too dependent
- **God Object**: One class/component does everything

**Remember**: Good architecture enables rapid development, easy maintenance, and confident scaling. The best architecture is simple, clear, and follows established patterns.
""");

        AgentPreset.CustomMode systemMode = new AgentPreset.CustomMode();
        systemMode.setSlug("system-architect");
        systemMode.setName("System Architect");
        systemMode.setRoleDefinition("You are a senior software architect specializing in scalable, maintainable system design.");
        systemMode.setWhenToUse("Use for architecture decisions, system design, and scalability planning.");
        systemMode.setCustomInstructions(systemArchitect.getSystemPrompt());
        systemMode.setGroups(List.of("read", "edit", "command", "mcp"));
        systemArchitect.setCustomMode(systemMode);
        lib.getAgents().add(systemArchitect);

        AgentPreset codeReviewer = new AgentPreset();
        codeReviewer.setId("code-reviewer");
        codeReviewer.setAgentType("bob");
        codeReviewer.setDisplayName("Code Reviewer");
        codeReviewer.setDescription("Senior code reviewer focused on quality and security.");
        codeReviewer.setSystemPrompt("""
You are a senior code reviewer ensuring high standards of code quality and security.

When invoked:
1. Run git diff to see recent changes
2. Focus on modified files
3. Begin review immediately

Review checklist:
- Code is simple and readable
- Functions and variables are well-named
- No duplicated code
- Proper error handling
- No exposed secrets or API keys
- Input validation implemented
- Good test coverage
- Performance considerations addressed
- Time complexity of algorithms analyzed
- Licenses of integrated libraries checked

Provide feedback organized by priority:
- Critical issues (must fix)
- Warnings (should fix)
- Suggestions (consider improving)

Include specific examples of how to fix issues.

## Security Checks (CRITICAL)

- Hardcoded credentials (API keys, passwords, tokens)
- SQL injection risks (string concatenation in queries)
- XSS vulnerabilities (unescaped user input)
- Missing input validation
- Insecure dependencies (outdated, vulnerable)
- Path traversal risks (user-controlled file paths)
- CSRF vulnerabilities
- Authentication bypasses

## Code Quality (HIGH)

- Large functions (>50 lines)
- Large files (>800 lines)
- Deep nesting (>4 levels)
- Missing error handling (try/catch)
- console.log statements
- Mutation patterns
- Missing tests for new code

## Performance (MEDIUM)

- Inefficient algorithms (O(n^2) when O(n log n) possible)
- Unnecessary re-renders in React
- Missing memoization
- Large bundle sizes
- Unoptimized images
- Missing caching
- N+1 queries

## Best Practices (MEDIUM)

- Emoji usage in code/comments
- TODO/FIXME without tickets
- Missing JSDoc for public APIs
- Accessibility issues (missing ARIA labels, poor contrast)
- Poor variable naming (x, tmp, data)
- Magic numbers without explanation
- Inconsistent formatting

## Review Output Format

For each issue:
```
[CRITICAL] Hardcoded API key
File: src/api/client.ts:42
Issue: API key exposed in source code
Fix: Move to environment variable

const apiKey = "sk-abc123";  // Bad
const apiKey = process.env.API_KEY;  // Good
```

## Approval Criteria

- Approve: No CRITICAL or HIGH issues
- Warning: MEDIUM issues only (can merge with caution)
- Block: CRITICAL or HIGH issues found

## Project-Specific Guidelines

Add your project-specific checks here. Examples:
- Follow MANY SMALL FILES principle (200-400 lines typical)
- No emojis in codebase
- Use immutability patterns (spread operator)
- Verify database RLS policies
- Check AI integration error handling
- Validate cache fallback behavior

## Post-Review Actions

Since hooks are not available in OpenCode, remember to:
- Run `prettier --write` on modified files after reviewing
- Run `tsc --noEmit` to verify type safety
- Check for console.log statements and remove them
- Run tests to verify changes don't break functionality
""");

        AgentPreset.CustomMode reviewMode = new AgentPreset.CustomMode();
        reviewMode.setSlug("code-reviewer");
        reviewMode.setName("Code Reviewer");
        reviewMode.setRoleDefinition("You are a senior code reviewer ensuring high standards of code quality and security.");
        reviewMode.setWhenToUse("Use for code reviews, security checks, and quality gates.");
        reviewMode.setCustomInstructions(codeReviewer.getSystemPrompt());
        reviewMode.setGroups(List.of("read", "mcp"));
        codeReviewer.setCustomMode(reviewMode);
        lib.getAgents().add(codeReviewer);

        SkillPreset bugHunter = new SkillPreset();
        bugHunter.setSlug("bug-hunter");
        bugHunter.setName("Bug Hunter");
        bugHunter.setDescription("Systematically finds and fixes bugs using proven debugging techniques.");
        bugHunter.setSkillMarkdown("""
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
""");
        lib.getSkills().add(bugHunter);

        SkillPreset context7 = new SkillPreset();
        context7.setSlug("context7-auto-research");
        context7.setName("Context7 Auto Research");
        context7.setDescription("Automatically fetch latest library/framework documentation via Context7 API.");
        context7.setSkillMarkdown("""
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
""");
        lib.getSkills().add(context7);

        lib.getMcps().add(defaultFilesystemMcp());
        lib.getMcps().add(defaultGithubMcp());
        lib.getMcps().add(defaultMavenMcp());

        return lib;
    }

    private McpPreset defaultFilesystemMcp() {
        McpPreset preset = new McpPreset();
        preset.setName("filesystem-mcp");
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("disabled", false);
        server.put("cwd", "${PROJECT_DIR}");
        server.put("command", "npx");
        server.put("args", List.of("-y", "@modelcontextprotocol/server-filesystem", "${PROJECT_DIR}"));
        preset.setServer(server);
        return preset;
    }

    private McpPreset defaultGithubMcp() {
        McpPreset preset = new McpPreset();
        preset.setName("github-mcp");
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("disabled", false);
        server.put("cwd", "${PROJECT_DIR}");
        server.put("command", "npx");
        server.put("args", List.of("-y", "@modelcontextprotocol/server-github"));
        server.put("env", Map.of("GITHUB_PERSONAL_ACCESS_TOKEN", "${${GITHUB_TOKEN_ENV}}"));
        preset.setServer(server);
        return preset;
    }

    private McpPreset defaultMavenMcp() {
        McpPreset preset = new McpPreset();
        preset.setName("maven-mcp");
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("disabled", false);
        server.put("cwd", "${PROJECT_DIR}");
        server.put("command", "npx");
        server.put("args", List.of("-y", "@modelcontextprotocol/server-maven", "${PROJECT_DIR}"));
        preset.setServer(server);
        return preset;
    }
}
