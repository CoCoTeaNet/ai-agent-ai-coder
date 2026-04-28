package com.example.agent.config;

/**
 * Agent 配置类
 * 管理 LLM 模型配置和 Agent 行为参数
 */
public class AgentConfig {

    /**
     * LLM 提供商类型
     */
    public enum LlmProvider {
        OLLAMA,
        OPENAI,
        OPENAI_COMPATIBLE
    }

    // 支持动态配置的字段
    private String providerConfigId;
    private LlmProvider provider = LlmProvider.OLLAMA;
    private String modelName = "llama2";
    private String baseUrl = "http://localhost:11434";
    private String apiKey = System.getenv("AI_API_KEY");
    private double temperature = 0.7;
    private int maxTokens = 2048;
    private double topP = 0.9;

    // Agent 配置
    private String systemPrompt = "你是一个有用的助手，可以回答问题并执行各种任务。";
    private boolean enableTools = true;
    private int maxIterations = 5;
    private boolean enableChainOfThought = true;
    private boolean enableReactMode = true;
    private boolean enableSelfReflection = true;
    private int maxConversationSummaryLength = 1000;

    public AgentConfig() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AgentConfig config = new AgentConfig();

        public Builder provider(LlmProvider provider) {
            config.provider = provider;
            return this;
        }

        public Builder modelName(String modelName) {
            config.modelName = modelName;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            config.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            config.apiKey = apiKey;
            return this;
        }

        // 从环境变量或 .env 文件获取 API 密钥，默认使用 AI_API_KEY
        public Builder apiKeyFromEnv() {
            return apiKeyFromEnv("AI_API_KEY");
        }

        // 指定环境变量名获取 API 密钥
        public Builder apiKeyFromEnv(String envKey) {
            String apiKey = System.getenv(envKey);
            
            // 如果系统环境变量中没有，尝试从项目根目录的 .env 文件中读取
            if (apiKey == null || apiKey.trim().isEmpty()) {
                try {
                    java.io.File envFile = new java.io.File(".env");
                    if (envFile.exists()) {
                        java.util.Properties props = new java.util.Properties();
                        try (java.io.FileInputStream fis = new java.io.FileInputStream(envFile)) {
                            props.load(fis);
                            apiKey = props.getProperty(envKey);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("读取 .env 文件失败: " + e.getMessage());
                }
            }

            if (apiKey != null && !apiKey.trim().isEmpty()) {
                config.apiKey = apiKey.trim();
            } else {
                System.err.println("警告: 未找到 " + envKey + " 环境变量或 .env 配置，使用占位符以防止启动报错。");
                config.apiKey = "dummy-key-to-prevent-startup-error";
            }
            return this;
        }

        public Builder temperature(double temperature) {
            config.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            config.maxTokens = maxTokens;
            return this;
        }

        public Builder topP(double topP) {
            config.topP = topP;
            return this;
        }

        public Builder systemPrompt(String systemPrompt) {
            config.systemPrompt = systemPrompt;
            return this;
        }

        public Builder enableTools(boolean enableTools) {
            config.enableTools = enableTools;
            return this;
        }

        public Builder maxIterations(int maxIterations) {
            config.maxIterations = maxIterations;
            return this;
        }

        public Builder enableChainOfThought(boolean enableChainOfThought) {
            config.enableChainOfThought = enableChainOfThought;
            return this;
        }

        public Builder enableReactMode(boolean enableReactMode) {
            config.enableReactMode = enableReactMode;
            return this;
        }

        public Builder enableSelfReflection(boolean enableSelfReflection) {
            config.enableSelfReflection = enableSelfReflection;
            return this;
        }

        public Builder maxConversationSummaryLength(int maxConversationSummaryLength) {
            config.maxConversationSummaryLength = maxConversationSummaryLength;
            return this;
        }

        public Builder fromProviderConfig(String providerConfigId) {
            ProviderConfig providerConfig = ProviderConfigManager.getInstance().getConfig(providerConfigId);
            if (providerConfig != null) {
                config.providerConfigId = providerConfigId;
                config.modelName = providerConfig.getModelName();
                config.baseUrl = providerConfig.getBaseUrl();
                config.apiKey = providerConfig.getApiKey();
                config.temperature = providerConfig.getTemperature();
                config.maxTokens = providerConfig.getMaxTokens();
                config.topP = providerConfig.getTopP();

                // 转换 ProviderType 到 LlmProvider
                switch (providerConfig.getType()) {
                    case OLLAMA:
                        config.provider = LlmProvider.OLLAMA;
                        break;
                    case OPENAI:
                        config.provider = LlmProvider.OPENAI;
                        break;
                    default:
                        config.provider = LlmProvider.OPENAI_COMPATIBLE;
                }
            }
            return this;
        }

        public Builder fromDefaultProviderConfig() {
            ProviderConfig defaultConfig = ProviderConfigManager.getInstance().getDefaultConfig();
            if (defaultConfig != null) {
                fromProviderConfig(defaultConfig.getId());
            }
            return this;
        }

        public AgentConfig build() {
            return config;
        }
    }

    public LlmProvider getProvider() {
        return provider;
    }

    public void setProvider(LlmProvider provider) {
        this.provider = provider;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTopP() {
        return topP;
    }

    public void setTopP(double topP) {
        this.topP = topP;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public boolean isEnableTools() {
        return enableTools;
    }

    public void setEnableTools(boolean enableTools) {
        this.enableTools = enableTools;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public boolean isEnableChainOfThought() {
        return enableChainOfThought;
    }

    public void setEnableChainOfThought(boolean enableChainOfThought) {
        this.enableChainOfThought = enableChainOfThought;
    }

    public boolean isEnableReactMode() {
        return enableReactMode;
    }

    public void setEnableReactMode(boolean enableReactMode) {
        this.enableReactMode = enableReactMode;
    }

    public boolean isEnableSelfReflection() {
        return enableSelfReflection;
    }

    public void setEnableSelfReflection(boolean enableSelfReflection) {
        this.enableSelfReflection = enableSelfReflection;
    }

    public int getMaxConversationSummaryLength() {
        return maxConversationSummaryLength;
    }

    public void setMaxConversationSummaryLength(int maxConversationSummaryLength) {
        this.maxConversationSummaryLength = maxConversationSummaryLength;
    }

    public String getProviderConfigId() {
        return providerConfigId;
    }

    public void setProviderConfigId(String providerConfigId) {
        this.providerConfigId = providerConfigId;
    }

    public void loadFromProviderConfig(ProviderConfig providerConfig) {
        if (providerConfig == null) {
            return;
        }
        this.providerConfigId = providerConfig.getId();
        this.modelName = providerConfig.getModelName();
        this.baseUrl = providerConfig.getBaseUrl();
        this.apiKey = providerConfig.getApiKey();
        this.temperature = providerConfig.getTemperature();
        this.maxTokens = providerConfig.getMaxTokens();
        this.topP = providerConfig.getTopP();

        // 转换 ProviderType 到 LlmProvider
        switch (providerConfig.getType()) {
            case OLLAMA:
                this.provider = LlmProvider.OLLAMA;
                break;
            case OPENAI:
                this.provider = LlmProvider.OPENAI;
                break;
            default:
                this.provider = LlmProvider.OPENAI_COMPATIBLE;
        }
    }

    public void loadFromDefaultProviderConfig() {
        ProviderConfig defaultConfig = ProviderConfigManager.getInstance().getDefaultConfig();
        if (defaultConfig != null) {
            loadFromProviderConfig(defaultConfig);
        }
    }
}
