package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 稳定扩散文生图工具
 * 支持使用自然语言描述生成图像，集成稳定扩散API
 */
public class StableDiffusionTool {

    private static final Logger log = LoggerFactory.getLogger(StableDiffusionTool.class);

    // 默认API配置
    private String apiUrl = "http://127.0.0.1:7860/sdapi/v1/txt2img";
    private String apiKey = null;

    public StableDiffusionTool() {
        // 从环境变量或配置中读取API配置
        String envUrl = System.getenv("STABLE_DIFFUSION_API_URL");
        if (envUrl != null && !envUrl.isEmpty()) {
            this.apiUrl = envUrl;
        }

        String envKey = System.getenv("STABLE_DIFFUSION_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            this.apiKey = envKey;
        }
    }

    public StableDiffusionTool(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    /**
     * 文生图 - 根据文本描述生成图像
     *
     * @param prompt 图像描述文本
     * @return 生成的图像Base64编码字符串
     */
    @Tool("文生图 - 根据文本描述生成图像")
    public String generateImage(@P("图像描述文本") String prompt) {
        return generateImage(prompt, null, 512, 512, 20, 7.5, 1);
    }

    /**
     * 文生图 - 高级选项
     *
     * @param prompt       图像描述文本
     * @param negativePrompt 负面描述文本（不想要的内容）
     * @param width        图像宽度
     * @param height       图像高度
     * @param steps        生成步数
     * @param cfgScale     引导尺度
     * @param batchSize    批次大小
     * @return 生成的图像Base64编码字符串
     */
    @Tool("文生图 - 高级选项")
    public String generateImage(
            @P("图像描述文本") String prompt,
            @P("负面描述文本（不想要的内容，可选）") String negativePrompt,
            @P("图像宽度（可选，默认512）") int width,
            @P("图像高度（可选，默认512）") int height,
            @P("生成步数（可选，默认20）") int steps,
            @P("引导尺度（可选，默认7.5）") double cfgScale,
            @P("批次大小（可选，默认1）") int batchSize) {

        try {
            // 确保参数合理
            width = Math.max(256, Math.min(2048, width));
            height = Math.max(256, Math.min(2048, height));
            steps = Math.max(10, Math.min(100, steps));
            cfgScale = Math.max(1.0, Math.min(30.0, cfgScale));
            batchSize = Math.max(1, Math.min(4, batchSize));

            // 构建请求参数
            String requestBody = buildRequestBody(prompt, negativePrompt, width, height, steps, cfgScale, batchSize);

            // 发送API请求
            String response = sendApiRequest(requestBody);

            // 解析响应，提取图像数据
            String imageBase64 = parseResponse(response);
            return imageBase64;

        } catch (Exception e) {
            log.error("图像生成失败", e);
            return "图像生成失败: " + e.getMessage();
        }
    }

    /**
     * 构建API请求参数
     */
    private String buildRequestBody(String prompt, String negativePrompt, int width, int height, int steps, double cfgScale, int batchSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"prompt\": \"").append(escapeJson(prompt)).append("\",");
        sb.append("\"negative_prompt\": \"").append(escapeJson(negativePrompt != null ? negativePrompt : "")).append("\",");
        sb.append("\"width\": ").append(width).append(",");
        sb.append("\"height\": ").append(height).append(",");
        sb.append("\"steps\": ").append(steps).append(",");
        sb.append("\"cfg_scale\": ").append(cfgScale).append(",");
        sb.append("\"batch_size\": ").append(batchSize).append(",");
        sb.append("\"sampler_index\": \"DPM++ 2M Karras\"");
        sb.append("}");
        return sb.toString();
    }

    /**
     * 发送API请求
     */
    private String sendApiRequest(String requestBody) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(300000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            if (apiKey != null && !apiKey.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            }

            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("API请求失败，状态码: " + responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            return response.toString();
        } catch (Exception e) {
            log.error("API请求失败", e);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 解析API响应，提取图像数据
     */
    private String parseResponse(String response) {
        try {
            // 解析Stable Diffusion API响应
            // 响应格式示例：{"images": ["base64_encoded_image"], "parameters": {...}, "info": "..."}
            int imagesStart = response.indexOf("\"images\": [") + "\"images\": [".length();
            int imagesEnd = response.indexOf("]", imagesStart);
            String imagesStr = response.substring(imagesStart, imagesEnd);

            int base64Start = imagesStr.indexOf("\"") + 1;
            int base64End = imagesStr.lastIndexOf("\"");
            String base64 = imagesStr.substring(base64Start, base64End);

            return "data:image/png;base64," + base64;

        } catch (Exception e) {
            log.error("解析响应失败", e);
            throw new RuntimeException("解析响应失败: " + e.getMessage());
        }
    }

    /**
     * 转义JSON字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 检查API连接是否正常
     */
    @Tool("检查稳定扩散API连接")
    public String checkApiConnection() {
        try {
            String testPrompt = "a simple test image";
            String testResponse = generateImage(testPrompt, "", 256, 256, 10, 5, 1);

            if (testResponse.startsWith("data:image")) {
                return "API连接正常";
            } else {
                return "API响应异常: " + testResponse;
            }

        } catch (Exception e) {
            log.error("API连接检查失败", e);
            return "API连接失败: " + e.getMessage();
        }
    }

    /**
     * 获取API配置信息
     */
    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
