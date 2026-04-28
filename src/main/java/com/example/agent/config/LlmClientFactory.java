package com.example.agent.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LLM 客户端工厂类
 * 根据配置创建不同的 LLM 客户端
 */
public class LlmClientFactory {

    private static final Logger log = LoggerFactory.getLogger(LlmClientFactory.class);
    private final AgentConfig config;

    public LlmClientFactory(AgentConfig config) {
        this.config = config;
    }

    public ChatLanguageModel createChatModel() {
        switch (config.getProvider()) {
            case OLLAMA:
                return createOllamaModel();
            case OPENAI:
                return createOpenAiModel();
            case OPENAI_COMPATIBLE:
                return createOpenAiCompatibleModel();
            default:
                throw new IllegalArgumentException("不支持的 LLM 提供商: " + config.getProvider());
        }
    }

    public StreamingChatLanguageModel createStreamingChatModel() {
        switch (config.getProvider()) {
            case OLLAMA:
                return createOllamaStreamingModel();
            case OPENAI:
                return createOpenAiStreamingModel();
            case OPENAI_COMPATIBLE:
                return createOpenAiCompatibleStreamingModel();
            default:
                throw new IllegalArgumentException("不支持的 LLM 提供商: " + config.getProvider());
        }
    }

    private ChatLanguageModel createOllamaModel() {
        log.info("初始化 Ollama 模型:");
        log.info("  - Base URL: {}", config.getBaseUrl());
        log.info("  - Model: {}", config.getModelName());

        return OllamaChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .build();
    }

    private StreamingChatLanguageModel createOllamaStreamingModel() {
        log.info("初始化 Ollama 流式模型:");
        log.info("  - Base URL: {}", config.getBaseUrl());
        log.info("  - Model: {}", config.getModelName());

        return OllamaStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .build();
    }

    private ChatLanguageModel createOpenAiModel() {
        log.info("初始化 OpenAI 模型:");
        log.info("  - Model: {}", config.getModelName());
        log.info("  - API Key exists: {}", config.getApiKey() != null && !config.getApiKey().isEmpty());

        return OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .topP(config.getTopP())
                .build();
    }

    private StreamingChatLanguageModel createOpenAiStreamingModel() {
        log.info("初始化 OpenAI 流式模型:");
        log.info("  - Model: {}", config.getModelName());
        log.info("  - API Key exists: {}", config.getApiKey() != null && !config.getApiKey().isEmpty());

        return OpenAiStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .topP(config.getTopP())
                .build();
    }

    private ChatLanguageModel createOpenAiCompatibleModel() {
        log.info("初始化 OpenAI 兼容模型:");
        log.info("  - Base URL: {}", config.getBaseUrl());
        log.info("  - Model: {}", config.getModelName());
        log.info("  - API Key exists: {}", config.getApiKey() != null && !config.getApiKey().isEmpty());

        String baseUrl = config.getBaseUrl();

        // 火山引擎特殊处理 - 确保 API URL 格式正确
        if (baseUrl.contains("volces.com")) {
            baseUrl = ensureCorrectVolcUrl(baseUrl);
            log.info("  - 使用火山引擎，调整后的 Base URL: {}", baseUrl);
        }

        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .topP(config.getTopP())
                .build();
    }

    private StreamingChatLanguageModel createOpenAiCompatibleStreamingModel() {
        log.info("初始化 OpenAI 兼容流式模型:");
        log.info("  - Base URL: {}", config.getBaseUrl());
        log.info("  - Model: {}", config.getModelName());
        log.info("  - API Key exists: {}", config.getApiKey() != null && !config.getApiKey().isEmpty());

        String baseUrl = config.getBaseUrl();

        // 火山引擎特殊处理 - 确保 API URL 格式正确
        if (baseUrl.contains("volces.com")) {
            baseUrl = ensureCorrectVolcUrl(baseUrl);
            log.info("  - 使用火山引擎，调整后的 Base URL: {}", baseUrl);
        }

        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .topP(config.getTopP())
                .build();
    }

    /**
     * 确保火山引擎 API URL 格式正确
     */
    private String ensureCorrectVolcUrl(String baseUrl) {
        // 火山引擎通常需要完整的 URL，包括 /v1
        if (!baseUrl.contains("/v1")) {
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "v1";
            } else {
                baseUrl = baseUrl + "/v1";
            }
        }
        return baseUrl;
    }
}
