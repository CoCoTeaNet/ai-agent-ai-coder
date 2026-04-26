package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 导出数据为Excel文件的工具
 */
public class ExcelTool {
    private static final Logger log = LoggerFactory.getLogger(ExcelTool.class);
    private final Gson gson = new Gson();

    @Tool("将JSON数组数据导出为Excel文件，并返回可以下载该文件的链接。jsonArrayData必须是包含对象的JSON数组字符串。")
    public String exportToExcel(
            @P("JSON数组格式的数据，例如: [{\"标题\":\"测试\",\"点赞\":100}]") String jsonArrayData,
            @P("导出的文件名，不需要带后缀，例如: '顺德美食数据'") String fileName) {
        try {
            List<Map<String, Object>> dataList = gson.fromJson(jsonArrayData, new TypeToken<List<Map<String, Object>>>(){}.getType());
            if (dataList == null || dataList.isEmpty()) {
                return "错误：数据为空或JSON格式不正确";
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("数据表");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            Map<String, Object> firstItem = dataList.get(0);
            int colNum = 0;
            String[] headers = new String[firstItem.size()];
            for (String key : firstItem.keySet()) {
                Cell cell = headerRow.createCell(colNum);
                cell.setCellValue(key);
                headers[colNum] = key;
                colNum++;
            }

            // 填充数据
            int rowNum = 1;
            for (Map<String, Object> item : dataList) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = row.createCell(i);
                    Object val = item.get(headers[i]);
                    if (val != null) {
                        if (val instanceof Number) {
                            cell.setCellValue(((Number) val).doubleValue());
                        } else {
                            cell.setCellValue(val.toString());
                        }
                    }
                }
            }

            // 保存到系统的临时上传目录中，以便 ApiController 可以提供下载
            String uploadPath = System.getProperty("java.io.tmpdir") + File.separator + "agent_uploads";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String timestamp = String.valueOf(System.currentTimeMillis());
            String finalFileName = fileName + ".xlsx";
            String fileId = "file_" + timestamp + "_" + finalFileName;
            
            File outputFile = Paths.get(uploadPath, fileId).toFile();
            
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                workbook.write(fileOut);
            }
            workbook.close();

            String downloadUrl = "/api/v1/files/" + fileId;
            return "Excel文件生成成功！\n请将以下下载链接提供给用户：[点击下载 " + finalFileName + "](" + downloadUrl + ")";
            
        } catch (Exception e) {
            log.error("生成Excel文件失败", e);
            return "生成Excel失败: " + e.getMessage();
        }
    }
}
