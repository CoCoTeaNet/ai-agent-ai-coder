package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索工具
 * 提供网络搜索功能
 */
public class SearchTool {

    @Tool("搜索网络信息")
    public String search(@P("搜索关键词") String query) {
        try {
            // 模拟搜索结果
            List<String> results = performSearch(query);
            return formatSearchResults(results);
        } catch (Exception e) {
            return "搜索失败: " + e.getMessage();
        }
    }

    @Tool("查找特定主题的详细信息")
    public String findInformation(
            @P("主题关键词") String topic,
            @P("信息类型（如 '定义', '用法', '示例' 等，可选）") String infoType) {
        try {
            String searchQuery = topic + (infoType != null ? " " + infoType : "");
            List<String> results = performSearch(searchQuery);
            return formatSearchResults(results);
        } catch (Exception e) {
            return "搜索失败: " + e.getMessage();
        }
    }

    private List<String> performSearch(String query) {
        List<String> results = new ArrayList<>();

        // 模拟搜索结果
        results.add("搜索结果 1 - " + query);
        results.add("这是关于 " + query + " 的详细信息...");
        results.add("搜索结果 2 - " + query);
        results.add("您可能感兴趣的相关内容: " + query);
        results.add("搜索结果 3 - 相关主题");
        results.add("更多关于 " + query + " 的资源和文档...");

        return results;
    }

    private String formatSearchResults(List<String> results) {
        if (results == null || results.isEmpty()) {
            return "未找到搜索结果";
        }

        StringBuilder sb = new StringBuilder("搜索结果:\n\n");
        for (int i = 0; i < results.size(); i++) {
            sb.append(i + 1).append(". ").append(results.get(i)).append("\n");
        }
        return sb.toString();
    }
}
