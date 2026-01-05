package com.intellihub.mybatis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 多租户配置属性
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "intellihub.mybatis.tenant")
public class TenantProperties {

    /**
     * 是否启用多租户功能
     * 默认启用
     */
    private boolean enabled = true;

    /**
     * 租户字段名
     * 默认为 tenant_id
     */
    private String column = "tenant_id";

    /**
     * 不需要租户隔离的表清单
     * 默认包含全局共享表（无 tenant_id 字段的表）
     * 各服务可通过配置追加自己的忽略表
     */
    private List<String> ignoreTables = new ArrayList<String>() {{
        // === IAM 服务 - 全局共享表 ===
        add("iam_tenant");              // 租户表本身（无 tenant_id）
        add("iam_permission");          // 权限表（全局共享，无 tenant_id）
        add("iam_menu");                // 菜单表（全局共享，无 tenant_id）
        add("iam_login_log");           // 登录日志（全局记录）
        add("iam_role_permission");     // 角色权限关联（无 tenant_id）
        add("iam_user_role");           // 用户角色关联（无 tenant_id）
        
        // === API Platform 服务 - 子表（跟随主表隔离）===
        add("api_request_param");       // API请求参数（跟随 api_id）
        add("api_response_param");      // API响应参数（跟随 api_id）
        add("api_backend");             // API后端配置（跟随 api_id）
        add("api_tag");                 // API标签（跟随 api_id）
        
        // === App Center 服务 ===
        add("app_api_subscription");    // 应用API订阅（跟随 app_id）
        
        // === Governance 服务 ===
        add("alert_request_detail");    // 告警请求详情（跟随 alert_record_id）
        
        // === 系统配置表 ===
        add("sys_config");              // 系统配置（全局共享）
        add("sys_announcement");        // 系统公告（全局共享）
    }};
}
