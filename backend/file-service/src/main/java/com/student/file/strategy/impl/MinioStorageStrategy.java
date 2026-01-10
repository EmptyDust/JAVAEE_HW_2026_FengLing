package com.student.file.strategy.impl;

import com.student.file.strategy.FileStorageStrategy;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Minio对象存储策略
 */
@Slf4j
@Component("minioStorageStrategy")
public class MinioStorageStrategy implements FileStorageStrategy {

    @Value("${file.minio.endpoint}")
    private String endpoint;

    @Value("${file.minio.access-key}")
    private String accessKey;

    @Value("${file.minio.secret-key}")
    private String secretKey;

    @Value("${file.minio.bucket-name}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // 检查bucket是否存在,不存在则创建
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("创建Minio bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("初始化Minio客户端失败", e);
        }
    }

    @Override
    public String upload(MultipartFile file, String storageName) throws Exception {
        // 按日期创建对象前缀
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = dateStr + "/" + storageName;

        // 上传文件
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        log.info("文件已上传到Minio: {}", objectName);
        return objectName;
    }

    @Override
    public InputStream download(String filePath) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .build()
        );
    }

    @Override
    public InputStream downloadRange(String filePath, long start, long end) throws Exception {
        long length = end - start + 1;
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .offset(start)
                        .length(length)
                        .build()
        );
    }

    @Override
    public void delete(String filePath) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .build()
        );
        log.info("文件已从Minio删除: {}", filePath);
    }

    @Override
    public String getAccessUrl(String filePath) {
        try {
            // 生成预签名URL(有效期7天)
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .expiry(7 * 24 * 60 * 60)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成Minio访问URL失败", e);
            return endpoint + "/" + bucketName + "/" + filePath;
        }
    }
}
