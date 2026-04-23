package com.example.agent.skills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 技能管理类
 * 负责管理 Agent 可用的所有技能
 */
public class SkillManager {

    private final List<Skill> skills;
    private final List<Skill> enabledSkills;

    public SkillManager() {
        this.skills = new ArrayList<>();
        this.enabledSkills = new ArrayList<>();
        initializeSkills();
    }

    /**
     * 初始化所有可用技能
     */
    private void initializeSkills() {
        // 添加内置技能
        addSkill(new CodeAnalysisSkill());
        addSkill(new TranslationSkill());
    }

    /**
     * 添加技能
     */
    public void addSkill(Skill skill) {
        skills.add(skill);
        enabledSkills.add(skill); // 默认启用所有技能
    }

    /**
     * 移除技能
     */
    public void removeSkill(String skillName) {
        skills.removeIf(skill -> skill.getName().equals(skillName));
        enabledSkills.removeIf(skill -> skill.getName().equals(skillName));
    }

    /**
     * 启用技能
     */
    public void enableSkill(String skillName) {
        Skill skill = findSkillByName(skillName);
        if (skill != null && !enabledSkills.contains(skill)) {
            enabledSkills.add(skill);
        }
    }

    /**
     * 禁用技能
     */
    public void disableSkill(String skillName) {
        enabledSkills.removeIf(skill -> skill.getName().equals(skillName));
    }

    /**
     * 查找技能
     */
    public Skill findSkillByName(String name) {
        return skills.stream()
                .filter(skill -> skill.getName().equals(name) ||
                        skill.getDescription().contains(name) ||
                        name.contains(skill.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 查找可处理特定输入的技能
     */
    public List<Skill> findSkillsForInput(String input) {
        List<Skill> matchingSkills = new ArrayList<>();
        for (Skill skill : enabledSkills) {
            if (skill.canHandle(input)) {
                matchingSkills.add(skill);
            }
        }
        return matchingSkills;
    }

    /**
     * 获取可处理特定输入的最佳技能
     */
    public Skill findBestSkill(String input) {
        List<Skill> candidates = findSkillsForInput(input);
        if (candidates.isEmpty()) {
            return null;
        }

        // 简单的评分机制
        int bestScore = 0;
        Skill bestSkill = null;

        for (Skill skill : candidates) {
            int score = calculateSkillScore(skill, input);
            if (score > bestScore) {
                bestScore = score;
                bestSkill = skill;
            }
        }

        return bestSkill;
    }

    /**
     * 计算技能评分
     */
    private int calculateSkillScore(Skill skill, String input) {
        int score = 0;

        // 基本匹配分数
        if (skill.getName().toLowerCase().contains(input.toLowerCase())) {
            score += 10;
        }

        if (skill.getDescription().toLowerCase().contains(input.toLowerCase())) {
            score += 5;
        }

        // 根据技能类型调整分数
        if (skill instanceof CodeAnalysisSkill) {
            if (input.toLowerCase().contains("代码")) {
                score += 8;
            }
            if (input.toLowerCase().contains("分析")) {
                score += 5;
            }
        }

        if (skill instanceof TranslationSkill) {
            if (input.toLowerCase().contains("翻译")) {
                score += 10;
            }
            if (input.toLowerCase().contains("英文")) {
                score += 5;
            }
            if (input.toLowerCase().contains("中文")) {
                score += 5;
            }
        }

        return score;
    }

    /**
     * 获取所有技能
     */
    public List<Skill> getAllSkills() {
        return Collections.unmodifiableList(skills);
    }

    /**
     * 获取启用的技能
     */
    public List<Skill> getEnabledSkills() {
        return Collections.unmodifiableList(enabledSkills);
    }

    /**
     * 检查技能是否已启用
     */
    public boolean isSkillEnabled(String skillName) {
        return enabledSkills.stream()
                .anyMatch(skill -> skill.getName().equals(skillName));
    }

    /**
     * 执行技能
     */
    public String executeSkill(String input) {
        Skill skill = findBestSkill(input);
        if (skill != null) {
            return skill.execute(input);
        }

        return "未找到可处理该输入的技能。";
    }

    /**
     * 获取技能信息
     */
    public List<String> getSkillDescriptions() {
        List<String> descriptions = new ArrayList<>();
        for (Skill skill : enabledSkills) {
            descriptions.add(String.format("%s - %s", skill.getName(), skill.getDescription()));
        }
        return descriptions;
    }

    /**
     * 执行指定技能
     */
    public String executeSpecificSkill(String skillName, String input) {
        Skill skill = findSkillByName(skillName);
        if (skill != null && enabledSkills.contains(skill)) {
            return skill.execute(input);
        }
        return String.format("技能 \"%s\" 未找到或未启用。", skillName);
    }

    /**
     * 获取技能数量
     */
    public int getSkillCount() {
        return skills.size();
    }

    /**
     * 清空所有技能
     */
    public void clearSkills() {
        skills.clear();
        enabledSkills.clear();
    }

    /**
     * 重新初始化技能
     */
    public void reinitialize() {
        clearSkills();
        initializeSkills();
    }
}
