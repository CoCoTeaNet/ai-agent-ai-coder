package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间工具
 * 提供日期时间相关的功能
 */
public class DateTimeTool {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool("获取当前日期和时间")
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return "当前时间: " + now.format(DEFAULT_FORMATTER);
    }

    @Tool("获取当前日期")
    public String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        return "当前日期: " + now.toLocalDate().toString();
    }

    @Tool("获取当前时间")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return "当前时间: " + now.toLocalTime().withNano(0).toString();
    }

    @Tool("计算两个日期之间的天数差")
    public String calculateDaysBetween(
            @P("开始日期（格式：yyyy-MM-dd）") String startDate,
            @P("结束日期（格式：yyyy-MM-dd）") String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T00:00:00");
            long days = ChronoUnit.DAYS.between(start, end);
            return String.format("日期 %s 和 %s 之间相差 %d 天", startDate, endDate, Math.abs(days));
        } catch (DateTimeParseException e) {
            return "日期格式错误，请使用 yyyy-MM-dd 格式";
        }
    }

    @Tool("格式化日期时间")
    public String formatDateTime(
            @P("日期时间字符串（可选，默认当前时间）") String dateTime,
            @P("格式模式（可选，默认 yyyy-MM-dd HH:mm:ss）") String pattern) {
        try {
            DateTimeFormatter formatter = pattern != null ? DateTimeFormatter.ofPattern(pattern) : DEFAULT_FORMATTER;
            LocalDateTime time = dateTime != null ? LocalDateTime.parse(dateTime, DEFAULT_FORMATTER) : LocalDateTime.now();
            return "格式化结果: " + time.format(formatter);
        } catch (IllegalArgumentException e) {
            return "格式错误: " + e.getMessage();
        }
    }
}
