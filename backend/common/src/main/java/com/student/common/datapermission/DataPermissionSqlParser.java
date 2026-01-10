package com.student.common.datapermission;

import com.student.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限SQL解析器
 * 使用JSqlParser解析SQL并添加数据权限过滤条件
 */
@Component
@Slf4j
public class DataPermissionSqlParser {

    /**
     * 添加数据权限过滤条件到SQL
     *
     * @param originalSql 原始SQL
     * @param context     用户上下文
     * @param registry    权限规则注册表
     * @return 修改后的SQL
     */
    public String addDataPermission(String originalSql, UserContext context, DataPermissionRuleRegistry registry) {
        try {
            Statement statement = CCJSqlParserUtil.parse(originalSql);

            if (statement instanceof Select) {
                Select select = (Select) statement;
                processSelect(select, context, registry);
                return select.toString();
            }

            return originalSql;
        } catch (JSQLParserException e) {
            log.error("Failed to parse SQL for data permission: {}", originalSql, e);
            // 故障安全：解析失败时返回原SQL，不影响业务
            return originalSql;
        }
    }

    /**
     * 处理SELECT语句
     */
    private void processSelect(Select select, UserContext context, DataPermissionRuleRegistry registry) {
        if (select instanceof PlainSelect) {
            processPlainSelect((PlainSelect) select, context, registry);
        } else if (select instanceof SetOperationList) {
            // 处理UNION等操作
            SetOperationList setOpList = (SetOperationList) select;
            List<Select> selects = setOpList.getSelects();
            if (selects != null) {
                for (Select s : selects) {
                    processSelect(s, context, registry);
                }
            }
        } else if (select instanceof ParenthesedSelect) {
            // 处理括号包裹的SELECT
            ParenthesedSelect parenthesedSelect = (ParenthesedSelect) select;
            processSelect(parenthesedSelect.getSelect(), context, registry);
        }
    }

    /**
     * 处理普通SELECT语句
     */
    private void processPlainSelect(PlainSelect plainSelect, UserContext context, DataPermissionRuleRegistry registry) {
        FromItem fromItem = plainSelect.getFromItem();

        // 处理主表
        if (fromItem instanceof Table) {
            Table table = (Table) fromItem;
            addPermissionCondition(plainSelect, table, context, registry);
        } else if (fromItem instanceof ParenthesedSelect) {
            // 处理子查询
            ParenthesedSelect parenthesedSelect = (ParenthesedSelect) fromItem;
            processSelect(parenthesedSelect.getSelect(), context, registry);
        }

        // 处理JOIN
        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                FromItem joinItem = join.getRightItem();
                if (joinItem instanceof Table) {
                    Table table = (Table) joinItem;
                    addPermissionCondition(plainSelect, table, context, registry);
                } else if (joinItem instanceof ParenthesedSelect) {
                    ParenthesedSelect parenthesedSelect = (ParenthesedSelect) joinItem;
                    processSelect(parenthesedSelect.getSelect(), context, registry);
                }
            }
        }
    }

    /**
     * 添加权限过滤条件
     */
    private void addPermissionCondition(PlainSelect plainSelect, Table table, UserContext context, DataPermissionRuleRegistry registry) {
        String tableName = table.getName();
        DataPermissionRule rule = registry.getRule(context.getUserType(), tableName);

        if (rule == null) {
            return;  // 没有规则，不过滤
        }

        DataPermissionRule.FieldRule fieldRule = rule.getFieldRules().get(context.getUserType());
        if (fieldRule == null) {
            return;  // 没有该角色的规则
        }

        // 获取表别名
        String alias = table.getAlias() != null ? table.getAlias().getName() : tableName;

        // 构建过滤条件
        Expression condition = buildCondition(alias, fieldRule, context);
        if (condition == null) {
            return;
        }

        // 添加到WHERE子句
        Expression where = plainSelect.getWhere();
        if (where != null) {
            plainSelect.setWhere(new AndExpression(where, condition));
        } else {
            plainSelect.setWhere(condition);
        }
    }

    /**
     * 构建过滤条件
     */
    private Expression buildCondition(String alias, DataPermissionRule.FieldRule fieldRule, UserContext context) {
        Object value = getContextValue(context, fieldRule.getContextField());
        if (value == null) {
            log.warn("Context field {} is null for user {}", fieldRule.getContextField(), context.getUserId());
            return null;
        }

        String columnName = alias + "." + fieldRule.getFieldName();
        Column column = new Column(columnName);

        // 根据过滤类型构建条件
        if ("SUBQUERY".equals(fieldRule.getFilterType())) {
            // 子查询类型
            return buildSubqueryCondition(column, fieldRule, value);
        } else {
            // 简单条件类型
            if ("=".equals(fieldRule.getOperator())) {
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(column);
                equalsTo.setRightExpression(new LongValue(value.toString()));
                return equalsTo;
            } else if ("IN".equals(fieldRule.getOperator())) {
                // IN操作符（暂时简化处理）
                InExpression inExpression = new InExpression();
                inExpression.setLeftExpression(column);
                List<Expression> expressions = new ArrayList<>();
                expressions.add(new LongValue(value.toString()));
                inExpression.setRightExpression(new ExpressionList(expressions));
                return inExpression;
            }
        }

        return null;
    }

    /**
     * 构建子查询条件
     */
    private Expression buildSubqueryCondition(Column column, DataPermissionRule.FieldRule fieldRule, Object value) {
        try {
            String subquerySql = fieldRule.getSubquerySql();
            if (subquerySql == null || subquerySql.isEmpty()) {
                return null;
            }

            // 替换占位符
            subquerySql = subquerySql.replace("?", value.toString());

            // 解析子查询
            Statement statement = CCJSqlParserUtil.parse(subquerySql);
            if (statement instanceof Select) {
                ParenthesedSelect parenthesedSelect = new ParenthesedSelect();
                parenthesedSelect.setSelect((Select) statement);

                InExpression inExpression = new InExpression();
                inExpression.setLeftExpression(column);
                inExpression.setRightExpression(parenthesedSelect);
                return inExpression;
            }
        } catch (JSQLParserException e) {
            log.error("Failed to parse subquery SQL: {}", fieldRule.getSubquerySql(), e);
        }

        return null;
    }

    /**
     * 从UserContext获取字段值
     */
    private Object getContextValue(UserContext context, String fieldName) {
        return switch (fieldName) {
            case "userId" -> context.getUserId();
            case "studentId" -> context.getStudentId();
            case "teacherId" -> context.getTeacherId();
            default -> null;
        };
    }
}
