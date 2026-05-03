package com.nel.onepiece.ai;

import com.nel.onepiece.model.ProjectAnalysis;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * AI service for analyzing project structure and recommending configurations.
 * Uses LangChain4j to interact with OpenAI GPT-4.
 */
@RegisterAiService
public interface ProjectAnalyzerAI {

    /**
     * Analyze a project directory and provide recommendations
     * 
     * @param projectStructure String representation of the project structure
     * @return ProjectAnalysis with framework, language, and MCP recommendations
     */
    @SystemMessage("""
        You are an expert software architect and DevOps engineer specializing in project analysis.
        Your task is to analyze project structures and recommend the best AI agent configuration.
        
        Analyze the provided project structure and determine:
        1. The programming language (Java, Python, JavaScript, etc.)
        2. The framework being used (Quarkus, Spring Boot, Express, Django, etc.)
        3. The build tool (Maven, Gradle, npm, pip, etc.)
        4. The project type (web application, CLI tool, library, microservice, etc.)
        5. Recommended MCP (Model Context Protocol) servers that would be useful
        
        For MCP recommendations, consider:
        - filesystem-mcp: Always useful for file operations
        - github-mcp: If there's a .git directory
        - maven-mcp: For Maven-based Java projects
        - gradle-mcp: For Gradle-based projects
        - npm-mcp: For Node.js projects
        - postgres-mcp: If database configuration is detected
        - docker-mcp: If Dockerfile is present
        
        Respond with a structured analysis in JSON format matching the ProjectAnalysis schema.
        """)
    @UserMessage("""
        Analyze this project structure:
        
        {{projectStructure}}
        
        Provide a detailed analysis with framework detection and MCP recommendations.
        """)
    ProjectAnalysis analyzeProject(String projectStructure);

    /**
     * Generate a system prompt for the AI agent based on project analysis
     * 
     * @param analysis The project analysis
     * @param agentType The type of AI agent (bob, claudecode, etc.)
     * @return A customized system prompt
     */
    @SystemMessage("""
        You are an AI prompt engineer specializing in creating effective system prompts
        for AI coding agents. Create a comprehensive system prompt that will help the
        AI agent understand the project context and work effectively.
        """)
    @UserMessage("""
        Create a system prompt for {{agentType}} agent working on a {{analysis.framework}} project.
        
        Project details:
        - Language: {{analysis.language}}
        - Build tool: {{analysis.buildTool}}
        - Type: {{analysis.projectType}}
        
        The prompt should:
        1. Explain the project structure
        2. Highlight key conventions and patterns
        3. Provide guidance on best practices
        4. Mention available MCP servers and their purposes
        
        Keep it concise but informative (max 500 words).
        """)
    String generateSystemPrompt(ProjectAnalysis analysis, String agentType);

    /**
     * Recommend skills for the AI agent based on project type
     * 
     * @param analysis The project analysis
     * @return Comma-separated list of recommended skills
     */
    @SystemMessage("""
        You are an expert in AI agent capabilities and skills. Recommend the most
        useful skills for an AI agent working on a specific type of project.
        
        Available skills include:
        - code-review: Analyze code quality and suggest improvements
        - test-generation: Generate unit and integration tests
        - documentation: Create and update documentation
        - refactoring: Suggest and implement code refactoring
        - debugging: Help identify and fix bugs
        - api-design: Design RESTful APIs
        - database-design: Design database schemas
        - security-audit: Identify security vulnerabilities
        """)
    @UserMessage("""
        Recommend skills for an AI agent working on:
        - Framework: {{analysis.framework}}
        - Language: {{analysis.language}}
        - Project type: {{analysis.projectType}}
        
        Return only a comma-separated list of skill names, no explanations.
        Example: code-review,test-generation,documentation
        """)
    String recommendSkills(ProjectAnalysis analysis);
}

// Made with Bob
