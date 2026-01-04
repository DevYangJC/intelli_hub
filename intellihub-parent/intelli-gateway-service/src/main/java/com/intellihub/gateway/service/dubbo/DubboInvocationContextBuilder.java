package com.intellihub.gateway.service.dubbo;

import com.intellihub.dubbo.ApiRouteDTO;
import com.intellihub.gateway.filter.OriginalPathSaveFilter;
import com.intellihub.gateway.service.dubbo.extractor.ParameterExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Comparator;
import java.util.List;

/**
 * Dubbo调用上下文建造者
 * <p>
 * 建造者模式：负责构建完整的 DubboInvocationContext
 * 使用责任链模式的参数提取器链，按顺序从不同来源提取参数
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class DubboInvocationContextBuilder {

    private final List<ParameterExtractor> extractors;

    /**
     * 构造函数，注入所有参数提取器并按顺序排序
     *
     * @param extractors 参数提取器列表（Spring自动注入所有实现类）
     */
    public DubboInvocationContextBuilder(List<ParameterExtractor> extractors) {
        this.extractors = extractors;
        this.extractors.sort(Comparator.comparingInt(ParameterExtractor::getOrder));
        log.info("初始化参数提取器链: {}", 
                extractors.stream().map(e -> e.getClass().getSimpleName()).collect(java.util.stream.Collectors.toList()));
    }

    /**
     * 构建Dubbo调用上下文
     * <p>
     * 1. 创建上下文对象，设置基础信息
     * 2. 按顺序执行参数提取器链
     * 3. 返回完整的调用上下文
     * </p>
     *
     * @param exchange HTTP交换对象
     * @param route    API路由配置
     * @return 构建完成的调用上下文
     */
    public DubboInvocationContext build(ServerWebExchange exchange, ApiRouteDTO route) {
        // 获取原始请求路径
        String originalPath = exchange.getAttribute(OriginalPathSaveFilter.ATTR_ORIGINAL_PATH);
        if (originalPath == null) {
            originalPath = exchange.getRequest().getURI().getPath();
        }

        // 创建上下文
        DubboInvocationContext context = DubboInvocationContext.builder()
                .route(route)
                .originalPath(originalPath)
                .httpMethod(exchange.getRequest().getMethod().name())
                .build();

        log.debug("[ContextBuilder] 开始构建上下文: path={}, routePath={}, method={}", 
                originalPath, route.getPath(), context.getHttpMethod());

        // 执行参数提取器链
        for (ParameterExtractor extractor : extractors) {
            if (extractor.supports(exchange, context)) {
                extractor.extract(exchange, context);
            }
        }

        log.info("[ContextBuilder] 上下文构建完成: {}", context.toSummary());
        return context;
    }
}
