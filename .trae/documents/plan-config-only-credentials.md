# Plan: Remove .env Usage, Use Global Config for IBM Cloud Credentials

## Summary

Update OnePiece CLI so it no longer generates or recommends `.env` / `.env.example`, and so IBM Cloud deployment reads reusable IBM Cloud credentials only from `~/.onepiece/config.json`. Also fix the highest-impact IBM Cloud deploy bugs discovered during inspection (incorrect “live URL” output and manifest issues).

## Current State Analysis (Grounded)

### Configuration storage

- Global config file location is `~/.onepiece/config.json`, managed by [ConfigManager.java](file:///workspace/src/main/java/com/nel/onepiece/config/ConfigManager.java#L17-L186) using the typed model [OnePieceConfig.java](file:///workspace/src/main/java/com/nel/onepiece/model/config/OnePieceConfig.java#L1-L60).
- AI provider credentials already live in this config file under `aiProvider` ([AIProviderConfig.java](file:///workspace/src/main/java/com/nel/onepiece/model/config/AIProviderConfig.java#L10-L129)), and are consumed directly by runtime code ([AIProviderService.java](file:///workspace/src/main/java/com/nel/onepiece/ai/AIProviderService.java#L18-L122)).
- Vault config is also stored in `~/.onepiece/config.json` under `vault` ([VaultConfig.java](file:///workspace/src/main/java/com/nel/onepiece/model/config/VaultConfig.java#L8-L57)), currently used by deploy to fetch IBM Cloud credentials from Vault.

### IBM Cloud deploy credential sourcing (problem)

- Deploy currently uses **Vault** when configured, otherwise falls back to environment variables `IBM_CLOUD_API_KEY` and `IBM_CLOUD_REGION` ([DeployCommand.java](file:///workspace/src/main/java/com/nel/onepiece/commands/DeployCommand.java#L201-L230)).
- User requirement: stop using `.env` and remove reliance on `IBM_CLOUD_*` env vars; credentials should be reusable and stored only in `~/.onepiece/config.json`.

### .env usage (problem)

- Setup always generates `.env.example` via [ConfigurationGenerator.generateEnvExample](file:///workspace/src/main/java/com/nel/onepiece/config/ConfigurationGenerator.java#L221-L251) and calls it from the setup wizard ([SetupCommand.java](file:///workspace/src/main/java/com/nel/onepiece/commands/SetupCommand.java#L914-L937)).
- Settings menu includes a “Local .env file” alternative prompt and prints `.env` instructions ([SettingsCommand.java](file:///workspace/src/main/java/com/nel/onepiece/commands/SettingsCommand.java#L230-L248)).
- Documentation includes a `.env.example` template section ([CONFIGURATION_FILES.md](file:///workspace/CONFIGURATION_FILES.md#L1018-L1049)).

### High-impact IBM Cloud deploy bugs (bug-hunter findings)

- **Wrong URL printed**: manifest generation forces `random-route: true` ([IbmCloudExecutor.generateManifest](file:///workspace/src/main/java/com/nel/onepiece/deployment/IbmCloudExecutor.java#L253-L272)) but deploy prints a deterministic URL `https://{app}.{region}.cf.appdomain.cloud` ([DeployCommand.java](file:///workspace/src/main/java/com/nel/onepiece/commands/DeployCommand.java#L320-L323)). This commonly yields a dead link.
- **Manifest path likely wrong**: manifest writes `path: .`, which may push sources instead of the built artifact and cause staging/buildpack failures ([IbmCloudExecutor.generateManifest](file:///workspace/src/main/java/com/nel/onepiece/deployment/IbmCloudExecutor.java#L253-L272)).

## Assumptions & Decisions (Locked)

- Global config stores **reusable credentials** only (IBM Cloud API key and optional defaults like org/space); project-specific deployment details (app name, project dir) remain CLI inputs.
- IBM Cloud deploy reads credentials from `~/.onepiece/config.json` only (no Vault and no `IBM_CLOUD_*` env fallback).
- Other integration secrets (GitHub token, database URL) remain OS environment variables (no `.env` file generation; users can export in their shell/CI).

## Proposed Changes (What/Why/How)

### 1) Add IBM Cloud credentials to global config model

**Files**
- Add new model: `src/main/java/com/nel/onepiece/model/config/IbmCloudConfig.java`
- Update: [OnePieceConfig.java](file:///workspace/src/main/java/com/nel/onepiece/model/config/OnePieceConfig.java)
- Update: [ConfigManager.java](file:///workspace/src/main/java/com/nel/onepiece/config/ConfigManager.java)

**How**
- `IbmCloudConfig` fields:
  - `apiKey` (required)
  - `region` (optional default)
  - `org` / `space` (optional defaults for `ibmcloud target --cf`)
- Provide `isConfigured()` and `getMaskedApiKey()` (mirrors AI provider masking approach).
- Add `@JsonProperty("ibmCloud") private IbmCloudConfig ibmCloud;` to `OnePieceConfig`, plus `hasIbmCloud()`.
- Add `getIbmCloudConfig()`, `updateIbmCloudConfig()`, `hasIbmCloud()` to `ConfigManager`.

**Why**
- Align IBM Cloud credentials storage with how AI provider credentials are handled: global, reusable, typed, and saved with secure file permissions.

### 2) Update settings UX to manage IBM Cloud credentials (no .env prompts)

**Files**
- Update: [SettingsCommand.java](file:///workspace/src/main/java/com/nel/onepiece/commands/SettingsCommand.java)

**How**
- Add a new settings menu entry: “IBM Cloud Deployment Credentials”.
- Interactive flow: prompt for API key (required), optional default region/org/space; save into config.json.
- Non-interactive flags (for CI/scriptability):
  - `--ibmcloud-api-key`
  - `--ibmcloud-region`
  - `--ibmcloud-org`
  - `--ibmcloud-space`
- Update `--show` output to include IBM Cloud status and masked key.
- Remove the “Local .env file” alternative UI and any `.env` instructions from settings.

### 3) Update deploy to read IBM Cloud credentials only from global config

**Files**
- Update: [DeployCommand.java](file:///workspace/src/main/java/com/nel/onepiece/commands/DeployCommand.java)

**How**
- Remove Vault/env credential paths.
- Read from `configManager.getIbmCloudConfig()`:
  - If missing/unconfigured: fail with a clear message instructing the user to run `onepiece settings` (and/or the new non-interactive flags).
- Keep deploy-time inputs as CLI-driven:
  - `--app-name` remains required-ish (interactive prompt otherwise).
  - `--region` keeps working as an override; if absent, use config default region, else default to `us-south`.
- If org/space are set in config, run CF targeting (same behavior as today, but sourced from config).

### 4) Remove `.env.example` generation and references

**Files**
- Update: [SetupCommand.java](file:///workspace/src/main/java/com/nel/onepiece/commands/SetupCommand.java)
- Update: [ConfigurationGenerator.java](file:///workspace/src/main/java/com/nel/onepiece/config/ConfigurationGenerator.java)
- Update docs:
  - [CONFIGURATION_FILES.md](file:///workspace/CONFIGURATION_FILES.md)
  - [README.md](file:///workspace/README.md) (update deploy description so it doesn’t claim Vault/.env behavior)

**How**
- Stop calling `configurationGenerator.generateEnvExample(...)` in setup.
- Either delete `generateEnvExample` method or leave it unused but remove it from docs; prefer removing to avoid drift.
- Remove `.env.example` section from docs and any instructions that recommend `.env`.

### 5) Fix IBM Cloud deploy “live URL” + manifest issues (bug-hunter remediation)

**Files**
- Update: [DeployCommand.java](file:///workspace/src/main/java/com/nel/onepiece/commands/DeployCommand.java)
- Update: [IbmCloudExecutor.java](file:///workspace/src/main/java/com/nel/onepiece/deployment/IbmCloudExecutor.java)

**How (URL correctness)**
- After push, call `ibmcloud cf app <app>` and parse the `routes:` line(s) to extract the real route hostname.
- Print the URL(s) derived from actual CF output instead of fabricating `{app}.{region}.cf.appdomain.cloud`.

**How (manifest path)**
- Defer manifest generation until after build, then set `path` to a detected artifact when applicable:
  - Maven: prefer a single `target/*.jar` (skip `*-sources.jar`, `*-javadoc.jar`)
  - Gradle: prefer `build/libs/*.jar`
  - npm/static: keep `path: .` (unless a known `dist/` exists; if it does, use `dist/`)
- Keep buildpack selection logic as-is.

**Note**
- This keeps deploy output correct regardless of `random-route` choice; if `random-route` remains enabled, the printed route will still be accurate.

## Verification Steps

- Unit/compile verification:
  - Run the project test suite (or at minimum build) to ensure no compilation errors after adding the new config model and wiring.
- Behavioral checks (manual/CLI):
  - `onepiece settings --show` shows IBM Cloud section with masked key.
  - Deploy fails with a clear message when IBM creds are not configured in `~/.onepiece/config.json`.
  - Deploy prints real routes parsed from `ibmcloud cf app` output (not a fabricated URL).
- Regression checks:
  - Existing AI provider config continues to load/save correctly.
  - Setup no longer generates `.env.example` and docs no longer reference it.

