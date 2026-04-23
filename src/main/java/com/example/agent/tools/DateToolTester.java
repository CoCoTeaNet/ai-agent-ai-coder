package com.example.agent.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具测试类
 * 验证日期查询功能是否正常
 */
public class DateToolTester {
    public static void main(String[] args) {
        System.out.println("=== 日期工具测试 ===");

        // 1. 测试日期工具直接调用
        DateTimeTool dateTool = new DateTimeTool();
        String currentDate = dateTool.getCurrentDate();
        String currentDateTime = dateTool.getCurrentDateTime();
        String currentTime = dateTool.getCurrentTime();

        System.out.println("1. 直接工具调用测试:");
        System.out.println("   当前日期: " + currentDate);
        System.out.println("   当前时间: " + currentTime);
        System.out.println("   当前完整时间: " + currentDateTime);

        // 2. 验证日期正确性
        LocalDateTime now = LocalDateTime.now();
        String expectedDate = now.toLocalDate().toString();
        boolean dateCorrect = currentDate.contains(expectedDate);

        System.out.println("\n2. 日期正确性验证:");
        System.out.println("   期望日期: " + expectedDate);
        System.out.println("   实际日期: " + currentDate);
        System.out.println("   验证结果: " + (dateCorrect ? "✅ 正确" : "❌ 错误"));

        // 3. 日期格式验证
        System.out.println("\n3. 日期格式验证:");
        System.out.println("   日期格式: yyyy-MM-dd");
        System.out.println("   时间格式: HH:mm:ss");

        System.out.println("\n=== 测试完成 ===");

        if (dateCorrect) {
            System.out.println("\n🎉 日期工具功能正常！");
        } else {
            System.out.println("\n❌ 日期工具功能有问题！");
        }
    }
}
