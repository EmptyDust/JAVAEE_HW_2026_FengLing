package com.student.common.datapermission;

import com.student.common.entity.DataPermissionRuleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限规则注册表
 * 负责管理和缓存数据权限规则
 */
@Component
@Slf4j
public class DataPermissionRuleRegistry {

    /**
     * 规则缓存：key = "roleType:tableName", value = DataPermissionRule
     */
    private final Map<String, DataPermissionRule> ruleCache = new ConcurrentHashMap<>();

    /**
     * 加载规则到缓存
     * 由具体的服务模块调用，传入从数据库查询的规则列表
     *
     * @param entities 规则实体列表
     */
    public void loadRules(List<DataPermissionRuleEntity> entities) {
        ruleCache.clear();

        for (DataPermissionRuleEntity entity : entities) {
            String key = buildKey(entity.getRoleType(), entity.getTableName());
            DataPermissionRule rule = convertToRule(entity);
            ruleCache.put(key, rule);
        }

        log.info("Loaded {} data permission rules into cache", entities.size());
    }

    /**
     * 刷新缓存
     * 清空现有缓存，需要重新调用loadRules加载
     */
    public void refreshCache() {
        ruleCache.clear();
        log.info("Data permission rule cache cleared");
    }

    /**
     * 获取规则
     *
     * @param roleType  角色类型
     * @param tableName 表名
     * @return 数据权限规则，如果不存在返回null
     */
    public DataPermissionRule getRule(String roleType, String tableName) {
        String key = buildKey(roleType, tableName);
        return ruleCache.get(key);
    }

    /**
     * 获取所有规则
     *
     * @return 所有规则的Map
     */
    public Map<String, DataPermissionRule> getAllRules() {
        return new HashMap<>(ruleCache);
    }

    /**
     * 构建缓存key
     */
    private String buildKey(String roleType, String tableName) {
        return roleType + ":" + tableName;
    }

    /**
     * 将实体转换为规则对象
     */
    private DataPermissionRule convertToRule(DataPermissionRuleEntity entity) {
        DataPermissionRule.FieldRule fieldRule = DataPermissionRule.FieldRule.builder()
                .fieldName(entity.getFilterField())
                .operator(entity.getFilterOperator())
                .contextField(entity.getContextField())
                .filterType(entity.getFilterType())
                .subquerySql(entity.getSubquerySql())
                .build();

        Map<String, DataPermissionRule.FieldRule> fieldRules = new HashMap<>();
        fieldRules.put(entity.getRoleType(), fieldRule);

        return DataPermissionRule.builder()
                .tableName(entity.getTableName())
                .entityClass(entity.getEntityClass())
                .fieldRules(fieldRules)
                .build();
    }
}
