package com.intellihub.gateway.service.dubbo;

import com.intellihub.dubbo.ApiRouteDTO;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dubbo泛化调用上下文
 * <p>
 * 封装一次Dubbo泛化调用所需的所有信息，包括：
 * - 路由配置（接口名、方法名、分组、版本等）
 * - 提取的参数（按顺序存储）
 * - 原始请求信息
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
public class DubboInvocationContext {

    /**
     * API路由配置
     */
    private ApiRouteDTO route;

    /**
     * 提取的参数（使用LinkedHashMap保持插入顺序）
     */
    @Builder.Default
    private Map<String, Object> parameters = new LinkedHashMap<>();

    /**
     * 原始请求路径
     */
    private String originalPath;

    /**
     * 请求方法（GET/POST/PUT/DELETE等）
     */
    private String httpMethod;

    /**
     * 获取Dubbo接口名称
     */
    public String getInterfaceName() {
        return route != null ? route.getDubboInterface() : null;
    }

    /**
     * 获取Dubbo方法名称
     */
    public String getMethodName() {
        return route != null ? route.getDubboMethod() : null;
    }

    /**
     * 获取Dubbo分组
     */
    public String getGroup() {
        return route != null ? route.getDubboGroup() : null;
    }

    /**
     * 获取Dubbo版本
     */
    public String getVersion() {
        return route != null ? route.getDubboVersion() : null;
    }

    /**
     * 获取超时时间
     */
    public Integer getTimeout() {
        return route != null ? route.getTimeout() : null;
    }

    /**
     * 获取路由配置的路径模板
     */
    public String getRoutePath() {
        return route != null ? route.getPath() : null;
    }

    /**
     * 获取参数数量
     */
    public int getParameterCount() {
        return parameters != null ? parameters.size() : 0;
    }

    /**
     * 判断是否有参数
     */
    public boolean hasParameters() {
        return parameters != null && !parameters.isEmpty();
    }

    /**
     * 添加参数
     *
     * @param name  参数名
     * @param value 参数值
     */
    public void addParameter(String name, Object value) {
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
        }
        parameters.put(name, value);
    }

    /**
     * 批量添加参数
     *
     * @param params 参数Map
     */
    public void addParameters(Map<String, ?> params) {
        if (params != null && !params.isEmpty()) {
            if (parameters == null) {
                parameters = new LinkedHashMap<>();
            }
            parameters.putAll(params);
        }
    }

    /**
     * 构建上下文摘要信息（用于日志）
     */
    public String toSummary() {
        return String.format("interface=%s, method=%s, group=%s, version=%s, paramCount=%d, params=%s",
                getInterfaceName(), getMethodName(), getGroup(), getVersion(),
                getParameterCount(), parameters != null ? parameters.keySet() : "[]");
    }
}
