package com.student.file.strategy;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件存储策略接口
 */
public interface FileStorageStrategy {

    /**
     * 上传文件
     * @param file 文件
     * @param storageName 存储文件名
     * @return 文件访问路径
     */
    String upload(MultipartFile file, String storageName) throws Exception;

    /**
     * 下载文件
     * @param filePath 文件路径
     * @return 文件输入流
     */
    InputStream download(String filePath) throws Exception;

    /**
     * 下载文件指定范围（支持Range请求）
     * @param filePath 文件路径
     * @param start 起始字节位置
     * @param end 结束字节位置
     * @return 文件输入流
     */
    InputStream downloadRange(String filePath, long start, long end) throws Exception;

    /**
     * 删除文件
     * @param filePath 文件路径
     */
    void delete(String filePath) throws Exception;

    /**
     * 获取文件访问URL
     * @param filePath 文件路径
     * @return 访问URL
     */
    String getAccessUrl(String filePath);
}
