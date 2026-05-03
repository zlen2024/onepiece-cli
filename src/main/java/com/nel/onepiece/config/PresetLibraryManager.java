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
                return objectMapper.readValue(file.toFile(), PresetLibrary.class);
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

        SkillPreset bugHunter = new SkillPreset();
        bugHunter.setSlug("bug-hunter");
        bugHunter.setName("Bug Hunter");
        bugHunter.setDescription("Systematically reproduce and fix bugs with evidence.");
        bugHunter.setSkillMarkdown("""
---
name: bug-hunter
description: Systematically reproduce, trace, and fix bugs with evidence.
---

1. Reproduce the bug with clear steps and capture logs/output.
2. Identify the failing component and trace to root cause.
3. Implement the smallest safe fix.
4. Add a regression check (test or reproducible command).
5. Summarize the change and verification results.
""");
        lib.getSkills().add(bugHunter);

        SkillPreset context7 = new SkillPreset();
        context7.setSlug("context7-auto-research");
        context7.setName("Context7 Auto Research");
        context7.setDescription("Fetch up-to-date docs for libraries/frameworks used in the project.");
        context7.setSkillMarkdown("""
---
name: context7-auto-research
description: Fetch up-to-date documentation for libraries/frameworks used in this project.
---

1. Identify the library/platform (Quarkus, LangChain4j, IBM Cloud, MCP).
2. Retrieve the most relevant docs for the exact versions and integration points.
3. Summarize best practices and required configuration.
4. Propose changes focused on functionality first.
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

