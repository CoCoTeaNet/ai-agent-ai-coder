package com.example.agent.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Agent Web 应用程序主入口
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.agent")
public class AgentWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentWebApplication.class, args);
        System.out.println("==========================================");
        System.out.println("       Agent Web 应用程序已启动");
        System.out.println("==========================================");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("API 文档: http://localhost:8080/api");
        System.out.println("==========================================");
    }
}
