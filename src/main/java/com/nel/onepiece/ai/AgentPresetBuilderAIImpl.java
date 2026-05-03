package com.nel.onepiece.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AgentPresetBuilderAIImpl {

    @Inject
    AIProviderService aiProviderService;

    private AgentPresetBuilderAI createService() {
        ChatLanguageModel chatModel = aiProviderService.getChatModel();
        return AiServices.builder(AgentPresetBuilderAI.class)
            .chatLanguageModel(chatModel)
            .build();
    }

    public String buildAgent(String projectContext, String userRequest) {
        return createService().buildAgent(projectContext, userRequest);
    }
}

