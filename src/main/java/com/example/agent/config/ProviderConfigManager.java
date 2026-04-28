package com.example.agent.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 提供商配置管理器
 * 负责配置的 CRUD、持久化和管理
 */
public class ProviderConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ProviderConfigManager.class);
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".cogniagent";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "providers.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private static ProviderConfigManager instance;
    private final Map<String, ProviderConfig> configs = new ConcurrentHashMap<>();
    private volatile ProviderConfig defaultConfig;

    private ProviderConfigManager() {
        try {
            loadConfigs();
        } catch (Exception e) {
            log.error("Failed to initialize ProviderConfigManager: {}", e.getMessage());
            // 确保至少有一个默认配置
            createDefaultConfigs();
        }
    }

    /**
     * 获取单例实例
     */
    public static synchronized ProviderConfigManager getInstance() {
        if (instance == null) {
            instance = new ProviderConfigManager();
        }
        return instance;
    }

    /**
     * 加载配置文件
     */
    private void loadConfigs() {
        try {
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                ProviderConfig[] loadedConfigs = gson.fromJson(json, ProviderConfig[].class);
                for (ProviderConfig config : loadedConfigs) {
                    configs.put(config.getId(), config);
                    if (config.isDefault()) {
                        defaultConfig = config;
                    }
                }
                log.info("成功加载 {} 个提供商配置", configs.size());
            } else {
                log.info("配置文件不存在，将使用默认配置");
                createDefaultConfigs();
            }
        } catch (Exception e) {
            log.error("加载配置文件失败: {}", e.getMessage());
            createDefaultConfigs();
        }
    }

    /**
     * 创建默认配置
     */
    private void createDefaultConfigs() {
        configs.clear();
        ProviderConfig ollamaConfig = ProviderConfig.createDefault(ProviderType.OLLAMA);
        ollamaConfig.setId(UUID.randomUUID().toString());
        ollamaConfig.setDefault(true);
        configs.put(ollamaConfig.getId(), ollamaConfig);
        defaultConfig = ollamaConfig;
        log.info("已创建默认配置: {}", ProviderType.OLLAMA.getName());
        saveConfigs();
    }

    /**
     * 保存配置到文件
     */
    private void saveConfigs() {
        try {
            Path dirPath = Paths.get(CONFIG_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            String json = gson.toJson(configs.values());
            Files.writeString(Paths.get(CONFIG_FILE), json);
            log.debug("配置已保存到文件");
        } catch (Exception e) {
            log.error("保存配置文件失败: {}", e.getMessage());
        }
    }

    public List<ProviderConfig> getAllConfigs() {
        return new ArrayList<>(configs.values());
    }

    public List<ProviderConfig> getEnabledConfigs() {
        return configs.values().stream()
                .filter(ProviderConfig::isEnabled)
                .collect(Collectors.toList());
    }

    public ProviderConfig getConfig(String id) {
        return configs.get(id);
    }

    public List<ProviderConfig> getConfigsByType(ProviderType type) {
        return configs.values().stream()
                .filter(config -> config.getType() == type)
                .collect(Collectors.toList());
    }

    public ProviderConfig getDefaultConfig() {
        return defaultConfig != null ? defaultConfig : getEnabledConfigs().stream().findFirst().orElse(null);
    }

    public synchronized boolean setDefaultConfig(String id) {
        ProviderConfig config = configs.get(id);
        if (config != null && config.isEnabled()) {
            for (ProviderConfig c : configs.values()) {
                c.setDefault(false);
            }
            config.setDefault(true);
            defaultConfig = config;
            saveConfigs();
            log.info("默认配置已更新为: {}", config.getName());
            return true;
        }
        log.warn("无法设置默认配置: 配置不存在或已禁用");
        return false;
    }

    public synchronized ProviderConfig addConfig(ProviderConfig config) {
        if (config.getId() == null || config.getId().isEmpty()) {
            config.setId(UUID.randomUUID().toString());
        }
        if (config.getName() == null || config.getName().isEmpty()) {
            config.setName(config.getType().getName());
        }
        if (config.isDefault() || configs.isEmpty()) {
            config.setDefault(true);
        }
        config.setUpdatedAt(LocalDateTime.now());
        configs.put(config.getId(), config);
        saveConfigs();
        log.info("配置已添加: {}", config.getName());
        return config;
    }

    public synchronized ProviderConfig updateConfig(String id, ProviderConfig updatedConfig) {
        ProviderConfig existingConfig = configs.get(id);
        if (existingConfig != null) {
            existingConfig.setName(updatedConfig.getName());
            existingConfig.setType(updatedConfig.getType());
            existingConfig.setApiKey(updatedConfig.getApiKey());
            existingConfig.setBaseUrl(updatedConfig.getBaseUrl());
            existingConfig.setModelName(updatedConfig.getModelName());
            existingConfig.setEnabled(updatedConfig.isEnabled());
            existingConfig.setTemperature(updatedConfig.getTemperature());
            existingConfig.setMaxTokens(updatedConfig.getMaxTokens());
            existingConfig.setTopP(updatedConfig.getTopP());
            existingConfig.setUpdatedAt(LocalDateTime.now());
            if (updatedConfig.isDefault()) {
                for (ProviderConfig c : configs.values()) {
                    c.setDefault(false);
                }
                existingConfig.setDefault(true);
                defaultConfig = existingConfig;
            }
            saveConfigs();
            log.info("配置已更新: {}", existingConfig.getName());
            return existingConfig;
        }
        return null;
    }

    public synchronized boolean deleteConfig(String id) {
        if (id.equals(defaultConfig.getId())) {
            log.warn("无法删除默认配置");
            return false;
        }
        ProviderConfig removed = configs.remove(id);
        if (removed != null) {
            saveConfigs();
            log.info("配置已删除: {}", removed.getName());
            return true;
        }
        return false;
    }

    public boolean validateConfig(ProviderConfig config) {
        return config != null && config.isValid();
    }

    public boolean healthCheck(ProviderConfig config) {
        if (!validateConfig(config)) {
            return false;
        }
        try {
            if (config.getType() == ProviderType.OLLAMA) {
                try (java.net.Socket socket = new java.net.Socket()) {
                    String[] parts = config.getBaseUrl().replace("http://", "").replace("https://", "").split(":");
                    String host = parts[0];
                    int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 11434;
                    socket.connect(new java.net.InetSocketAddress(host, port), 5000);
                    return true;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("健康检查失败: {}", e.getMessage());
            return false;
        }
    }
}
