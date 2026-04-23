package com.example.agent.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 执行链路跟踪类
 * 记录Agent的思考和执行过程
 */
public class ExecutionTrace {

    private final List<TraceStep> steps;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;

    public ExecutionTrace() {
        this.steps = new ArrayList<>();
        this.startTime = LocalDateTime.now();
        this.completed = false;
    }

    /**
     * 添加思考步骤
     */
    public void addThought(String thought) {
        steps.add(new TraceStep(TraceStepType.THOUGHT, thought, LocalDateTime.now()));
    }

    /**
     * 添加工具调用步骤
     */
    public void addToolCall(String toolName, String parameters) {
        steps.add(new TraceStep(TraceStepType.TOOL_CALL,
                String.format("调用工具: %s, 参数: %s", toolName, parameters),
                LocalDateTime.now()));
    }

    /**
     * 添加工具执行结果步骤
     */
    public void addToolResult(String toolName, String result) {
        steps.add(new TraceStep(TraceStepType.TOOL_RESULT,
                String.format("工具 %s 返回结果: %s", toolName,
                        result.length() > 200 ? result.substring(0, 200) + "..." : result),
                LocalDateTime.now()));
    }

    /**
     * 添加观察步骤
     */
    public void addObservation(String observation) {
        steps.add(new TraceStep(TraceStepType.OBSERVATION, observation, LocalDateTime.now()));
    }

    /**
     * 添加最终答案步骤
     */
    public void addFinalAnswer(String answer) {
        steps.add(new TraceStep(TraceStepType.FINAL_ANSWER,
                answer.length() > 300 ? answer.substring(0, 300) + "..." : answer,
                LocalDateTime.now()));
    }

    /**
     * 标记完成
     */
    public void markComplete() {
        this.endTime = LocalDateTime.now();
        this.completed = true;
    }

    /**
     * 获取所有步骤
     */
    public List<TraceStep> getSteps() {
        return new ArrayList<>(steps);
    }

    /**
     * 获取执行时间（毫秒）
     */
    public long getExecutionTimeMs() {
        if (endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
    }

    /**
     * 格式化的步骤列表
     */
    public String formatSteps() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        for (int i = 0; i < steps.size(); i++) {
            TraceStep step = steps.get(i);
            sb.append(String.format("[%s] [%s] %s\n",
                    step.timestamp.format(formatter),
                    step.type.getIcon(),
                    step.content));
        }

        if (completed) {
            sb.append(String.format("\n✅ 完成, 耗时: %d ms\n", getExecutionTimeMs()));
        }

        return sb.toString();
    }

    /**
     * 转换为JSON格式（用于前端显示）
     */
    public List<TraceStepDTO> toDTOList() {
        List<TraceStepDTO> dtos = new ArrayList<>();
        for (TraceStep step : steps) {
            dtos.add(new TraceStepDTO(step.type.name(), step.type.getIcon(), step.content, step.timestamp.toString()));
        }
        return dtos;
    }

    /**
     * 跟踪步骤类型
     */
    public enum TraceStepType {
        THOUGHT("💭"),
        TOOL_CALL("🔧"),
        TOOL_RESULT("📊"),
        OBSERVATION("👀"),
        FINAL_ANSWER("✅");

        private final String icon;

        TraceStepType(String icon) {
            this.icon = icon;
        }

        public String getIcon() {
            return icon;
        }
    }

    /**
     * 单个跟踪步骤
     */
    public static class TraceStep {
        final TraceStepType type;
        final String content;
        final LocalDateTime timestamp;

        TraceStep(TraceStepType type, String content, LocalDateTime timestamp) {
            this.type = type;
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    /**
     * DTO 用于前端传输
     */
    public static class TraceStepDTO {
        public String type;
        public String icon;
        public String content;
        public String timestamp;

        public TraceStepDTO(String type, String icon, String content, String timestamp) {
            this.type = type;
            this.icon = icon;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}
