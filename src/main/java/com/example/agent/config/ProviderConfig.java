package com.example.agent.config;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 提供商配置数据模型
 */
public class ProviderConfig {

    private String id;
    private String name;
    private ProviderType type;
    private String apiKey;
    private String baseUrl;
    private String modelName;
    private boolean isDefault;
    private boolean enabled;
    private double temperature;
    private int maxTokens;
    private double topP;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProviderConfig() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.temperature = 0.7;
        this.maxTokens = 2048;
        this.topP = 0.9;
    }

    public ProviderConfig(String id, String name, ProviderType type, String apiKey, String baseUrl, String modelName) {
        this();
        this.id = id;
        this.name = name;
        this.type = type;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.modelName = modelName;
    }

    /**
     * 验证配置的有效性
     */
    public boolean isValid() {
        if (type == null) {
            return false;
        }
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (type.requiresApiKey() && (apiKey == null || apiKey.trim().isEmpty())) {
            return false;
        }
        if (!type.getDefaultBaseUrl().isEmpty() && (baseUrl == null || baseUrl.trim().isEmpty())) {
            return false;
        }
        if (modelName == null || modelName.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 掩码显示 API Key（用于安全显示）
     */
    public String getMaskedApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            return "";
        }
        if (apiKey.length() <= 8) {
            return apiKey;
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * 创建默认配置
     */
    public static ProviderConfig createDefault(ProviderType type) {
        ProviderConfig config = new ProviderConfig();
        config.setType(type);
        config.setName(type.getName());
        config.setBaseUrl(type.getDefaultBaseUrl());
        config.setModelName(type.getDefaultModelName());
        config.setTemperature(0.7);
        config.setMaxTokens(2048);
        config.setTopP(0.9);
        config.setEnabled(true);
        return config;
    }

    /**
     * 获取配置的详细描述
     */
    public String getDescription() {
        return String.format("%s (%s) - %s", name, type.getName(), modelName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProviderConfig that = (ProviderConfig) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProviderConfig{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", maskedApiKey='" + getMaskedApiKey() + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", modelName='" + modelName + '\'' +
                ", isDefault=" + isDefault +
                ", enabled=" + enabled +
                '}';
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public ProviderType getType() {
        return type;
    }

    public void setType(ProviderType type) {
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        this.updatedAt = LocalDateTime.now();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        this.updatedAt = LocalDateTime.now();
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        this.updatedAt = LocalDateTime.now();
    }

    public double getTopP() {
        return topP;
    }

    public void setTopP(double topP) {
        this.topP = topP;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
