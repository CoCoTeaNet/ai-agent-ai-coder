package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 网络请求工具
 * 提供 HTTP 请求功能
 */
public class NetworkTool {

    private static final Logger log = LoggerFactory.getLogger(NetworkTool.class);
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    private static final int DEFAULT_READ_TIMEOUT = 60000;

    /**
     * 发送 GET 请求
     * @param url URL 地址
     * @return 响应内容
     */
    @Tool("发送 GET 请求")
    public String get(@P("URL地址") String url) {
        return sendRequest(url, "GET", null, null, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送 POST 请求
     * @param url URL 地址
     * @param body 请求体
     * @param contentType 内容类型（如 application/json）
     * @return 响应内容
     */
    @Tool("发送 POST 请求")
    public String post(@P("URL地址") String url,
                       @P("请求体") String body,
                       @P("内容类型（如application/json）") String contentType) {
        return sendRequest(url, "POST", body, contentType, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送 PUT 请求
     * @param url URL 地址
     * @param body 请求体
     * @param contentType 内容类型
     * @return 响应内容
     */
    @Tool("发送 PUT 请求")
    public String put(@P("URL地址") String url,
                      @P("请求体") String body,
                      @P("内容类型") String contentType) {
        return sendRequest(url, "PUT", body, contentType, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送 DELETE 请求
     * @param url URL 地址
     * @return 响应内容
     */
    @Tool("发送 DELETE 请求")
    public String delete(@P("URL地址") String url) {
        return sendRequest(url, "DELETE", null, null, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送 PATCH 请求
     * @param url URL 地址
     * @param body 请求体
     * @param contentType 内容类型
     * @return 响应内容
     */
    @Tool("发送 PATCH 请求")
    public String patch(@P("URL地址") String url,
                        @P("请求体") String body,
                        @P("内容类型") String contentType) {
        return sendRequest(url, "PATCH", body, contentType, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 通用请求方法
     * @param url URL 地址
     * @param method HTTP 方法
     * @param body 请求体
     * @param contentType 内容类型
     * @param connectTimeout 连接超时（毫秒）
     * @param readTimeout 读取超时（毫秒）
     * @return 响应内容
     */
    @Tool("发送通用HTTP请求")
    public String sendRequest(@P("URL地址") String url,
                              @P("HTTP方法") String method,
                              @P("请求体（可选）") String body,
                              @P("内容类型（可选）") String contentType,
                              @P("连接超时（毫秒）") int connectTimeout,
                              @P("读取超时（毫秒）") int readTimeout) {
        HttpURLConnection connection = null;
        try {
            log.info("发送 {} 请求到: {}", method, url);

            URL requestUrl = URI.create(url).toURL();
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            // 设置请求头
            if (contentType != null && !contentType.isEmpty()) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", "LangChain4j-Agent/1.2.0");

            // 发送请求体
            if (body != null && !body.isEmpty() &&
                    !method.equals("GET") && !method.equals("DELETE")) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = connection.getResponseCode();
            log.info("响应码: {}", responseCode);

            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();

            StringBuilder result = new StringBuilder();
            result.append("状态码: ").append(responseCode).append("\n");
            result.append("响应头:\n");
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    result.append("  ").append(header.getKey()).append(": ")
                          .append(String.join(", ", header.getValue())).append("\n");
                }
            }
            result.append("\n响应体:\n").append(response.toString());

            return result.toString();
        } catch (Exception e) {
            log.error("HTTP请求失败: {} {}", method, url, e);
            return "错误：" + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
