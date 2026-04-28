package com.example.agent.web.controller;

import com.example.agent.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/providers")
@CrossOrigin(origins = "*")
public class ProviderConfigController {

    private static final Logger log = LoggerFactory.getLogger(ProviderConfigController.class);
    private final ProviderConfigManager configManager = ProviderConfigManager.getInstance();
    private final ProviderRegistry providerRegistry = ProviderRegistry.getInstance();

    @GetMapping("")
    public Map<String, Object> getAllProviders() {
        List<ProviderConfig> configs = configManager.getAllConfigs();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", configs);
        result.put("count", configs.size());
        log.info("获取所有提供商配置，数量: {}", configs.size());
        return result;
    }

    @GetMapping("/enabled")
    public Map<String, Object> getEnabledProviders() {
        List<ProviderConfig> configs = configManager.getEnabledConfigs();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", configs);
        result.put("count", configs.size());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getProvider(@PathVariable String id) {
        ProviderConfig config = configManager.getConfig(id);
        Map<String, Object> result = new HashMap<>();
        if (config != null) {
            result.put("success", true);
            result.put("data", config);
        } else {
            result.put("success", false);
            result.put("message", "配置未找到");
        }
        return result;
    }

    @PostMapping("")
    public Map<String, Object> addProvider(@RequestBody ProviderConfig config) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!config.isValid()) {
                result.put("success", false);
                result.put("message", "配置验证失败，请检查必填字段");
                return result;
            }

            ProviderConfig added = configManager.addConfig(config);
            result.put("success", true);
            result.put("data", added);
            result.put("message", "配置添加成功");
            log.info("添加提供商配置: {}", config.getName());
        } catch (Exception e) {
            log.error("添加配置失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
        }
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateProvider(@PathVariable String id, @RequestBody ProviderConfig config) {
        Map<String, Object> result = new HashMap<>();
        try {
            config.setId(id);
            if (!config.isValid()) {
                result.put("success", false);
                result.put("message", "配置验证失败，请检查必填字段");
                return result;
            }

            ProviderConfig updated = configManager.updateConfig(id, config);
            if (updated != null) {
                result.put("success", true);
                result.put("data", updated);
                result.put("message", "配置更新成功");
                log.info("更新提供商配置: {}", config.getName());
            } else {
                result.put("success", false);
                result.put("message", "配置未找到");
            }
        } catch (Exception e) {
            log.error("更新配置失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteProvider(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (configManager.deleteConfig(id)) {
                result.put("success", true);
                result.put("message", "配置删除成功");
                log.info("删除提供商配置: {}", id);
            } else {
                result.put("success", false);
                result.put("message", "无法删除默认配置");
            }
        } catch (Exception e) {
            log.error("删除配置失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/{id}/default")
    public Map<String, Object> setDefaultProvider(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (configManager.setDefaultConfig(id)) {
                ProviderConfig defaultConfig = configManager.getDefaultConfig();
                result.put("success", true);
                result.put("data", defaultConfig);
                result.put("message", "默认配置已设置");
                log.info("设置默认提供商配置: {}", id);
            } else {
                result.put("success", false);
                result.put("message", "配置未找到");
            }
        } catch (Exception e) {
            log.error("设置默认配置失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "设置失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/default")
    public Map<String, Object> getDefaultProvider() {
        ProviderConfig defaultConfig = configManager.getDefaultConfig();
        Map<String, Object> result = new HashMap<>();
        if (defaultConfig != null) {
            result.put("success", true);
            result.put("data", defaultConfig);
        } else {
            result.put("success", false);
            result.put("message", "未找到默认配置");
        }
        return result;
    }

    @GetMapping("/types")
    public Map<String, Object> getProviderTypes() {
        ProviderType[] types = ProviderType.values();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", types);
        return result;
    }

    @GetMapping("/types/{typeName}/metadata")
    public Map<String, Object> getProviderMetadata(@PathVariable String typeName) {
        ProviderType type = ProviderType.fromName(typeName);
        Map<String, Object> result = new HashMap<>();
        if (type != null) {
            ProviderRegistry.ProviderMetadata metadata = providerRegistry.getMetadata(type);
            result.put("success", true);
            result.put("data", metadata);
            result.put("supportedModels", type.getSupportedModels());
        } else {
            result.put("success", false);
            result.put("message", "不支持的提供商类型");
        }
        return result;
    }

    @PostMapping("/{id}/test")
    public Map<String, Object> testProvider(@PathVariable String id) {
        ProviderConfig config = configManager.getConfig(id);
        Map<String, Object> result = new HashMap<>();
        if (config == null) {
            result.put("success", false);
            result.put("message", "配置未找到");
            return result;
        }

        boolean healthy = configManager.healthCheck(config);
        result.put("success", healthy);
        result.put("message", healthy ? "健康检查通过" : "健康检查失败，请检查配置");
        return result;
    }

    @GetMapping("/{id}/test")
    public Map<String, Object> testProviderGet(@PathVariable String id) {
        return testProvider(id);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<ProviderConfig> allConfigs = configManager.getAllConfigs();
        List<ProviderConfig> enabledConfigs = configManager.getEnabledConfigs();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allConfigs.size());
        stats.put("enabled", enabledConfigs.size());
        stats.put("disabled", allConfigs.size() - enabledConfigs.size());

        Map<String, Integer> byType = new HashMap<>();
        for (ProviderConfig config : allConfigs) {
            String type = config.getType().name();
            byType.put(type, byType.getOrDefault(type, 0) + 1);
        }
        stats.put("byType", byType);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", stats);
        return result;
    }

    @PostMapping("/test")
    public Map<String, Object> testProvider(@RequestBody ProviderConfig config) {
        Map<String, Object> result = new HashMap<>();
        if (config == null || !config.isValid()) {
            result.put("success", false);
            result.put("message", "配置验证失败");
            return result;
        }

        boolean healthy = configManager.healthCheck(config);
        result.put("success", healthy);
        result.put("message", healthy ? "配置有效" : "健康检查失败");
        return result;
    }
}
