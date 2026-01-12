# 选课系统并发问题修复指南

## 📋 修复概述

本次修复解决了选课系统的两个关键并发问题：
1. **重复选课问题**：多个请求同时选同一门课，导致重复选课记录
2. **超卖问题**：课程容量100人，并发选课导致实际选课人数超过100

## 🔧 修改内容

### 1. 数据库层面修改

**文件**: `scripts/fix_enrollment_concurrency.sql`

```sql
-- 添加唯一索引，防止重复选课
ALTER TABLE course_enrollment
ADD UNIQUE INDEX uk_course_student_status (course_id, student_id, status);
```

**作用**：
- 数据库层面保证同一学生不能重复选同一门课（相同状态）
- 并发插入时会触发 `DuplicateKeyException`

---

### 2. Mapper层修改

**文件**: `backend/course-service/src/main/java/com/student/course/mapper/CourseInfoMapper.java`

**新增方法**：
```java
// 原子性增加选课人数
int incrementEnrollmentAtomic(@Param("courseId") Long courseId);

// 原子性减少选课人数
int decrementEnrollmentAtomic(@Param("courseId") Long courseId);
```

**SQL逻辑**：
```sql
UPDATE course_info
SET enrolled_students = enrolled_students + 1,
    status = CASE WHEN enrolled_students + 1 >= max_students THEN 2 ELSE status END
WHERE id = ?
AND enrolled_students < max_students
AND status = 1
```

**关键点**：
- `enrolled_students = enrolled_students + 1`：原子操作，避免 Lost Update
- `WHERE enrolled_students < max_students`：防止超卖
- 返回影响行数：0表示失败（已满），1表示成功

---

### 3. Service层修改

#### CourseInfoService.java

**修改方法**: `incrementEnrollment()` 和 `decrementEnrollment()`

**核心改进**：
- 使用原子更新SQL替代"先查后改"
- 根据影响行数判断操作是否成功
- 失败时提供详细的错误信息

#### CourseEnrollmentService.java

**修改方法**: `enrollCourse()`

**核心改进**：
1. 移除了"先查后改"的重复选课检查
2. 直接插入选课记录，依赖数据库唯一索引
3. 捕获 `DuplicateKeyException` 转换为友好提示
4. 使用原子更新增加选课人数

**并发控制策略**：
```
请求1: 插入选课记录 → 成功 → 增加人数 → 成功 ✅
请求2: 插入选课记录 → DuplicateKeyException → 提示"已选过" ❌
```

---

## 🚀 部署步骤

### 步骤1：执行数据库脚本

```bash
cd /home/emptydust/JAVAEE_HWF

# 连接数据库
mysql -uroot -proot123456 student_system

# 执行索引创建脚本
source scripts/fix_enrollment_concurrency.sql;

# 验证索引是否创建成功
SHOW INDEX FROM course_enrollment WHERE Key_name = 'uk_course_student_status';
```

**预期输出**：
```
+-------------------+------------+---------------------------+
| Table             | Key_name   | Column_name               |
+-------------------+------------+---------------------------+
| course_enrollment | uk_course_student_status | course_id  |
| course_enrollment | uk_course_student_status | student_id |
| course_enrollment | uk_course_student_status | status     |
+-------------------+------------+---------------------------+
```

---

### 步骤2：重新编译项目

```bash
cd /home/emptydust/JAVAEE_HWF

# 清理并重新编译
mvn clean package -DskipTests

# 检查编译结果
ls -lh backend/course-service/target/course-service-1.0.0.jar
```

---

### 步骤3：重启课程服务

```bash
# 停止旧的课程服务
lsof -ti:8084 | xargs -r kill -9

# 启动新的课程服务
java -jar backend/course-service/target/course-service-1.0.0.jar &

# 查看启动日志
tail -f logs/course-service.log
```

---

## 🧪 测试验证

### 测试1：防止重复选课

**测试脚本**：
```bash
# 使用同一学生ID快速发送两次选课请求
STUDENT_ID=1
COURSE_ID=1
TOKEN="your_jwt_token"

# 第一次请求（应该成功）
curl -X POST "http://localhost:8080/course/enroll" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"courseId\": $COURSE_ID, \"studentId\": $STUDENT_ID}" &

# 第二次请求（应该失败，提示"您已选过该课程"）
curl -X POST "http://localhost:8080/course/enroll" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"courseId\": $COURSE_ID, \"studentId\": $STUDENT_ID}" &

wait
```

**预期结果**：
- 第一次请求：`{"code": 200, "message": "选课成功"}`
- 第二次请求：`{"code": 500, "message": "您已选过该课程"}`

---

### 测试2：防止超卖

**准备工作**：
```sql
-- 创建一个容量为2的测试课程
INSERT INTO course_info (course_name, course_code, max_students, enrolled_students, status)
VALUES ('并发测试课程', 'TEST001', 2, 0, 1);
```

**测试脚本**：
```bash
# 使用3个不同学生同时选课（容量只有2人）
COURSE_ID=测试课程ID
TOKEN="your_jwt_token"

for STUDENT_ID in 1 2 3; do
  curl -X POST "http://localhost:8080/course/enroll" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"courseId\": $COURSE_ID, \"studentId\": $STUDENT_ID}" &
done

wait
```

**预期结果**：
- 2个请求成功
- 1个请求失败，提示"该课程选课人数已满"
- 数据库中 `enrolled_students = 2`（不会超过 `max_students`）

---

### 测试3：高并发压力测试（可选）

使用 Apache Bench 或 JMeter 进行压力测试：

```bash
# 安装 Apache Bench
sudo apt install apache2-utils

# 100个并发请求，共1000次
ab -n 1000 -c 100 -p enroll.json -T application/json \
   -H "Authorization: Bearer $TOKEN" \
   http://localhost:8080/course/enroll
```

**验证点**：
- 选课记录数 = 成功的请求数
- `enrolled_students` 字段值正确
- 没有重复选课记录

---

## 📊 性能对比

### 修复前（先查后改）

```
并发场景：100个学生同时选同一门课（容量100人）
- 重复选课：可能出现
- 超卖问题：可能出现
- 数据一致性：❌ 不保证
```

### 修复后（原子操作 + 唯一索引）

```
并发场景：100个学生同时选同一门课（容量100人）
- 重复选课：✅ 数据库唯一索引保证不会发生
- 超卖问题：✅ 原子更新SQL保证不会超卖
- 数据一致性：✅ 完全保证
```

---

## ⚠️ 注意事项

### 1. 唯一索引的影响

**索引**: `uk_course_student_status (course_id, student_id, status)`

**含义**：
- 同一学生可以选同一门课多次，但 `status` 必须不同
- 例如：学生先选课（status=1），然后退课（status=0），可以再次选课（status=1）

**如果需要完全禁止重复选课**：
```sql
-- 修改索引为不包含 status
ALTER TABLE course_enrollment DROP INDEX uk_course_student_status;
ALTER TABLE course_enrollment ADD UNIQUE INDEX uk_course_student (course_id, student_id);
```

---

### 2. 事务隔离级别

当前使用 MySQL 默认的 `REPEATABLE READ` 隔离级别，已足够应对选课场景。

如果需要更严格的隔离：
```yaml
# application.yml
spring:
  datasource:
    hikari:
      transaction-isolation: TRANSACTION_SERIALIZABLE
```

---

### 3. 死锁风险

原子更新SQL已经最小化了锁的持有时间，但在极端高并发下仍可能出现死锁。

**监控死锁**：
```sql
-- 查看死锁日志
SHOW ENGINE INNODB STATUS;
```

**如果频繁死锁**：
- 考虑使用分布式锁（Redis）
- 调整事务隔离级别
- 优化索引顺序

---

## 🔍 故障排查

### 问题1：索引创建失败

**错误信息**：`Duplicate key name 'uk_course_student_status'`

**解决方案**：
```sql
-- 删除旧索引
ALTER TABLE course_enrollment DROP INDEX uk_course_student_status;

-- 重新创建
ALTER TABLE course_enrollment
ADD UNIQUE INDEX uk_course_student_status (course_id, student_id, status);
```

---

### 问题2：编译错误

**错误信息**：`cannot find symbol: class DuplicateKeyException`

**解决方案**：
确保 `CourseEnrollmentService.java` 导入了正确的包：
```java
import org.springframework.dao.DuplicateKeyException;
```

---

### 问题3：仍然出现超卖

**排查步骤**：
1. 检查索引是否创建成功
2. 检查 `incrementEnrollmentAtomic` 方法是否被调用
3. 查看数据库日志，确认SQL是否正确执行

```sql
-- 检查课程状态
SELECT id, course_name, enrolled_students, max_students, status
FROM course_info
WHERE id = ?;

-- 检查选课记录
SELECT COUNT(*) FROM course_enrollment
WHERE course_id = ? AND status = 1;
```

---

## 📚 相关文档

- [MySQL 唯一索引文档](https://dev.mysql.com/doc/refman/8.0/en/create-index.html)
- [Spring 事务管理](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
- [MyBatis-Plus 文档](https://baomidou.com/)

---

## ✅ 修复完成检查清单

- [ ] 数据库索引创建成功
- [ ] 代码编译无错误
- [ ] 课程服务重启成功
- [ ] 测试1：防止重复选课 ✅
- [ ] 测试2：防止超卖 ✅
- [ ] 测试3：高并发压力测试 ✅
- [ ] 生产环境部署

---

**修复完成时间**: 2026-01-10
**修复人员**: Claude Code
**版本**: v1.1.0-concurrency-fix
