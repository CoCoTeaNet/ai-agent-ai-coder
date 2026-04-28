package com.example.agent.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器
 * 处理 Web 页面请求
 */
@Controller
public class WebController {

    /**
     * 首页 - 聊天界面
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * API 文档页面
     */
    @GetMapping("/api")
    public String apiDocs() {
        return "api";
    }

    /**
     * 配置页面
     */
    @GetMapping("/config")
    public String config() {
        return "config";
    }

    /**
     * 提供商配置页面
     */
    @GetMapping("/provider-config")
    public String providerConfig() {
        return "provider-config";
    }
}
