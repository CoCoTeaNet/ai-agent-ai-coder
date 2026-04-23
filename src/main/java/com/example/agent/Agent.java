package com.example.agent;

import java.util.List;

/**
 * Agent 接口
 * 定义 Agent 的核心功能
 */
public interface Agent {

    /**
     * 与 Agent 进行交互
     *
     * @param message 用户消息
     * @return Agent 的响应
     */
    String interact(String message);

    /**
     * 执行特定任务
     *
     * @param task 任务描述
     * @return 任务执行结果
     */
    String executeTask(String task);

    /**
     * 重置 Agent 状态
     */
    void reset();

    /**
     * 获取 Agent 状态信息
     *
     * @return 状态信息
     */
    String getStatus();

    /**
     * 获取所有可用工具的名称
     *
     * @return 工具名称列表
     */
    List<String> getAvailableTools();

    /**
     * 获取所有可用技能
     *
     * @return 技能描述列表
     */
    List<String> getAvailableSkills();

    /**
     * 获取启用的技能数量
     *
     * @return 启用的技能数量
     */
    int getEnabledSkillCount();

    /**
     * 获取所有技能数量
     *
     * @return 所有技能数量
     */
    int getTotalSkillCount();
}
