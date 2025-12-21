package com.student.course.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文档内容提取服务
 * 使用Apache Tika提取各种格式文档的文本内容
 */
@Slf4j
@Service
public class DocumentContentExtractor {

    private final Tika tika = new Tika();

    /**
     * 从输入流中提取文本内容
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名（用于类型检测）
     * @return 提取的文本内容
     */
    public String extractContent(InputStream inputStream, String fileName) {
        try {
            // 使用Tika自动检测文件类型并提取内容
            String content = tika.parseToString(inputStream);

            // 限制内容长度（避免索引过大的文档）
            if (content.length() > 100000) {
                content = content.substring(0, 100000);
                log.warn("文档内容过长，已截取前100000个字符: {}", fileName);
            }

            log.info("成功提取文档内容: {}, 长度: {}", fileName, content.length());
            return content;

        } catch (IOException e) {
            log.error("读取文件失败: {}", fileName, e);
            return "";
        } catch (TikaException e) {
            log.error("解析文档失败: {}", fileName, e);
            return "";
        } catch (Exception e) {
            log.error("提取文档内容时发生未知错误: {}", fileName, e);
            return "";
        }
    }

    /**
     * 判断文件类型是否支持内容提取
     *
     * @param fileName 文件名
     * @return 是否支持
     */
    public boolean isSupportedFileType(String fileName) {
        if (fileName == null) {
            return false;
        }

        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".pdf") ||
               lowerName.endsWith(".doc") ||
               lowerName.endsWith(".docx") ||
               lowerName.endsWith(".ppt") ||
               lowerName.endsWith(".pptx") ||
               lowerName.endsWith(".xls") ||
               lowerName.endsWith(".xlsx") ||
               lowerName.endsWith(".txt");
    }
}
