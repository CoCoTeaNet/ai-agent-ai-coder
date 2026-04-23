package com.example.agent.skills;

/**
 * Skill（技能）接口
 * 定义 Agent 可以执行的高级技能
 */
public interface Skill {

    /**
     * 获取技能名称
     */
    String getName();

    /**
     * 获取技能描述
     */
    String getDescription();

    /**
     * 执行技能
     */
    String execute(String input);

    /**
     * 检查是否可以处理特定输入
     */
    boolean canHandle(String input);
}
