# 📄 One Piece CLI - Configuration File Formats Specification

## 1. Overview

One Piece CLI generates and manages multiple configuration files to orchestrate the AI development environment. This document defines the complete structure, validation rules, and examples for all configuration files.

### Configuration File Hierarchy

```
project-root/
├── .bob.workspace              # IBM Bob agent configuration
├── .onepiece/
│   ├── config.json            # User-level One Piece CLI settings
│   ├── project.json           # Project-level metadata
│   ├── mcp-registry.json      # MCP server registry
│   ├── skills-config.json     # Skills library configuration
│   └── deployment.json        # Deployment configuration
└── .env.example               # Environment variables template
```

---

## 2. .bob.workspace (IBM Bob Configuration)

### 2.1 Complete Schema

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/bob-workspace-v1.json",
  "version": "1.0",
  "workspace": {
    "name": "string",
    "root": "string (absolute path)",
    "language": "string",
    "framework": "string",
    "projectType": "string"
  },
  "mcpServers": {
    "serverName": {
      "command": "string",
      "args": ["string"],
      "env": {
        "KEY": "value"
      },
      "enabled": "boolean",
      "timeout": "number (seconds)",
      "retryAttempts": "number"
    }
  },
  "agent": {
    "codeStyle": "string",
    "focusAreas": ["string"],
    "testingLevel": "string",
    "documentationLevel": "string",
    "assistanceLevel": "string",
    "customInstructions": "string (optional)"
  },
  "skills": {
    "configPath": "string",
    "enabled": ["string"]
  },
  "settings": {
    "autoSave": "boolean",
    "formatOnSave": "boolean",
    "linting": "boolean",
    "autoImport": "boolean",
    "codeCompletion": {
      "enabled": "boolean",
      "triggerCharacters": ["string"]
    }
  },
  "excludePatterns": ["string"],
  "metadata": {
    "createdAt": "ISO 8601 timestamp",
    "updatedAt": "ISO 8601 timestamp",
    "generatedBy": "onepiece-cli",
    "version": "string"
  }
}
```

### 2.2 Field Definitions

#### **workspace**
- `name`: Project name (alphanumeric, hyphens, underscores)
- `root`: Absolute path to project root directory
- `language`: Primary language (Java, JavaScript, Python, TypeScript, Go, Rust, etc.)
- `framework`: Framework name (Quarkus, Spring Boot, React, Express, Django, etc.)
- `projectType`: Type of project (rest-api, web-app, microservice, library, cli-tool, etc.)

#### **mcpServers**
Each MCP server configuration includes:
- `command`: Executable command (npx, java, python, etc.)
- `args`: Array of command-line arguments
- `env`: Environment variables (use ${VAR} for references)
- `enabled`: Whether the MCP is active (default: true)
- `timeout`: Connection timeout in seconds (default: 30)
- `retryAttempts`: Number of retry attempts on failure (default: 3)

#### **agent**
- `codeStyle`: Coding style preference
  - Values: `enterprise-java`, `modern-javascript`, `pythonic`, `minimal`, `verbose`
- `focusAreas`: Array of focus areas
  - Values: `security`, `performance`, `testing`, `documentation`, `maintainability`, `scalability`
- `testingLevel`: Testing approach
  - Values: `minimal`, `standard`, `comprehensive`, `tdd`
- `documentationLevel`: Documentation detail
  - Values: `minimal`, `standard`, `comprehensive`, `api-docs`
- `assistanceLevel`: Level of AI assistance
  - Values: `minimal`, `balanced`, `comprehensive`, `pair-programming`
- `customInstructions`: Optional free-form instructions for the agent

#### **skills**
- `configPath`: Path to skills configuration file (relative to project root)
- `enabled`: Array of enabled skill identifiers

#### **settings**
- `autoSave`: Auto-save files after changes
- `formatOnSave`: Format code on save
- `linting`: Enable linting
- `autoImport`: Automatically add imports
- `codeCompletion`: Code completion settings

#### **excludePatterns**
Array of glob patterns to exclude from agent access:
- `node_modules/**`
- `target/**`
- `.git/**`
- `*.log`

### 2.3 Complete Example

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/bob-workspace-v1.json",
  "version": "1.0",
  "workspace": {
    "name": "ecommerce-api",
    "root": "/home/user/projects/ecommerce-api",
    "language": "Java",
    "framework": "Quarkus",
    "projectType": "rest-api"
  },
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/home/user/projects/ecommerce-api"
      ],
      "env": {
        "ALLOWED_PATHS": "/home/user/projects/ecommerce-api",
        "DENIED_PATHS": "/home/user/projects/ecommerce-api/.git,/home/user/projects/ecommerce-api/target"
      },
      "enabled": true,
      "timeout": 30,
      "retryAttempts": 3
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "${GITHUB_TOKEN}"
      },
      "enabled": true,
      "timeout": 30,
      "retryAttempts": 3
    },
    "maven": {
      "command": "java",
      "args": [
        "-jar",
        "/home/user/.onepiece/mcp-servers/maven/maven-mcp-server.jar"
      ],
      "env": {
        "MAVEN_HOME": "${MAVEN_HOME}",
        "PROJECT_DIR": "/home/user/projects/ecommerce-api"
      },
      "enabled": true,
      "timeout": 60,
      "retryAttempts": 2
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "POSTGRES_CONNECTION_STRING": "${DATABASE_URL}"
      },
      "enabled": true,
      "timeout": 30,
      "retryAttempts": 3
    },
    "redis": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-redis"],
      "env": {
        "REDIS_URL": "${REDIS_URL}"
      },
      "enabled": true,
      "timeout": 30,
      "retryAttempts": 3
    }
  },
  "agent": {
    "codeStyle": "enterprise-java",
    "focusAreas": [
      "security",
      "performance",
      "testing",
      "documentation"
    ],
    "testingLevel": "comprehensive",
    "documentationLevel": "standard",
    "assistanceLevel": "balanced",
    "customInstructions": "Follow SOLID principles and use dependency injection. Prefer immutable objects where possible."
  },
  "skills": {
    "configPath": ".onepiece/skills-config.json",
    "enabled": [
      "java-enterprise-patterns",
      "rest-api-design",
      "database-schema-design",
      "security-best-practices",
      "payment-gateway-integration",
      "shopping-cart-logic",
      "order-processing-workflows"
    ]
  },
  "settings": {
    "autoSave": true,
    "formatOnSave": true,
    "linting": true,
    "autoImport": true,
    "codeCompletion": {
      "enabled": true,
      "triggerCharacters": [".", "@", "/"]
    }
  },
  "excludePatterns": [
    "node_modules/**",
    "target/**",
    ".git/**",
    "*.log",
    ".onepiece/cache/**"
  ],
  "metadata": {
    "createdAt": "2026-05-02T17:30:00Z",
    "updatedAt": "2026-05-02T17:30:00Z",
    "generatedBy": "onepiece-cli",
    "version": "1.0.0"
  }
}
```

### 2.4 Validation Rules

```java
public class BobWorkspaceValidator {
    
    public ValidationResult validate(BobWorkspaceConfig config) {
        List<String> errors = new ArrayList<>();
        
        // Required fields
        if (config.getVersion() == null) {
            errors.add("version is required");
        }
        
        if (config.getWorkspace() == null) {
            errors.add("workspace section is required");
        } else {
            validateWorkspace(config.getWorkspace(), errors);
        }
        
        // MCP servers
        if (config.getMcpServers() == null || config.getMcpServers().isEmpty()) {
            errors.add("At least one MCP server must be configured");
        } else {
            validateMcpServers(config.getMcpServers(), errors);
        }
        
        // Agent configuration
        if (config.getAgent() != null) {
            validateAgent(config.getAgent(), errors);
        }
        
        return errors.isEmpty() 
            ? ValidationResult.success() 
            : ValidationResult.failure(errors);
    }
    
    private void validateWorkspace(WorkspaceMetadata workspace, List<String> errors) {
        if (workspace.getName() == null || workspace.getName().isBlank()) {
            errors.add("workspace.name is required");
        }
        
        if (workspace.getRoot() == null || !Files.exists(Path.of(workspace.getRoot()))) {
            errors.add("workspace.root must be a valid directory path");
        }
        
        if (workspace.getLanguage() == null) {
            errors.add("workspace.language is required");
        }
    }
    
    private void validateMcpServers(Map<String, McpServerConfig> servers, List<String> errors) {
        for (Map.Entry<String, McpServerConfig> entry : servers.entrySet()) {
            String name = entry.getKey();
            McpServerConfig server = entry.getValue();
            
            if (server.getCommand() == null || server.getCommand().isBlank()) {
                errors.add(name + ": command is required");
            }
            
            if (server.getArgs() == null || server.getArgs().isEmpty()) {
                errors.add(name + ": args array is required");
            }
        }
    }
    
    private void validateAgent(AgentSettings agent, List<String> errors) {
        List<String> validCodeStyles = List.of(
            "enterprise-java", "modern-javascript", "pythonic", "minimal", "verbose"
        );
        
        if (agent.getCodeStyle() != null && 
            !validCodeStyles.contains(agent.getCodeStyle())) {
            errors.add("agent.codeStyle must be one of: " + validCodeStyles);
        }
        
        List<String> validTestingLevels = List.of(
            "minimal", "standard", "comprehensive", "tdd"
        );
        
        if (agent.getTestingLevel() != null && 
            !validTestingLevels.contains(agent.getTestingLevel())) {
            errors.add("agent.testingLevel must be one of: " + validTestingLevels);
        }
    }
}
```

---

## 3. .onepiece/mcp-registry.json (MCP Server Registry)

### 3.1 Complete Schema

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/mcp-registry-v1.json",
  "version": "1.0.0",
  "lastUpdated": "ISO 8601 timestamp",
  "servers": [
    {
      "name": "string",
      "displayName": "string",
      "description": "string",
      "type": "npm | jar | binary | docker",
      "package": "string (for npm)",
      "version": "string",
      "installed": "boolean",
      "installPath": "string (absolute path)",
      "command": "string",
      "args": ["string"],
      "requiresEnv": ["string"],
      "status": "active | inactive | error | installing",
      "lastVerified": "ISO 8601 timestamp",
      "healthCheck": {
        "endpoint": "string (optional)",
        "interval": "number (seconds)",
        "timeout": "number (seconds)"
      },
      "metadata": {
        "repository": "string (URL)",
        "documentation": "string (URL)",
        "license": "string",
        "author": "string"
      }
    }
  ],
  "bundles": [
    {
      "id": "string",
      "name": "string",
      "description": "string",
      "servers": ["string"]
    }
  ]
}
```

### 3.2 Complete Example

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/mcp-registry-v1.json",
  "version": "1.0.0",
  "lastUpdated": "2026-05-02T17:30:00Z",
  "servers": [
    {
      "name": "filesystem",
      "displayName": "Filesystem MCP",
      "description": "Provides file system operations (read, write, search, list)",
      "type": "npm",
      "package": "@modelcontextprotocol/server-filesystem",
      "version": "1.2.0",
      "installed": true,
      "installPath": "/usr/local/lib/node_modules/@modelcontextprotocol/server-filesystem",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem"],
      "requiresEnv": ["ALLOWED_PATHS"],
      "status": "active",
      "lastVerified": "2026-05-02T17:25:00Z",
      "healthCheck": {
        "interval": 300,
        "timeout": 10
      },
      "metadata": {
        "repository": "https://github.com/modelcontextprotocol/servers",
        "documentation": "https://modelcontextprotocol.io/docs/servers/filesystem",
        "license": "MIT",
        "author": "Anthropic"
      }
    },
    {
      "name": "github",
      "displayName": "GitHub MCP",
      "description": "GitHub API integration for repositories, issues, and PRs",
      "type": "npm",
      "package": "@modelcontextprotocol/server-github",
      "version": "2.0.1",
      "installed": true,
      "installPath": "/usr/local/lib/node_modules/@modelcontextprotocol/server-github",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "requiresEnv": ["GITHUB_PERSONAL_ACCESS_TOKEN"],
      "status": "active",
      "lastVerified": "2026-05-02T17:25:00Z",
      "healthCheck": {
        "interval": 300,
        "timeout": 10
      },
      "metadata": {
        "repository": "https://github.com/modelcontextprotocol/servers",
        "documentation": "https://modelcontextprotocol.io/docs/servers/github",
        "license": "MIT",
        "author": "Anthropic"
      }
    },
    {
      "name": "maven",
      "displayName": "Maven MCP",
      "description": "Maven build system integration for Java projects",
      "type": "jar",
      "version": "1.0.0",
      "installed": true,
      "installPath": "/home/user/.onepiece/mcp-servers/maven/maven-mcp-server.jar",
      "command": "java",
      "args": ["-jar", "/home/user/.onepiece/mcp-servers/maven/maven-mcp-server.jar"],
      "requiresEnv": ["MAVEN_HOME", "PROJECT_DIR"],
      "status": "active",
      "lastVerified": "2026-05-02T17:25:00Z",
      "healthCheck": {
        "interval": 300,
        "timeout": 15
      },
      "metadata": {
        "repository": "https://github.com/onepiece-cli/maven-mcp",
        "documentation": "https://onepiece-cli.dev/docs/mcps/maven",
        "license": "Apache-2.0",
        "author": "One Piece CLI Team"
      }
    },
    {
      "name": "postgres",
      "displayName": "PostgreSQL MCP",
      "description": "PostgreSQL database operations and schema management",
      "type": "npm",
      "package": "@modelcontextprotocol/server-postgres",
      "version": "1.5.3",
      "installed": true,
      "installPath": "/usr/local/lib/node_modules/@modelcontextprotocol/server-postgres",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "requiresEnv": ["POSTGRES_CONNECTION_STRING"],
      "status": "active",
      "lastVerified": "2026-05-02T17:25:00Z",
      "healthCheck": {
        "endpoint": "postgres://localhost:5432",
        "interval": 300,
        "timeout": 10
      },
      "metadata": {
        "repository": "https://github.com/modelcontextprotocol/servers",
        "documentation": "https://modelcontextprotocol.io/docs/servers/postgres",
        "license": "MIT",
        "author": "Anthropic"
      }
    },
    {
      "name": "docker",
      "displayName": "Docker MCP",
      "description": "Docker container management and operations",
      "type": "npm",
      "package": "@modelcontextprotocol/server-docker",
      "version": "1.3.0",
      "installed": false,
      "status": "inactive",
      "metadata": {
        "repository": "https://github.com/modelcontextprotocol/servers",
        "documentation": "https://modelcontextprotocol.io/docs/servers/docker",
        "license": "MIT",
        "author": "Anthropic"
      }
    }
  ],
  "bundles": [
    {
      "id": "java-enterprise",
      "name": "Java Enterprise Development",
      "description": "Complete setup for Java enterprise applications",
      "servers": ["filesystem", "github", "maven", "postgres", "docker"]
    },
    {
      "id": "nodejs-fullstack",
      "name": "Node.js Full-Stack",
      "description": "Full-stack JavaScript/TypeScript development",
      "servers": ["filesystem", "github", "npm", "postgres", "redis"]
    },
    {
      "id": "minimal",
      "name": "Minimal Setup",
      "description": "Basic file and git operations only",
      "servers": ["filesystem", "github"]
    }
  ]
}
```

---

## 4. .onepiece/skills-config.json (Skills Library)

### 4.1 Complete Schema

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/skills-config-v1.json",
  "version": "1.0.0",
  "lastUpdated": "ISO 8601 timestamp",
  "skills": [
    {
      "id": "string",
      "name": "string",
      "category": "string",
      "description": "string",
      "isCustom": "boolean",
      "knowledge": ["string"],
      "bestPractices": ["string"],
      "codeExamples": [
        {
          "language": "string",
          "description": "string",
          "code": "string"
        }
      ],
      "relatedPatterns": ["string"],
      "tags": ["string"],
      "metadata": {
        "createdAt": "ISO 8601 timestamp",
        "updatedAt": "ISO 8601 timestamp",
        "author": "string",
        "version": "string"
      }
    }
  ],
  "categories": [
    {
      "id": "string",
      "name": "string",
      "description": "string",
      "icon": "string (emoji)"
    }
  ]
}
```

### 4.2 Complete Example

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/skills-config-v1.json",
  "version": "1.0.0",
  "lastUpdated": "2026-05-02T17:30:00Z",
  "skills": [
    {
      "id": "java-enterprise-patterns",
      "name": "Java Enterprise Patterns",
      "category": "architecture",
      "description": "Enterprise Java design patterns and best practices",
      "isCustom": false,
      "knowledge": [
        "Dependency Injection and IoC containers",
        "Repository and Service layer patterns",
        "DTO and Entity mapping strategies",
        "Transaction management patterns",
        "Exception handling hierarchies",
        "Configuration management approaches"
      ],
      "bestPractices": [
        "Use constructor injection over field injection",
        "Keep services stateless",
        "Separate business logic from infrastructure concerns",
        "Use DTOs for API boundaries",
        "Implement proper exception handling with custom exceptions"
      ],
      "codeExamples": [
        {
          "language": "java",
          "description": "Service layer with dependency injection",
          "code": "@ApplicationScoped\npublic class OrderService {\n    \n    private final OrderRepository orderRepository;\n    private final PaymentService paymentService;\n    \n    @Inject\n    public OrderService(OrderRepository orderRepository, PaymentService paymentService) {\n        this.orderRepository = orderRepository;\n        this.paymentService = paymentService;\n    }\n    \n    @Transactional\n    public Order createOrder(OrderDTO orderDTO) {\n        // Business logic\n    }\n}"
        }
      ],
      "relatedPatterns": [
        "Hexagonal Architecture",
        "Clean Architecture",
        "Domain-Driven Design"
      ],
      "tags": ["java", "enterprise", "patterns", "architecture"],
      "metadata": {
        "createdAt": "2026-05-01T10:00:00Z",
        "updatedAt": "2026-05-02T15:00:00Z",
        "author": "onepiece-cli",
        "version": "1.0.0"
      }
    },
    {
      "id": "rest-api-design",
      "name": "REST API Design",
      "category": "architecture",
      "description": "RESTful API design principles and best practices",
      "isCustom": false,
      "knowledge": [
        "HTTP methods and status codes",
        "Resource naming conventions",
        "HATEOAS principles",
        "API versioning strategies",
        "Pagination and filtering",
        "Error response formats",
        "Content negotiation"
      ],
      "bestPractices": [
        "Use nouns for resource names, not verbs",
        "Use HTTP methods correctly (GET, POST, PUT, DELETE, PATCH)",
        "Return appropriate status codes",
        "Implement consistent error responses",
        "Version your API from the start",
        "Use pagination for large collections",
        "Document with OpenAPI/Swagger"
      ],
      "codeExamples": [
        {
          "language": "java",
          "description": "RESTful resource endpoint",
          "code": "@Path(\"/api/v1/orders\")\n@Produces(MediaType.APPLICATION_JSON)\n@Consumes(MediaType.APPLICATION_JSON)\npublic class OrderResource {\n    \n    @GET\n    public Response listOrders(\n        @QueryParam(\"page\") @DefaultValue(\"0\") int page,\n        @QueryParam(\"size\") @DefaultValue(\"20\") int size\n    ) {\n        // Implementation\n    }\n    \n    @POST\n    public Response createOrder(OrderDTO order) {\n        // Implementation\n        return Response.status(Status.CREATED)\n            .entity(createdOrder)\n            .build();\n    }\n}"
        }
      ],
      "relatedPatterns": [
        "Richardson Maturity Model",
        "API Gateway Pattern",
        "Backend for Frontend (BFF)"
      ],
      "tags": ["rest", "api", "http", "web-services"],
      "metadata": {
        "createdAt": "2026-05-01T10:00:00Z",
        "updatedAt": "2026-05-02T15:00:00Z",
        "author": "onepiece-cli",
        "version": "1.0.0"
      }
    },
    {
      "id": "payment-gateway-integration",
      "name": "Payment Gateway Integration",
      "category": "ecommerce",
      "description": "Integration patterns for payment gateways (Stripe, PayPal, etc.)",
      "isCustom": false,
      "knowledge": [
        "Payment flow patterns (redirect, embedded, API)",
        "Webhook handling and verification",
        "Idempotency in payment processing",
        "PCI DSS compliance basics",
        "Refund and chargeback handling",
        "Multi-currency support",
        "Payment method tokenization"
      ],
      "bestPractices": [
        "Never store raw credit card data",
        "Use payment gateway SDKs when available",
        "Implement idempotency keys for payment requests",
        "Verify webhook signatures",
        "Handle payment failures gracefully",
        "Log all payment transactions",
        "Implement retry logic with exponential backoff"
      ],
      "codeExamples": [
        {
          "language": "java",
          "description": "Stripe payment intent creation",
          "code": "@ApplicationScoped\npublic class StripePaymentService {\n    \n    @ConfigProperty(name = \"stripe.api.key\")\n    String stripeApiKey;\n    \n    public PaymentIntent createPaymentIntent(PaymentRequest request) {\n        Stripe.apiKey = stripeApiKey;\n        \n        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()\n            .setAmount(request.getAmount())\n            .setCurrency(request.getCurrency())\n            .setCustomer(request.getCustomerId())\n            .putMetadata(\"orderId\", request.getOrderId())\n            .setIdempotencyKey(request.getIdempotencyKey())\n            .build();\n        \n        return PaymentIntent.create(params);\n    }\n}"
        }
      ],
      "relatedPatterns": [
        "Saga Pattern for distributed transactions",
        "Event Sourcing for payment history",
        "Circuit Breaker for external API calls"
      ],
      "tags": ["payment", "ecommerce", "stripe", "paypal", "integration"],
      "metadata": {
        "createdAt": "2026-05-01T10:00:00Z",
        "updatedAt": "2026-05-02T15:00:00Z",
        "author": "onepiece-cli",
        "version": "1.0.0"
      }
    },
    {
      "id": "cryptocurrency-payments",
      "name": "Cryptocurrency Payment Processing",
      "category": "ecommerce",
      "description": "Cryptocurrency payment processing and wallet integration",
      "isCustom": true,
      "knowledge": [
        "Blockchain integration patterns using Web3.js and Ethers.js",
        "Wallet connection protocols (MetaMask, WalletConnect)",
        "Transaction signing and verification best practices",
        "Gas fee estimation and optimization",
        "Multi-chain support for Ethereum, Polygon, and BSC",
        "Smart contract interaction patterns",
        "Security considerations for crypto transactions"
      ],
      "bestPractices": [
        "Always verify transaction signatures",
        "Implement proper gas fee estimation",
        "Handle network congestion gracefully",
        "Support multiple wallet providers",
        "Implement transaction monitoring",
        "Use event listeners for transaction confirmations",
        "Store transaction hashes for audit trail"
      ],
      "codeExamples": [
        {
          "language": "javascript",
          "description": "Web3 payment processing",
          "code": "async function processPayment(amount, recipientAddress) {\n  const provider = new ethers.providers.Web3Provider(window.ethereum);\n  const signer = provider.getSigner();\n  \n  const tx = await signer.sendTransaction({\n    to: recipientAddress,\n    value: ethers.utils.parseEther(amount.toString())\n  });\n  \n  const receipt = await tx.wait();\n  return receipt.transactionHash;\n}"
        }
      ],
      "relatedPatterns": [
        "Event-driven architecture for blockchain events",
        "Retry pattern for failed transactions",
        "Observer pattern for transaction monitoring"
      ],
      "tags": ["cryptocurrency", "blockchain", "web3", "ethereum", "custom"],
      "metadata": {
        "createdAt": "2026-05-02T17:30:00Z",
        "updatedAt": "2026-05-02T17:30:00Z",
        "author": "user",
        "version": "1.0.0"
      }
    }
  ],
  "categories": [
    {
      "id": "architecture",
      "name": "Architecture & Design Patterns",
      "description": "Software architecture patterns and design principles",
      "icon": "🏗️"
    },
    {
      "id": "security",
      "name": "Security & Authentication",
      "description": "Security best practices and authentication patterns",
      "icon": "🔐"
    },
    {
      "id": "database",
      "name": "Data & Database Design",
      "description": "Database design patterns and data management",
      "icon": "📊"
    },
    {
      "id": "testing",
      "name": "Testing & Quality Assurance",
      "description": "Testing strategies and quality assurance practices",
      "icon": "🧪"
    },
    {
      "id": "devops",
      "name": "DevOps & Deployment",
      "description": "Deployment automation and DevOps practices",
      "icon": "🚀"
    },
    {
      "id": "ecommerce",
      "name": "E-commerce Specific",
      "description": "E-commerce domain-specific patterns and practices",
      "icon": "💰"
    }
  ]
}
```

---

## 5. .onepiece/project.json (Project Metadata)

### 5.1 Complete Schema

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/project-v1.json",
  "version": "1.0.0",
  "project": {
    "id": "string (UUID)",
    "name": "string",
    "description": "string",
    "createdAt": "ISO 8601 timestamp",
    "updatedAt": "ISO 8601 timestamp"
  },
  "agent": {
    "type": "bob | claudecode | opencode | pi",
    "version": "string",
    "configPath": "string"
  },
  "framework": {
    "name": "string",
    "version": "string",
    "language": "string"
  },
  "mcps": {
    "registryPath": "string",
    "enabled": ["string"],
    "lastSync": "ISO 8601 timestamp"
  },
  "skills": {
    "configPath": "string",
    "enabled": ["string"],
    "customCount": "number"
  },
  "deployment": {
    "target": "ibmcloud | flyio | aws | azure | gcp",
    "region": "string",
    "appName": "string",
    "lastDeployment": "ISO 8601 timestamp",
    "deploymentHistory": [
      {
        "timestamp": "ISO 8601 timestamp",
        "version": "string",
        "status": "success | failed",
        "duration": "number (seconds)"
      }
    ]
  },
  "statistics": {
    "setupCompletedAt": "ISO 8601 timestamp",
    "totalDeployments": "number",
    "lastActivity": "ISO 8601 timestamp"
  }
}
```

### 5.2 Complete Example

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/project-v1.json",
  "version": "1.0.0",
  "project": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "ecommerce-api",
    "description": "E-commerce REST API built with Quarkus",
    "createdAt": "2026-05-02T17:00:00Z",
    "updatedAt": "2026-05-02T17:30:00Z"
  },
  "agent": {
    "type": "bob",
    "version": "1.0.0",
    "configPath": ".bob.workspace"
  },
  "framework": {
    "name": "Quarkus",
    "version": "3.8.0",
    "language": "Java"
  },
  "mcps": {
    "registryPath": ".onepiece/mcp-registry.json",
    "enabled": ["filesystem", "github", "maven", "postgres", "redis"],
    "lastSync": "2026-05-02T17:25:00Z"
  },
  "skills": {
    "configPath": ".onepiece/skills-config.json",
    "enabled": [
      "java-enterprise-patterns",
      "rest-api-design",
      "database-schema-design",
      "security-best-practices",
      "payment-gateway-integration",
      "shopping-cart-logic",
      "order-processing-workflows"
    ],
    "customCount": 1
  },
  "deployment": {
    "target": "ibmcloud",
    "region": "us-south",
    "appName": "ecommerce-api-prod",
    "lastDeployment": "2026-05-02T16:00:00Z",
    "deploymentHistory": [
      {
        "timestamp": "2026-05-02T16:00:00Z",
        "version": "1.0.0",
        "status": "success",
        "duration": 180
      },
      {
        "timestamp": "2026-05-01T14:30:00Z",
        "version": "0.9.0",
        "status": "success",
        "duration": 165
      }
    ]
  },
  "statistics": {
    "setupCompletedAt": "2026-05-02T17:30:00Z",
    "totalDeployments": 2,
    "lastActivity": "2026-05-02T17:30:00Z"
  }
}
```

---

## 6. .onepiece/config.json (User Configuration)

### 6.1 Complete Schema

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/user-config-v1.json",
  "version": "1.0.0",
  "vault": {
    "url": "string",
    "tokenHash": "string (SHA-256)",
    "lastVerified": "ISO 8601 timestamp",
    "enabled": "boolean"
  },
  "preferences": {
    "defaultAgent": "bob | claudecode | opencode | pi",
    "defaultRegion": "string",
    "verbose": "boolean",
    "autoUpdate": "boolean",
    "telemetry": "boolean"
  },
  "ai": {
    "provider": "openai | watsonx | anthropic",
    "model": "string",
    "apiKeySource": "env | vault",
    "temperature": "number (0-1)",
    "maxTokens": "number"
  },
  "history": {
    "lastSetup": "ISO 8601 timestamp",
    "lastDeploy": "ISO 8601 timestamp",
    "recentProjects": ["string"]
  }
}
```

### 6.2 Complete Example

```json
{
  "$schema": "https://onepiece-cli.dev/schemas/user-config-v1.json",
  "version": "1.0.0",
  "vault": {
    "url": "https://vault.example.com",
    "tokenHash": "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
    "lastVerified": "2026-05-02T17:00:00Z",
    "enabled": true
  },
  "preferences": {
    "defaultAgent": "bob",
    "defaultRegion": "us-south",
    "verbose": false,
    "autoUpdate": true,
    "telemetry": true
  },
  "ai": {
    "provider": "openai",
    "model": "gpt-4o",
    "apiKeySource": "env",
    "temperature": 0.7,
    "maxTokens": 2000
  },
  "history": {
    "lastSetup": "2026-05-02T17:30:00Z",
    "lastDeploy": "2026-05-02T16:00:00Z",
    "recentProjects": [
      "/home/user/projects/ecommerce-api",
      "/home/user/projects/blog-platform",
      "/home/user/projects/inventory-system"
    ]
  }
}
```

---

## 7. .env.example (Environment Variables Template)

```bash
# One Piece CLI Environment Variables

# AI Provider Configuration
OPENAI_API_KEY=sk-...
# OR
WATSONX_API_KEY=...
WATSONX_PROJECT_ID=...

# HashiCorp Vault (if using BYOV)
VAULT_URL=https://vault.example.com
VAULT_TOKEN=hvs....

# GitHub Integration
GITHUB_TOKEN=ghp_...

# Database Configuration
DATABASE_URL=postgresql://user:password@localhost:5432/dbname
REDIS_URL=redis://localhost:6379

# IBM Cloud Deployment
IBM_CLOUD_API_KEY=...
IBM_CLOUD_REGION=us-south

# Maven Configuration (if needed)
MAVEN_HOME=/usr/local/maven

# Optional: Custom MCP Configurations
CUSTOM_MCP_PATH=/path/to/custom/mcps
```

---

## 8. Configuration File Management Service

### 8.1 Configuration Manager Implementation

```java
package com.nel.onepiece.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class ConfigurationManager {
    
    private final ObjectMapper objectMapper;
    
    public ConfigurationManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public void generateBobWorkspace(Path projectDir, BobWorkspaceConfig config) {
        Path configPath = projectDir.resolve(".bob.workspace");
        writeJson(configPath, config);
    }
    
    public void generateMcpRegistry(Path projectDir, McpRegistryConfig config) {
        Path onepieceDir = projectDir.resolve(".onepiece");
        Files.createDirectories(onepieceDir);
        
        Path registryPath = onepieceDir.resolve("mcp-registry.json");
        writeJson(registryPath, config);
    }
    
    public void generateSkillsConfig(Path projectDir, SkillsConfig config) {
        Path onepieceDir = projectDir.resolve(".onepiece");
        Files.createDirectories(onepieceDir);
        
        Path skillsPath = onepieceDir.resolve("skills-config.json");
        writeJson(skillsPath, config);
    }
    
    public void generateProjectMetadata(Path projectDir, ProjectMetadata metadata) {
        Path onepieceDir = projectDir.resolve(".onepiece");
        Files.createDirectories(onepieceDir);
        
        Path projectPath = onepieceDir.resolve("project.json");
        writeJson(projectPath, metadata);
    }
    
    public void generateEnvTemplate(Path projectDir, List<String> requiredEnvVars) {
        Path envPath = projectDir.resolve(".env.example");
        
        StringBuilder content = new StringBuilder();
        content.append("# One Piece CLI Environment Variables\n\n");
        
        for (String envVar : requiredEnvVars) {
            content.append(envVar).append("=\n");
        }
        
        Files.writeString(envPath, content.toString());
    }
    
    private void writeJson(Path path, Object config) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(path.toFile(), config);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to write configuration", e);
        }
    }
    
    public <T> T readConfig(Path path, Class<T> configClass) {
        try {
            return objectMapper.readValue(path.toFile(), configClass);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration", e);
        }
    }
}
```

---

## 9. Configuration Validation

### 9.1 JSON Schema Validation

```java
package com.nel.onepiece.validation;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConfigurationValidator {
    
    private final JsonSchemaFactory schemaFactory;
    
    public ConfigurationValidator() {
        this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }
    
    public ValidationResult validateBobWorkspace(String jsonContent) {
        JsonSchema schema = loadSchema("bob-workspace-v1.json");
        Set<ValidationMessage> errors = schema.validate(
            objectMapper.readTree(jsonContent)
        );
        
        return errors.isEmpty() 
            ? ValidationResult.success() 
            : ValidationResult.failure(errors);
    }
    
    public ValidationResult validateMcpRegistry(String jsonContent) {
        JsonSchema schema = loadSchema("mcp-registry-v1.json");
        Set<ValidationMessage> errors = schema.validate(
            objectMapper.readTree(jsonContent)
        );
        
        return errors.isEmpty() 
            ? ValidationResult.success() 
            : ValidationResult.failure(errors);
    }
    
    private JsonSchema loadSchema(String schemaName) {
        InputStream schemaStream = getClass()
            .getResourceAsStream("/schemas/" + schemaName);
        return schemaFactory.getSchema(schemaStream);
    }
}
```

---

## 10. Implementation Checklist

- [ ] Create JSON schema files for all configuration formats
- [ ] Implement `ConfigurationManager` service
- [ ] Build `ConfigurationValidator` with JSON schema validation
- [ ] Create configuration file templates
- [ ] Implement configuration migration for version updates
- [ ] Add configuration backup and restore functionality
- [ ] Build configuration diff and merge tools
- [ ] Write comprehensive validation tests
- [ ] Document configuration file formats
- [ ] Create configuration examples for common scenarios