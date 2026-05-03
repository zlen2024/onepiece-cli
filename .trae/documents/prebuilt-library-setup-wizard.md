# Plan: Prebuilt Library + Setup Wizard

## Summary
Implement a global “prebuilt library” stored under `~/.onepiece` that contains:
- Prebuilt Bob agent presets (system prompt + matching Bob custom mode entry)
- Prebuilt skill presets (prompt text to write into `.bob/skills/<slug>/SKILL.md`)
- Preconfigured MCP server presets (server definitions for `.bob/mcp.json`, with placeholder patching)

Update the interactive Setup UX to a stateful wizard:
- `Setup > IBM Bob` opens a page with `> agent`, `> skills`, `> mcp`, `> generate`, `> start-bob`
- After choosing/building an agent, return to that page (do not exit early)
- Add Settings screens to manage (add/list/remove) prebuilt agents, skills, and MCP presets

## Current State Analysis (Repo Facts)
- User config is stored in `~/.onepiece/config.json` via [ConfigManager](file:///workspace/src/main/java/com/nel/onepiece/config/ConfigManager.java) and [OnePieceConfig](file:///workspace/src/main/java/com/nel/onepiece/model/config/OnePieceConfig.java).
- Interactive UI is numeric-input (not arrow-key) via [InteractiveMenu](file:///workspace/src/main/java/com/nel/onepiece/ui/InteractiveMenu.java).
- Setup is currently a single-pass flow inside [SetupCommand](file:///workspace/src/main/java/com/nel/onepiece/commands/SetupCommand.java): pick agent → analyze → maybe pick workflow/skills/mcps → write config → show completion.
- Bob config generation writes:
  - `.bob.workspace`, `.bob/mcp.json`, `.bob/custom_modes.yaml`, `.bob/rules/*`, `.bob/skills/*` from [ConfigurationGenerator](file:///workspace/src/main/java/com/nel/onepiece/config/ConfigurationGenerator.java)
- Settings currently supports Vault + AI Provider only in [SettingsCommand](file:///workspace/src/main/java/com/nel/onepiece/commands/SettingsCommand.java).

## Goals / Success Criteria
- A global library exists at `~/.onepiece` and is editable through Settings:
  - JSON source-of-truth: `~/.onepiece/presets.json`
  - Export/import directory: `~/.onepiece/templates/` (round-trip supported)
- Setup UX:
  - `Setup > IBM Bob` shows a wizard page with actionable items: agent/skills/mcp/generate/start-bob/back
  - Selecting agent does not trigger generation; it returns to the wizard page
  - User can choose a prebuilt agent or create “custom-agent” (AI generates system prompt only, fallback to manual input if AI fails)
  - After `Generate`, show summary and then return to main menu
  - `Start-bob` prints instructions by default (as chosen)
- Bug fix:
  - Prevent early exit or auto-generation that skips the skills/MCP selection step.

## Out of Scope (Explicit)
- Full support for ClaudeCode/OpenCode/Pi output formats (schema remains extensible; implementation is Bob-only for now).
- Actually executing `bob start` automatically (we print instructions; optional confirmation can be added later).

## Proposed Changes (Decision-Complete)

### 1) Add a global presets store under `~/.onepiece`
**New file paths**
- `~/.onepiece/presets.json` (source of truth)
- `~/.onepiece/templates/` (exported templates directory)

**New Java types**
- `com.nel.onepiece.model.presets.PresetLibrary`
  - `version`
  - `agents` (list of `AgentPreset`)
  - `skills` (list of `SkillPreset`)
  - `mcps` (list of `McpPreset`)
- `AgentPreset` (Bob-focused initially)
  - `id` (stable slug)
  - `displayName`
  - `description`
  - `agentType` (enum/string; for now only `"bob"`)
  - `systemPrompt` (string)
  - `customMode` (object with `slug`, `name`, `roleDefinition`, `whenToUse`, `customInstructions`, `groups`)
- `SkillPreset`
  - `slug`
  - `name`
  - `description`
  - `skillMarkdown` (contents for `SKILL.md`)
- `McpPreset`
  - `name`
  - `server` (JSON object matching `.bob/mcp.json` server config schema used by current generator)
  - `placeholders` supported in strings: `${PROJECT_DIR}`, `${GITHUB_TOKEN_ENV}`, `${DATABASE_URL_ENV}`, etc.

**New services**
- `com.nel.onepiece.config.PresetLibraryManager`
  - Loads/saves `~/.onepiece/presets.json`
  - If file missing: seeds with a small default library (equivalent to today’s “recommended defaults”)
  - Export: writes `~/.onepiece/templates/index.json` plus materialized prompt files (agent/systemPrompt.txt, skill/SKILL.md, mcp/server.json)
  - Import: rebuilds `presets.json` from `templates/` (validates structure)

**Why separate file**
- Keeps secrets in `config.json` (aiProvider/vault) separated from large editable presets content.

### 2) Implement placeholder patching for MCP presets
**New utility**
- `com.nel.onepiece.config.PlaceholderRenderer`
  - Walk a `Map<String, Object>` recursively and replace placeholders in strings
  - Enforce that only known placeholders are allowed (fail with clear error otherwise)
  - Inputs include:
    - `PROJECT_DIR` = selected project path
    - Token env var names chosen during setup (GitHub/Postgres)

**Used by**
- Setup when materializing `.bob/mcp.json` from selected MCP presets.

### 3) Refactor Setup into a wizard page with state (Bob-only)
**Main changes**
- Update [SetupCommand](file:///workspace/src/main/java/com/nel/onepiece/commands/SetupCommand.java) to:
  - Keep a `SetupState` object:
    - selected agent preset (or custom-agent prompt result)
    - selected skills (list of skill preset slugs)
    - selected MCP presets (list of mcp preset names)
    - env var names mapping (GitHub/Postgres)
  - Provide a looped page:
    - `1) Agent` → list presets from `PresetLibraryManager` + `Custom Agent`
    - `2) Skills` → multi-select list of skill presets (at minimum allow comma-separated selection)
    - `3) MCP` → select MCP presets; prompt for env var names if needed
    - `4) Generate` → write `.bob/*` + `.onepiece/*`
    - `5) Start-bob` → print instructions
    - `6) Back` → return to main menu without changes

**Custom-agent flow (AI generates system prompt only)**
- If AI provider configured:
  - Prompt user: “describe your agent” (free text)
  - Call a new AI service `AgentPresetBuilderAI` to return `displayName` + `systemPrompt` (JSON output)
  - Convert to an `AgentPreset` in-memory and optionally offer: “save to library?” (yes → write to presets.json)
- If AI provider missing/fails:
  - Ask user for `displayName` and `systemPrompt` manually.

**Bug fix (from your report)**
- Ensure no code path calls “generate files” immediately after agent selection; generation happens only when user selects `Generate`.
- After agent selection, wizard returns to the same page showing updated counters: `agent ✓`, `skills N`, `mcp N`.

### 4) Generate Bob config from presets
**Update generator APIs**
- Extend [ConfigurationGenerator](file:///workspace/src/main/java/com/nel/onepiece/config/ConfigurationGenerator.java) with Bob-specific methods that accept:
  - agent preset (systemPrompt + customMode)
  - selected skill presets (to write `SKILL.md`)
  - selected MCP servers (materialized via placeholder patching)
- Generation rules:
  - `.bob.workspace`: set systemPrompt from agent preset; keep model params from configured provider (existing behavior)
  - `.bob/custom_modes.yaml`: generate from selected agent preset’s `customMode` plus any default modes you want to keep
  - `.bob/skills/**`: write SKILL.md for each selected skill preset
  - `.bob/mcp.json`: write servers from selected MCP presets, patched with placeholders

### 5) Add Settings screens to manage presets library
**Update**
- [SettingsCommand](file:///workspace/src/main/java/com/nel/onepiece/commands/SettingsCommand.java)

**Add menu option**
- “📚 Presets Library”

**Submenus (MVP)**
- Agents:
  - List agents
  - Add agent (manual)
  - Delete agent
- Skills:
  - List skills
  - Add skill (paste SKILL.md content)
  - Delete skill
- MCP:
  - List MCP presets
  - Add MCP preset (guided prompts to build server JSON; support placeholders)
  - Delete MCP preset
- Import/Export:
  - Export to `~/.onepiece/templates/`
  - Import from `~/.onepiece/templates/` (with validation + confirmation)

### 6) Logging + error handling
- Keep interactive output clean; on failures:
  - show user-friendly messages via `ColorFormatter`
  - do not dump stack traces unless a debug flag is added later
- For malformed presets.json/templates:
  - show exact file path and the field that failed validation

## Assumptions & Decisions (Locked)
- Storage choice: both `presets.json` and `templates/` export/import.
- Initial scope: Bob only.
- Custom-agent: AI generates system prompt only (manual fallback).
- Start-bob: print instructions by default.
- Agent preset for Bob affects both `.bob.workspace` (system prompt) and `.bob/custom_modes.yaml` (custom mode).
- Skill preset writes `.bob/skills/<slug>/SKILL.md`.
- MCP preset supports placeholder patching.

## Verification Plan
1. Unit tests (add `src/test/java`):
   - Placeholder renderer replaces `${PROJECT_DIR}` correctly in nested maps/lists.
   - PresetLibraryManager round-trips `presets.json` load/save with a temp directory.
2. Manual interactive checks:
   - Run `onepiece` → Setup → IBM Bob:
     - selecting agent returns to wizard page (does not exit)
     - selecting skills/mcp updates counts
     - Generate writes `.bob/*` as expected
     - Start-bob prints the instructions and stays in wizard/menu correctly
3. Regression check:
   - If AI provider is missing or returns 502, custom-agent falls back to manual prompt entry and setup still works.

