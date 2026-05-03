package com.nel.onepiece.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AgentPresetBuilderAI {

    @SystemMessage("""
        You are an expert prompt engineer for coding agents.
        Return STRICT JSON with keys: displayName, systemPrompt.
        displayName must be short (2-6 words). systemPrompt must be concise but actionable (max 250 words).
        """)
    @UserMessage("""
        Project context:
        {{projectContext}}

        User request for the agent:
        {{userRequest}}

        Return JSON only.
        """)
    String buildAgent(String projectContext, String userRequest);
}

