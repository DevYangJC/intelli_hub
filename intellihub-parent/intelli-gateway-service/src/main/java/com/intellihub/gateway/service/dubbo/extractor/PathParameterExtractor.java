package com.intellihub.gateway.service.dubbo.extractor;

import com.intellihub.gateway.filter.OriginalPathSaveFilter;
import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import com.intellihub.gateway.service.dubbo.enums.ParameterSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * 路径参数提取器
 * <p>
 * 从URL路径中提取参数，如 /open/app/{appkey} 中的 appkey
 * 使用 AntPathMatcher 进行路径模板匹配和变量提取
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class PathParameterExtractor implements ParameterExtractor {

    private static final int ORDER = 100;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public boolean supports(ServerWebExchange exchange, DubboInvocationContext context) {
        String routePath = context.getRoutePath();
        return routePath != null && routePath.contains("{");
    }

    @Override
    public void extract(ServerWebExchange exchange, DubboInvocationContext context) {
        String routePath = context.getRoutePath();
        String originalPath = context.getOriginalPath();

        if (originalPath == null) {
            originalPath = exchange.getAttribute(OriginalPathSaveFilter.ATTR_ORIGINAL_PATH);
            if (originalPath == null) {
                originalPath = exchange.getRequest().getURI().getPath();
            }
            context.setOriginalPath(originalPath);
        }

        try {
            if (pathMatcher.match(routePath, originalPath)) {
                Map<String, String> pathVariables = pathMatcher.extractUriTemplateVariables(routePath, originalPath);
                if (!pathVariables.isEmpty()) {
                    context.addParameters(pathVariables);
                    log.debug("[{}] 提取成功: routePath={}, originalPath={}, params={}", 
                            ParameterSource.PATH.getDescription(), routePath, originalPath, pathVariables);
                }
            } else {
                log.warn("[{}] 路径模板不匹配: routePath={}, originalPath={}", 
                        ParameterSource.PATH.getDescription(), routePath, originalPath);
            }
        } catch (Exception e) {
            log.warn("[{}] 提取失败: routePath={}, originalPath={}, error={}", 
                    ParameterSource.PATH.getDescription(), routePath, originalPath, e.getMessage());
        }
    }
}
