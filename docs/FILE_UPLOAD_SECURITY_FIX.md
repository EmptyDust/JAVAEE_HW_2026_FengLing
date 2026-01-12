# 文件上传安全加固指南

## 📋 安全问题修复

本次修复解决了文件上传接口的三大安全风险：
1. **磁盘爆满风险**：未限制文件大小
2. **恶意脚本注入**：未校验文件类型
3. **路径遍历攻击**：未清理文件名

---

## 🔧 修改内容

### 1. 新增文件校验工具类

**文件**: `FileValidationUtil.java`

**核心功能**：
```java
// 1. 文件大小校验（限制100MB）
validateFileSize(file);

// 2. MimeType白名单校验
validateFileType(file);

// 3. 文件名安全清理
String safeName = sanitizeFileName(originalName);
```

**白名单类型**：
- 图片：JPEG, PNG, GIF, BMP, WebP, SVG
- 文档：PDF, Word, Excel, PowerPoint, TXT, CSV
- 压缩包：ZIP, RAR, 7Z

**黑名单类型**（即使MimeType通过也拒绝）：
- 可执行文件：.exe, .bat, .cmd, .sh
- 脚本文件：.js, .jsp, .php, .asp, .py

---

### 2. 修改 FileController

**文件**: `FileController.java`

**改进点**：
```java
@PostMapping("/upload")
public Result<FileInfo> upload(...) {
    // 1. 文件安全校验（在调用Service之前）
    FileValidationUtil.validateFile(file);

    // 2. 记录详细日志
    log.info("文件上传请求: filename={}, size={}, contentType={}",
            file.getOriginalFilename(),
            FileValidationUtil.formatFileSize(file.getSize()),
            file.getContentType());

    // 3. 调用服务层
    FileInfo fileInfo = fileService.upload(...);

    return Result.success(fileInfo);
}
```

---

### 3. 修改 FileService

**文件**: `FileService.java`

**改进点**：
```java
public FileInfo upload(...) {
    // 1. 清理原始文件名（防止XSS和路径遍历）
    String originalName = file.getOriginalFilename();
    String sanitizedName = FileValidationUtil.sanitizeFileName(originalName);

    // 2. 使用UUID生成存储文件名
    String storageName = UUID.randomUUID().toString() + extension;

    // 3. 保存清理后的文件名到数据库
    fileInfo.setOriginalName(sanitizedName);

    return fileInfo;
}
```

---

## 🛡️ 安全改进对比

### 修改前的安全风险

| 风险类型 | 问题描述 | 影响 |
|---------|---------|------|
| **磁盘爆满** | 未限制文件大小 | 攻击者上传超大文件耗尽磁盘空间 |
| **恶意脚本** | 未校验文件类型 | 上传.jsp/.php等脚本文件执行恶意代码 |
| **路径遍历** | 未清理文件名 | 文件名包含`../`可能覆盖系统文件 |
| **XSS攻击** | 文件名未转义 | 文件名包含`<script>`标签导致XSS |

### 修改后的安全保障

| 安全措施 | 实现方式 | 防御效果 |
|---------|---------|---------|
| **大小限制** | 100MB硬限制 | ✅ 防止磁盘爆满 |
| **类型白名单** | MimeType + 扩展名双重校验 | ✅ 防止恶意脚本上传 |
| **黑名单拦截** | 拒绝可执行文件和脚本 | ✅ 多层防护 |
| **文件名清理** | 移除非法字符和路径分隔符 | ✅ 防止路径遍历和XSS |
| **UUID重命名** | 存储文件名使用UUID | ✅ 防止文件名冲突和猜测 |

---

## 🚀 部署步骤

### 步骤1：重新编译项目

```bash
cd /home/emptydust/JAVAEE_HWF

# 清理并重新编译
mvn clean package -DskipTests

# 检查编译结果
ls -lh backend/file-service/target/file-service-1.0.0.jar
```

---

### 步骤2：重启文件服务

```bash
# 停止旧的文件服务
lsof -ti:8083 | xargs -r kill -9

# 启动新的文件服务
java -jar backend/file-service/target/file-service-1.0.0.jar &

# 查看启动日志
tail -f logs/file-service.log
```

---

## 🧪 测试验证

### 测试1：文件大小限制

**测试脚本**：
```bash
# 创建一个101MB的测试文件
dd if=/dev/zero of=large_file.bin bs=1M count=101

# 尝试上传（应该失败）
curl -X POST "http://localhost:8080/file/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@large_file.bin"
```

**预期结果**：
```json
{
  "code": 500,
  "message": "文件大小超过限制，最大允许 100.00 MB，当前文件 101.00 MB"
}
```

---

### 测试2：文件类型白名单

**测试脚本**：
```bash
# 创建一个恶意脚本文件
echo "<?php system(\$_GET['cmd']); ?>" > malicious.php

# 尝试上传（应该失败）
curl -X POST "http://localhost:8080/file/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@malicious.php"
```

**预期结果**：
```json
{
  "code": 500,
  "message": "不允许上传可执行文件或脚本文件"
}
```

---

### 测试3：路径遍历攻击防御

**测试脚本**：
```bash
# 创建一个包含路径遍历的文件名
echo "test content" > "../../etc/passwd"

# 尝试上传（文件名会被清理）
curl -X POST "http://localhost:8080/file/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@../../etc/passwd"
```

**预期结果**：
- 上传成功
- 数据库中的 `original_name` 字段为清理后的安全文件名（如 `___etc_passwd`）
- 存储文件名为 UUID（如 `a1b2c3d4-e5f6-7890-abcd-ef1234567890`）

---

### 测试4：正常文件上传

**测试脚本**：
```bash
# 上传一个正常的图片文件
curl -X POST "http://localhost:8080/file/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test_image.jpg"
```

**预期结果**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "originalName": "test_image.jpg",
    "storageName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
    "fileSize": 102400,
    "fileType": "image/jpeg",
    "accessUrl": "http://localhost:9000/student-system/..."
  }
}
```

---

## ⚠️ 注意事项

### 1. 文件大小限制配置

如果需要调整文件大小限制，修改 `FileValidationUtil.java`：

```java
// 修改最大文件大小（当前100MB）
private static final long MAX_FILE_SIZE = 100 * 1024 * 1024L;
```

同时需要配置 Spring Boot 的上传限制：

```yaml
# application.yml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

---

### 2. 白名单扩展

如果需要支持更多文件类型，修改 `FileValidationUtil.java`：

```java
// 添加新的MimeType
private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
    // 现有类型...
    "video/mp4",  // 添加视频支持
    "audio/mpeg"  // 添加音频支持
));

// 添加新的扩展名
private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
    // 现有扩展名...
    ".mp4", ".mp3"  // 添加新扩展名
));
```

---

### 3. 性能优化建议

**大文件上传优化**：
```yaml
# application.yml
spring:
  servlet:
    multipart:
      enabled: true
      file-size-threshold: 2MB  # 超过2MB使用临时文件
      location: /tmp            # 临时文件存储位置
```

**并发上传限制**：
```java
// 使用Semaphore限制并发上传数量
private static final Semaphore uploadSemaphore = new Semaphore(10);

public FileInfo upload(...) {
    uploadSemaphore.acquire();
    try {
        // 上传逻辑
    } finally {
        uploadSemaphore.release();
    }
}
```

---

## 🔍 故障排查

### 问题1：编译错误

**错误信息**：`cannot find symbol: class FileValidationUtil`

**解决方案**：
确保 `FileValidationUtil.java` 在正确的包路径下：
```
backend/file-service/src/main/java/com/student/file/util/FileValidationUtil.java
```

---

### 问题2：上传失败但没有明确错误

**排查步骤**：
```bash
# 1. 查看文件服务日志
tail -f logs/file-service.log

# 2. 检查文件大小配置
grep -r "max-file-size" backend/file-service/src/main/resources/

# 3. 测试文件校验逻辑
curl -X POST "http://localhost:8080/file/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.jpg" \
  -v
```

---

### 问题3：某些合法文件被拒绝

**原因**：MimeType不在白名单中

**解决方案**：
1. 查看日志中的 `contentType` 值
2. 将该 MimeType 添加到白名单
3. 重新编译并重启服务

---

## 📚 相关文档

- [OWASP 文件上传安全](https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload)
- [Spring Boot 文件上传配置](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.web)
- [MinIO 安全最佳实践](https://min.io/docs/minio/linux/operations/security.html)

---

## ✅ 安全加固完成检查清单

- [ ] 代码编译无错误
- [ ] 文件服务重启成功
- [ ] 测试1：文件大小限制 ✅
- [ ] 测试2：文件类型白名单 ✅
- [ ] 测试3：路径遍历防御 ✅
- [ ] 测试4：正常文件上传 ✅
- [ ] 配置 Spring Boot 上传限制
- [ ] 生产环境部署

---

**修复完成时间**: 2026-01-10
**修复人员**: Claude Code
**版本**: v1.1.0-file-upload-security-fix
