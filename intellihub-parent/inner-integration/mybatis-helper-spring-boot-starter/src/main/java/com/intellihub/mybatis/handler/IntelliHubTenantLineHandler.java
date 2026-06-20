package com.intellihub.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.intellihub.context.UserContextHolder;
import com.intellihub.mybatis.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * IntelliHub 多租户处理器
 * 自动在SQL中注入 tenant_id 条件
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class IntelliHubTenantLineHandler implements TenantLineHandler {

    private final TenantProperties tenantProperties;

    /**
     * 获取租户ID值
     * 从 UserContextHolder 中获取当前租户ID
     */
    @Override
    public Expression getTenantId() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        if (tenantId != null) {
            log.trace("多租户拦截器 - 获取租户ID: {}", tenantId);
            return new StringValue(tenantId);
        }
        // 如果没有租户ID，返回一个不存在的值，避免查询到数据
        log.trace("多租户拦截器 - 未找到租户ID，使用默认值: UNKNOWN");
        return new StringValue("UNKNOWN");
    }

    /**
     * 获取租户字段名
     * 默认为 tenant_id
     */
    @Override
    public String getTenantIdColumn() {
        return tenantProperties.getColumn();
    }

    /**
     * 判断是否忽略租户隔离
     *
     * @param tableName 表名
     * @return true-忽略（不拼接tenant_id），false-不忽略（拼接tenant_id）
     */
    @Override
    public boolean ignoreTable(String tableName) {
        // 1. 可跨租户访问的角色豁免租户隔离（超级管理员）
        if (UserContextHolder.canCrossTenant() || UserContextHolder.isIgnoreTenant()) {
            log.trace("多租户拦截器 - 豁免租户隔离: table={}, reason=跨租户角色或手动豁免", tableName);
            return true;
        }

        // 2. 配置的忽略表（无tenant_id字段的表）
        if (tenantProperties.getIgnoreTables().contains(tableName)) {
            log.trace("多租户拦截器 - 忽略表: table={}, reason=配置忽略", tableName);
            return true;
        }

        log.trace("多租户拦截器 - 应用租户隔离: table={}", tableName);
        return false;
    }
}
