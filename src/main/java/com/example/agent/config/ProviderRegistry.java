package com.example.agent.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 提供商注册表
 * 管理所有支持的提供商类型和它们的元数据
 */
public class ProviderRegistry {

    private static final ProviderRegistry instance = new ProviderRegistry();
    private final Map<ProviderType, ProviderMetadata> metadataMap = new HashMap<>();

    private ProviderRegistry() {
        registerProviders();
    }

    public static ProviderRegistry getInstance() {
        return instance;
    }

    private void registerProviders() {
        registerProvider(ProviderType.OLLAMA, "本地开源模型",
                "Ollama 是一个本地运行开源大语言模型的平台，支持 Llama 2、Mistral、Qwen 等多种模型",
                true, false);

        registerProvider(ProviderType.OPENAI, "OpenAI 官方 API",
                "OpenAI 是领先的 AI 公司，提供 GPT-3.5、GPT-4 等强大模型，适用于各种复杂任务",
                false, true);

        registerProvider(ProviderType.ANTHROPIC, "Anthropic Claude",
                "Anthropic 提供的 Claude 系列模型，以其优秀的代码理解和推理能力著称",
                false, true);

        registerProvider(ProviderType.GOOGLE, "Google Gemini",
                "Google 的 Generative AI 模型，提供强大的多模态支持和搜索能力",
                false, true);

        registerProvider(ProviderType.DEEPSEEK, "DeepSeek 模型",
                "DeepSeek 专注于代码理解的模型，提供 DeepSeek-Coder 等专业模型",
                false, true);

        registerProvider(ProviderType.VOLCENGINE, "火山引擎方舟平台",
                "火山引擎的 AI 平台，提供豆包大模型，支持多种应用场景",
                false, true);

        registerProvider(ProviderType.CUSTOM, "自定义 API 接口",
                "支持自定义的 OpenAI 兼容接口，适用于各种第三方服务和本地部署",
                false, true);
    }

    private void registerProvider(ProviderType type, String shortDescription, String fullDescription,
                                  boolean isLocal, boolean isCloud) {
        metadataMap.put(type, new ProviderMetadata(type, shortDescription, fullDescription, isLocal, isCloud));
    }

    /**
     * 获取所有支持的提供商类型
     */
    public ProviderType[] getAllProviderTypes() {
        return ProviderType.values();
    }

    /**
     * 获取提供商元数据
     */
    public ProviderMetadata getMetadata(ProviderType type) {
        return metadataMap.get(type);
    }

    /**
     * 获取提供商元数据
     */
    public ProviderMetadata getMetadata(String typeName) {
        ProviderType type = ProviderType.fromName(typeName);
        return type != null ? metadataMap.get(type) : null;
    }

    /**
     * 获取本地运行的提供商
     */
    public ProviderType[] getLocalProviders() {
        return metadataMap.values().stream()
                .filter(ProviderMetadata::isLocal)
                .map(ProviderMetadata::getType)
                .toArray(ProviderType[]::new);
    }

    /**
     * 获取云端提供商
     */
    public ProviderType[] getCloudProviders() {
        return metadataMap.values().stream()
                .filter(ProviderMetadata::isCloud)
                .map(ProviderMetadata::getType)
                .toArray(ProviderType[]::new);
    }

    /**
     * 获取免费/开源的提供商
     */
    public ProviderType[] getFreeProviders() {
        return new ProviderType[]{ProviderType.OLLAMA};
    }

    /**
     * 检查类型是否有效
     */
    public boolean isValidType(String typeName) {
        return ProviderType.fromName(typeName) != null;
    }

    /**
     * 获取提供商的完整描述
     */
    public String getTypeDescription(ProviderType type) {
        ProviderMetadata metadata = metadataMap.get(type);
        return metadata != null ? metadata.getFullDescription() : type.getName();
    }

    /**
     * 检查是否支持模型切换
     */
    public boolean supportsModelSwitching(ProviderType type) {
        return true;
    }

    /**
     * 获取提供商的推荐使用场景
     */
    public String getUsageScenarios(ProviderType type) {
        return switch (type) {
            case OLLAMA -> "本地开发测试、隐私敏感场景";
            case OPENAI -> "生产环境、高质量任务、复杂问题";
            case ANTHROPIC -> "代码理解、长文本处理、推理任务";
            case GOOGLE -> "多模态应用、搜索增强、研究项目";
            case DEEPSEEK -> "代码生成、代码审查、编程任务";
            case VOLCENGINE -> "中文内容生成、企业应用";
            case CUSTOM -> "自定义部署、第三方服务、特殊需求";
        };
    }

    /**
     * 获取配置建议
     */
    public String getConfigurationTips(ProviderType type) {
        return switch (type) {
            case OLLAMA -> "确保本地已安装 Ollama 并运行服务";
            case OPENAI -> "需要有效的 OpenAI API Key";
            case ANTHROPIC -> "需要 Claude API Key，可在 anthropic.com 申请";
            case GOOGLE -> "需要 Google Cloud API Key，支持 Vertex AI";
            case DEEPSEEK -> "需要 DeepSeek API Key，可在 deepseek.com 申请";
            case VOLCENGINE -> "需要火山引擎 API Key，可在控制台获取";
            case CUSTOM -> "需要提供兼容 OpenAI 格式的 API 接口";
        };
    }

    /**
     * 提供商元数据类
     */
    public static class ProviderMetadata {
        private final ProviderType type;
        private final String shortDescription;
        private final String fullDescription;
        private final boolean isLocal;
        private final boolean isCloud;

        public ProviderMetadata(ProviderType type, String shortDescription, String fullDescription,
                                boolean isLocal, boolean isCloud) {
            this.type = type;
            this.shortDescription = shortDescription;
            this.fullDescription = fullDescription;
            this.isLocal = isLocal;
            this.isCloud = isCloud;
        }

        public ProviderType getType() {
            return type;
        }

        public String getShortDescription() {
            return shortDescription;
        }

        public String getFullDescription() {
            return fullDescription;
        }

        public boolean isLocal() {
            return isLocal;
        }

        public boolean isCloud() {
            return isCloud;
        }
    }
}
