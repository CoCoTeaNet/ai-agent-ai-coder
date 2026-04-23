package com.example.agent.skills;

/**
 * 基础技能实现类
 * 提供技能的通用功能
 */
public abstract class BaseSkill implements Skill {

    private final String name;
    private final String description;

    public BaseSkill(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public abstract boolean canHandle(String input);

    @Override
    public abstract String execute(String input);
}
