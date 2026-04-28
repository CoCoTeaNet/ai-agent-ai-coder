package com.example.agent.config;

/**
 * LLM 提供商类型枚举
 * 支持主流的 AI 模型提供商
 */
public enum ProviderType {

    OLLAMA("Ollama", "本地运行的开源模型", "http://localhost:11434", "llama2"),
    OPENAI("OpenAI", "OpenAI 官方 API", "https://api.openai.com", "gpt-3.5-turbo"),
    ANTHROPIC("Anthropic", "Claude 系列模型", "https://api.anthropic.com", "claude-3-sonnet-20250219"),
    GOOGLE("Google", "Google Gemini 系列模型", "https://generativelanguage.googleapis.com", "gemini-pro"),
    DEEPSEEK("DeepSeek", "DeepSeek 系列模型", "https://api.deepseek.com", "deepseek-chat"),
    VOLCENGINE("VolcEngine", "火山引擎方舟平台", "https://ark.cn-beijing.volces.com/api/coding", "doubao-pro-32k"),
    CUSTOM("Custom", "自定义 API 端点", "", "custom-model");

    private final String name;
    private final String description;
    private final String defaultBaseUrl;
    private final String defaultModelName;

    ProviderType(String name, String description, String defaultBaseUrl, String defaultModelName) {
        this.name = name;
        this.description = description;
        this.defaultBaseUrl = defaultBaseUrl;
        this.defaultModelName = defaultModelName;
    }

    /**
     * 获取提供商的显示名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取提供商的描述信息
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取默认的 API 基础 URL
     */
    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    /**
     * 获取默认的模型名称
     */
    public String getDefaultModelName() {
        return defaultModelName;
    }

    /**
     * 根据名称获取 ProviderType 枚举值
     */
    public static ProviderType fromName(String name) {
        if (name == null) {
            return null;
        }
        for (ProviderType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查是否需要 API Key
     */
    public boolean requiresApiKey() {
        return this != OLLAMA;
    }

    /**
     * 检查是否支持流式响应
     */
    public boolean supportsStreaming() {
        return true; // 所有主流提供商都支持流式响应
    }

    /**
     * 获取支持的模型列表（示例）
     */
    public String[] getSupportedModels() {
        return switch (this) {
            case OLLAMA -> new String[]{"llama2", "mistral", "qwen2", "codegemma"};
            case OPENAI -> new String[]{"gpt-3.5-turbo", "gpt-4-turbo", "gpt-4o"};
            case ANTHROPIC -> new String[]{"claude-3-opus-20250219", "claude-3-sonnet-20250219", "claude-3-haiku-20250219"};
            case GOOGLE -> new String[]{"gemini-pro", "gemini-ultra"};
            case DEEPSEEK -> new String[]{"deepseek-chat", "deepseek-coder"};
            case VOLCENGINE -> new String[]{"doubao-pro-32k", "doubao-lite"};
            case CUSTOM -> new String[]{"custom-model"};
        };
    }

    /**
     * 验证配置是否有效
     */
    public boolean isValidConfig(String baseUrl, String apiKey) {
        if (requiresApiKey() && (apiKey == null || apiKey.trim().isEmpty())) {
            return false;
        }
        if (!defaultBaseUrl.isEmpty() && baseUrl == null) {
            return false;
        }
        return true;
    }
}
