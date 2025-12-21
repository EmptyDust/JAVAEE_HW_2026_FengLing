package com.student.file.strategy.impl;

import com.student.file.strategy.FileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 本地硬盘存储策略
 */
@Slf4j
@Component("localStorageStrategy")
public class LocalStorageStrategy implements FileStorageStrategy {

    @Value("${file.local.upload-path}")
    private String uploadPath;

    @Value("${file.local.access-url}")
    private String accessUrl;

    @Override
    public String upload(MultipartFile file, String storageName) throws Exception {
        // 按日期创建子目录
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dirPath = uploadPath + File.separator + dateStr;

        // 创建目录
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        String filePath = dateStr + File.separator + storageName;
        File destFile = new File(uploadPath + File.separator + filePath);
        file.transferTo(destFile);

        log.info("文件已保存到本地: {}", destFile.getAbsolutePath());
        return filePath;
    }

    @Override
    public InputStream download(String filePath) throws Exception {
        File file = new File(uploadPath + File.separator + filePath);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在: " + filePath);
        }
        return new FileInputStream(file);
    }

    @Override
    public void delete(String filePath) throws Exception {
        Path path = Paths.get(uploadPath + File.separator + filePath);
        Files.deleteIfExists(path);
        log.info("文件已删除: {}", filePath);
    }

    @Override
    public String getAccessUrl(String filePath) {
        return accessUrl + "/" + filePath.replace(File.separator, "/");
    }
}
