package com.student.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.exception.BusinessException;
import com.student.course.entity.NotificationTemplate;
import com.student.course.mapper.NotificationTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知模板服务
 */
@Slf4j
@Service
public class NotificationTemplateService {

    @Autowired
    private NotificationTemplateMapper templateMapper;

    /**
     * 获取模板列表（分页）
     */
    public IPage<NotificationTemplate> getTemplateList(String templateType, Integer page, Integer size) {
        Page<NotificationTemplate> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();

        if (templateType != null && !templateType.isEmpty()) {
            wrapper.eq(NotificationTemplate::getTemplateType, templateType);
        }

        wrapper.eq(NotificationTemplate::getStatus, 1)
               .orderByDesc(NotificationTemplate::getCreateTime);

        return templateMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 获取所有启用的模板
     */
    public List<NotificationTemplate> getAllEnabledTemplates() {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getStatus, 1)
               .orderByDesc(NotificationTemplate::getCreateTime);
        return templateMapper.selectList(wrapper);
    }

    /**
     * 根据类型获取模板列表
     */
    public List<NotificationTemplate> getTemplatesByType(String templateType) {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getTemplateType, templateType)
               .eq(NotificationTemplate::getStatus, 1)
               .orderByDesc(NotificationTemplate::getCreateTime);
        return templateMapper.selectList(wrapper);
    }

    /**
     * 获取模板详情
     */
    public NotificationTemplate getTemplateById(Long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        return template;
    }

    /**
     * 创建模板
     */
    @Transactional(rollbackFor = Exception.class)
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        // 验证模板
        validateTemplate(template);

        template.setStatus(1);
        templateMapper.insert(template);

        log.info("创建通知模板成功: id={}, name={}", template.getId(), template.getTemplateName());
        return template;
    }

    /**
     * 更新模板
     */
    @Transactional(rollbackFor = Exception.class)
    public NotificationTemplate updateTemplate(NotificationTemplate template) {
        NotificationTemplate existing = templateMapper.selectById(template.getId());
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }

        // 验证模板
        validateTemplate(template);

        templateMapper.updateById(template);

        log.info("更新通知模板成功: id={}, name={}", template.getId(), template.getTemplateName());
        return template;
    }

    /**
     * 删除模板（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        template.setStatus(0);
        templateMapper.updateById(template);

        log.info("删除通知模板成功: id={}", id);
    }

    /**
     * 应用模板（替换变量）
     */
    public Map<String, String> applyTemplate(Long templateId, Map<String, String> variables) {
        NotificationTemplate template = getTemplateById(templateId);

        String title = replaceVariables(template.getTitleTemplate(), variables);
        String content = replaceVariables(template.getContentTemplate(), variables);

        Map<String, String> result = new HashMap<>();
        result.put("title", title);
        result.put("content", content);

        return result;
    }

    /**
     * 替换模板中的变量
     */
    private String replaceVariables(String template, Map<String, String> variables) {
        if (template == null || variables == null) {
            return template;
        }

        String result = template;
        Pattern pattern = Pattern.compile("\\{(\\w+)\\}");
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String varName = matcher.group(1);
            String varValue = variables.getOrDefault(varName, "{" + varName + "}");
            result = result.replace("{" + varName + "}", varValue);
        }

        return result;
    }

    /**
     * 验证模板
     */
    private void validateTemplate(NotificationTemplate template) {
        if (template.getTemplateName() == null || template.getTemplateName().trim().isEmpty()) {
            throw new BusinessException("模板名称不能为空");
        }

        if (template.getTemplateType() == null || template.getTemplateType().trim().isEmpty()) {
            throw new BusinessException("模板类型不能为空");
        }

        if (template.getTitleTemplate() == null || template.getTitleTemplate().trim().isEmpty()) {
            throw new BusinessException("标题模板不能为空");
        }

        if (template.getContentTemplate() == null || template.getContentTemplate().trim().isEmpty()) {
            throw new BusinessException("内容模板不能为空");
        }
    }

    /**
     * 插入默认模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertDefaultTemplates() {
        // 检查是否已有模板
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        Long count = templateMapper.selectCount(wrapper);
        if (count > 0) {
            return;
        }

        // 插入默认模板
        insertTemplate("作业通知模板", "homework",
            "{courseName} - 作业通知",
            "各位同学：\n\n{courseName}课程有新的作业需要完成。\n\n作业内容：{content}\n截止时间：{deadline}\n\n请按时完成并提交。\n\n{teacherName}\n{date}",
            "{\"courseName\":\"课程名称\",\"content\":\"作业内容\",\"deadline\":\"截止时间\",\"teacherName\":\"教师姓名\",\"date\":\"日期\"}");

        insertTemplate("考试通知模板", "exam",
            "{courseName} - 考试通知",
            "各位同学：\n\n{courseName}课程将于{examDate}进行考试。\n\n考试时间：{examTime}\n考试地点：{location}\n考试形式：{examType}\n\n请提前做好准备，准时参加考试。\n\n{teacherName}\n{date}",
            "{\"courseName\":\"课程名称\",\"examDate\":\"考试日期\",\"examTime\":\"考试时间\",\"location\":\"考试地点\",\"examType\":\"考试形式\",\"teacherName\":\"教师姓名\",\"date\":\"日期\"}");

        insertTemplate("课程公告模板", "announcement",
            "{courseName} - 课程公告",
            "各位同学：\n\n{content}\n\n{teacherName}\n{date}",
            "{\"courseName\":\"课程名称\",\"content\":\"公告内容\",\"teacherName\":\"教师姓名\",\"date\":\"日期\"}");

        insertTemplate("课程取消模板", "cancel",
            "{courseName} - 课程取消通知",
            "各位同学：\n\n由于{reason}，原定于{originalTime}的{courseName}课程取消。\n\n补课时间：{makeupTime}\n补课地点：{makeupLocation}\n\n请相互转告。\n\n{teacherName}\n{date}",
            "{\"courseName\":\"课程名称\",\"reason\":\"取消原因\",\"originalTime\":\"原定时间\",\"makeupTime\":\"补课时间\",\"makeupLocation\":\"补课地点\",\"teacherName\":\"教师姓名\",\"date\":\"日期\"}");

        log.info("默认通知模板插入成功");
    }

    private void insertTemplate(String name, String type, String titleTemplate, String contentTemplate, String variables) {
        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateName(name);
        template.setTemplateType(type);
        template.setTitleTemplate(titleTemplate);
        template.setContentTemplate(contentTemplate);
        template.setVariables(variables);
        template.setStatus(1);
        template.setCreateUserName("系统");
        templateMapper.insert(template);
    }
}
