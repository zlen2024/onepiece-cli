package com.nel.onepiece.ai;

import com.nel.onepiece.model.ProjectAnalysis;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Implementation of ProjectAnalyzerAI that uses dynamic AI provider configuration
 */
@ApplicationScoped
public class ProjectAnalyzerAIImpl {

    @Inject
    AIProviderService aiProviderService;

    private ProjectAnalyzerAI cachedAiService;

    /**
     * Get or create the AI service with current provider configuration
     */
    private ProjectAnalyzerAI getAiService() {
        // Always create a new service to ensure we use the latest configuration
        ChatLanguageModel chatModel = aiProviderService.getChatModel();
        
        cachedAiService = AiServices.builder(ProjectAnalyzerAI.class)
            .chatLanguageModel(chatModel)
            .build();
            
        return cachedAiService;
    }

    /**
     * Analyze a project directory and provide recommendations
     */
    public ProjectAnalysis analyzeProject(String projectStructure) {
        return getAiService().analyzeProject(projectStructure);
    }

    /**
     * Generate a system prompt for the AI agent based on project analysis
     */
    public String generateSystemPrompt(ProjectAnalysis analysis, String agentType) {
        return getAiService().generateSystemPrompt(analysis, agentType);
    }

    /**
     * Recommend skills for the AI agent based on project type
     */
    public String recommendSkills(ProjectAnalysis analysis) {
        return getAiService().recommendSkills(analysis);
    }

    /**
     * Clear cached AI service (force recreation with new configuration)
     */
    public void clearCache() {
        cachedAiService = null;
    }
}

// Made with Bob