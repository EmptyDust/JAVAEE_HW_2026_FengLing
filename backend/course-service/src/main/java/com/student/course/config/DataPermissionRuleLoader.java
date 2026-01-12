package com.student.course.config;

import com.student.common.datapermission.DataPermissionRuleRegistry;
import com.student.common.entity.DataPermissionRuleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据权限规则加载器
 * 在应用启动时从数据库加载权限规则到缓存
 */
@Component
@Slf4j
public class DataPermissionRuleLoader implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataPermissionRuleRegistry ruleRegistry;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 从数据库查询所有启用的权限规则
            String sql = "SELECT * FROM data_permission_rule WHERE enabled = 1";
            List<DataPermissionRuleEntity> rules = jdbcTemplate.query(
                    sql,
                    new BeanPropertyRowMapper<>(DataPermissionRuleEntity.class)
            );

            // 加载到缓存
            ruleRegistry.loadRules(rules);

            log.info("Data permission rules loaded successfully: {} rules", rules.size());
        } catch (Exception e) {
            log.error("Failed to load data permission rules", e);
            // 不抛出异常，允许服务继续启动
        }
    }
}
