package com.student.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.datapermission.DataPermissionRuleRegistry;
import com.student.common.entity.DataPermissionRuleEntity;
import com.student.common.exception.BusinessException;
import com.student.student.mapper.DataPermissionRuleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * 数据权限规则服务
 */
@Service
@Slf4j
public class DataPermissionRuleService {

    @Autowired
    private DataPermissionRuleMapper ruleMapper;

    @Autowired
    private DataPermissionRuleRegistry ruleRegistry;

    /**
     * 应用启动时加载规则到缓存
     */
    @PostConstruct
    public void init() {
        loadRulesToCache();
    }

    /**
     * 分页查询权限规则列表
     */
    public IPage<DataPermissionRuleEntity> list(Integer current, Integer size, String roleType, String tableName) {
        Page<DataPermissionRuleEntity> page = new Page<>(current, size);
        LambdaQueryWrapper<DataPermissionRuleEntity> wrapper = new LambdaQueryWrapper<>();

        if (roleType != null && !roleType.isEmpty()) {
            wrapper.eq(DataPermissionRuleEntity::getRoleType, roleType);
        }
        if (tableName != null && !tableName.isEmpty()) {
            wrapper.like(DataPermissionRuleEntity::getTableName, tableName);
        }

        wrapper.orderByDesc(DataPermissionRuleEntity::getCreateTime);
        return ruleMapper.selectPage(page, wrapper);
    }

    /**
     * 获取权限规则详情
     */
    public DataPermissionRuleEntity getById(Long id) {
        DataPermissionRuleEntity entity = ruleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("权限规则不存在");
        }
        return entity;
    }

    /**
     * 添加权限规则
     */
    @Transactional(rollbackFor = Exception.class)
    public DataPermissionRuleEntity add(DataPermissionRuleEntity entity) {
        // 验证必填字段
        validateRule(entity);

        // 检查是否已存在相同的规则
        LambdaQueryWrapper<DataPermissionRuleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataPermissionRuleEntity::getRoleType, entity.getRoleType())
                .eq(DataPermissionRuleEntity::getTableName, entity.getTableName())
                .eq(DataPermissionRuleEntity::getFilterField, entity.getFilterField());
        Long count = ruleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该规则已存在");
        }

        ruleMapper.insert(entity);
        log.info("添加数据权限规则成功: ", entity);

        // 刷新缓存
        loadRulesToCache();

        return entity;
    }

    /**
     * 更新权限规则
     */
    @Transactional(rollbackFor = Exception.class)
    public DataPermissionRuleEntity update(DataPermissionRuleEntity entity) {
        if (entity.getId() == null) {
            throw new BusinessException("规则ID不能为空");
        }

        DataPermissionRuleEntity existing = ruleMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException("权限规则不存在");
        }

        // 验证必填字段
        validateRule(entity);

        ruleMapper.updateById(entity);
        log.info("更新数据权限规则成功: {}", entity);

        // 刷新缓存
        loadRulesToCache();

        return entity;
    }

    /**
     * 删除权限规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DataPermissionRuleEntity entity = ruleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("权限规则不存在");
        }

        ruleMapper.deleteById(id);
        log.info("删除数据权限规则成功: {}", entity);

        // 刷新缓存
        loadRulesToCache();
    }

    /**
     * 启用/禁用权限规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggle(Long id) {
        DataPermissionRuleEntity entity = ruleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("权限规则不存在");
        }

        entity.setEnabled(entity.getEnabled() == 1 ? 0 : 1);
        ruleMapper.updateById(entity);
        log.info("切换数据权限规则状态成功: {}, enabled={}", entity, entity.getEnabled());

        // 刷新缓存
        loadRulesToCache();
    }

    /**
     * 刷新缓存
     */
    public void refreshCache() {
        loadRulesToCache();
        log.info("手动刷新数据权限规则缓存成功");
    }

    /**
     * 获取所有角色列表
     */
    public List<String> getRoles() {
        return Arrays.asList("admin", "teacher", "student");
    }

    /**
     * 获取所有表名列表
     */
    public List<String> getTables() {
        return Arrays.asList(
                "course_info",
                "course_enrollment",
                "course_notification",
                "course_attachment",
                "notification_receive",
                "notification_template",
                "file_info",
                "student",
                "class"
        );
    }

    /**
     * 加载规则到缓存
     */
    private void loadRulesToCache() {
        LambdaQueryWrapper<DataPermissionRuleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataPermissionRuleEntity::getEnabled, 1);
        List<DataPermissionRuleEntity> entities = ruleMapper.selectList(wrapper);
        ruleRegistry.loadRules(entities);
        log.info("加载{}条数据权限规则到缓存", entities.size());
    }

    /**
     * 验证规则
     */
    private void validateRule(DataPermissionRuleEntity entity) {
        if (entity.getRoleType() == null || entity.getRoleType().isEmpty()) {
            throw new BusinessException("角色类型不能为空");
        }
        if (entity.getTableName() == null || entity.getTableName().isEmpty()) {
            throw new BusinessException("表名不能为空");
        }
        if (entity.getFilterField() == null || entity.getFilterField().isEmpty()) {
            throw new BusinessException("过滤字段不能为空");
        }
        if (entity.getContextField() == null || entity.getContextField().isEmpty()) {
            throw new BusinessException("上下文字段不能为空");
        }
    }
}
