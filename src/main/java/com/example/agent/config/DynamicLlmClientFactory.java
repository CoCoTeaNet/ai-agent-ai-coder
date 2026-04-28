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
 * 动态 LLM 客户端工厂
 * 根据配置创建不同的 LLM 客户端实例
 */
public class DynamicLlmClientFactory {

    private static final Logger log = LoggerFactory.getLogger(DynamicLlmClientFactory.class);

    private final ProviderConfig config;

    public DynamicLlmClientFactory(ProviderConfig config) {
        this.config = config;
    }

    public ChatLanguageModel createChatModel() {
        if (config == null) {
            log.warn("配置为空，将使用默认 Ollama 模型");
            return createDefaultOllamaModel();
        }

        try {
            switch (config.getType()) {
                case OLLAMA:
                    return createOllamaModel();
                case OPENAI:
                case DEEPSEEK:
                case VOLCENGINE:
                case CUSTOM:
                    return createOpenAiCompatibleModel();
                case ANTHROPIC:
                case GOOGLE:
                default:
                    log.warn("提供商 {} 暂不支持，将使用默认 Ollama 模型", config.getType().getName());
                    return createDefaultOllamaModel();
            }
        } catch (Exception e) {
            log.error("创建 {} 模型客户端失败: {}, 将使用默认 Ollama 模型",
                    config.getType().getName(), e.getMessage());
            return createDefaultOllamaModel();
        }
    }

    public StreamingChatLanguageModel createStreamingChatModel() {
        if (config == null) {
            log.warn("配置为空，将使用默认 Ollama 流式模型");
            return createDefaultOllamaStreamingModel();
        }

        try {
            switch (config.getType()) {
                case OLLAMA:
                    return createOllamaStreamingModel();
                case OPENAI:
                case DEEPSEEK:
                case VOLCENGINE:
                case CUSTOM:
                    return createOpenAiCompatibleStreamingModel();
                case ANTHROPIC:
                case GOOGLE:
                default:
                    log.warn("提供商 {} 暂不支持，将使用默认 Ollama 流式模型", config.getType().getName());
                    return createDefaultOllamaStreamingModel();
            }
        } catch (Exception e) {
            log.error("创建 {} 流式模型客户端失败: {}, 将使用默认 Ollama 流式模型",
                    config.getType().getName(), e.getMessage());
            return createDefaultOllamaStreamingModel();
        }
    }

    private ChatLanguageModel createOllamaModel() {
        log.info("创建 Ollama 模型客户端:");
        log.info("  - 基础 URL: {}", config.getBaseUrl());
        log.info("  - 模型名称: {}", config.getModelName());
        log.info("  - 温度: {}", config.getTemperature());

        return OllamaChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .build();
    }

    private StreamingChatLanguageModel createOllamaStreamingModel() {
        log.info("创建 Ollama 流式模型客户端:");
        log.info("  - 基础 URL: {}", config.getBaseUrl());
        log.info("  - 模型名称: {}", config.getModelName());
        log.info("  - 温度: {}", config.getTemperature());

        return OllamaStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .build();
    }

    private ChatLanguageModel createOpenAiCompatibleModel() {
        log.info("创建 OpenAI 兼容模型客户端:");
        log.info("  - 基础 URL: {}", config.getBaseUrl());
        log.info("  - 模型名称: {}", config.getModelName());
        log.info("  - API Key: {}", config.getMaskedApiKey());
        log.info("  - 温度: {}", config.getTemperature());
        log.info("  - 最大 Token: {}", config.getMaxTokens());

        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .topP(config.getTopP())
                .build();
    }

    private StreamingChatLanguageModel createOpenAiCompatibleStreamingModel() {
        log.info("创建 OpenAI 兼容流式模型客户端:");
        log.info("  - 基础 URL: {}", config.getBaseUrl());
        log.info("  - 模型名称: {}", config.getModelName());
        log.info("  - API Key: {}", config.getMaskedApiKey());
        log.info("  - 温度: {}", config.getTemperature());
        log.info("  - 最大 Token: {}", config.getMaxTokens());

        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .topP(config.getTopP())
                .build();
    }

    private ChatLanguageModel createDefaultOllamaModel() {
        try {
            log.info("创建默认 Ollama 模型 (http://localhost:11434, model: llama2)");
            return OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("llama2")
                    .temperature(0.7)
                    .topP(0.9)
                    .build();
        } catch (Exception e) {
            log.error("创建默认 Ollama 模型失败: {}", e.getMessage());
            throw e;
        }
    }

    private StreamingChatLanguageModel createDefaultOllamaStreamingModel() {
        try {
            log.info("创建默认 Ollama 流式模型");
            return OllamaStreamingChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("llama2")
                    .temperature(0.7)
                    .topP(0.9)
                    .build();
        } catch (Exception e) {
            log.error("创建默认 Ollama 流式模型失败: {}", e.getMessage());
            // 对于流式模型，我们无法提供简单的回退，需要重新考虑
            throw e;
        }
    }

    /**
     * 创建回退模型
     */
    public static ChatLanguageModel createFallbackModel() {
        log.warn("使用回退模型: 默认 Ollama 模型");
        try {
            return new DynamicLlmClientFactory(null).createDefaultOllamaModel();
        } catch (Exception e) {
            log.error("初始化回退模型也失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 创建回退流式模型
     */
    public static StreamingChatLanguageModel createFallbackStreamingModel() {
        log.warn("使用回退流式模型: 默认 Ollama 流式模型");
        return new DynamicLlmClientFactory(null).createDefaultOllamaStreamingModel();
    }

    /**
     * 根据配置 ID 创建工厂
     */
    public static DynamicLlmClientFactory fromConfigId(String configId) {
        ProviderConfig config = ProviderConfigManager.getInstance().getConfig(configId);
        return new DynamicLlmClientFactory(config);
    }

    /**
     * 获取默认配置的工厂
     */
    public static DynamicLlmClientFactory getDefaultFactory() {
        ProviderConfig config = ProviderConfigManager.getInstance().getDefaultConfig();
        return new DynamicLlmClientFactory(config);
    }
}
