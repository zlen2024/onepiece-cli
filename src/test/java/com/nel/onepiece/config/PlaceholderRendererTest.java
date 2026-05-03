package com.nel.onepiece.config;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlaceholderRendererTest {

    @Test
    void rendersKnownPlaceholdersAndKeepsEnvRefs() {
        Map<String, String> placeholders = Map.of(
            "PROJECT_DIR", "/tmp/project",
            "GITHUB_TOKEN_ENV", "GITHUB_TOKEN"
        );
        PlaceholderRenderer renderer = new PlaceholderRenderer(placeholders);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("cwd", "${PROJECT_DIR}");
        input.put("args", List.of("${PROJECT_DIR}", "x"));
        input.put("env", Map.of(
            "GITHUB_PERSONAL_ACCESS_TOKEN", "${${GITHUB_TOKEN_ENV}}",
            "RAW", "${GITHUB_TOKEN}"
        ));

        Map<String, Object> out = renderer.renderMap(input);
        assertEquals("/tmp/project", out.get("cwd"));
        assertEquals(List.of("/tmp/project", "x"), out.get("args"));

        @SuppressWarnings("unchecked")
        Map<String, Object> env = (Map<String, Object>) out.get("env");
        assertEquals("${GITHUB_TOKEN}", env.get("GITHUB_PERSONAL_ACCESS_TOKEN"));
        assertEquals("${GITHUB_TOKEN}", env.get("RAW"));
    }
}

