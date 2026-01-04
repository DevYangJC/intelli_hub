package com.intellihub.gateway.service;

import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import com.intellihub.gateway.service.dubbo.strategy.InvocationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dubbo泛化调用服务
 * <p>
 * 支持在不依赖服务接口的情况下调用Dubbo服务
 * 用于开放API的Dubbo后端类型转发
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
public class DubboGenericService {

    @Value("${dubbo.registry.address:nacos://127.0.0.1:8848}")
    private String registryAddress;

    @Value("${dubbo.application.name:intelli-gateway-service}")
    private String applicationName;

    @Value("${dubbo.consumer.group:}")
    private String defaultGroup;

    private ApplicationConfig applicationConfig;
    private RegistryConfig registryConfig;

    /**
     * 调用策略列表（Spring自动注入）
     */
    private final List<InvocationStrategy> strategies;

    /**
     * GenericService缓存（接口名:version:group -> GenericService）
     */
    private final Map<String, GenericService> genericServiceCache = new ConcurrentHashMap<>();

    public DubboGenericService(List<InvocationStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void init() {
        applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName + "-generic");

        registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);

        log.info("Dubbo泛化调用服务初始化完成: registry={}, strategies={}", 
                registryAddress, strategies.stream().map(InvocationStrategy::getStrategyName).collect(java.util.stream.Collectors.toList()));
    }

    /**
     * 执行Dubbo泛化调用（使用调用上下文）
     * <p>
     * 策略模式：根据参数数量自动选择合适的调用策略
     * </p>
     *
     * @param context Dubbo调用上下文
     * @return 调用结果
     */
    public Mono<Object> invoke(DubboInvocationContext context) {
        return Mono.fromCallable(() -> {
            try {
                log.info("[DubboGenericService] 开始泛化调用: {}", context.toSummary());

                // 获取或创建GenericService
                String cacheKey = buildCacheKey(context.getInterfaceName(), context.getVersion(), context.getGroup());
                GenericService genericService = genericServiceCache.computeIfAbsent(cacheKey,
                        k -> createGenericService(context.getInterfaceName(), context.getVersion(), 
                                context.getGroup(), context.getTimeout()));

                // 选择并执行调用策略
                InvocationStrategy strategy = selectStrategy(context);
                log.debug("[DubboGenericService] 使用策略: {}", strategy.getStrategyName());
                
                Object result = strategy.invoke(genericService, context);

                log.info("[DubboGenericService] 泛化调用成功: interface={}, method={}, resultType={}", 
                        context.getInterfaceName(), context.getMethodName(), 
                        result != null ? result.getClass().getSimpleName() : "null");
                return result;

            } catch (Exception e) {
                log.error("[DubboGenericService] 泛化调用失败: {}, error={}", context.toSummary(), e.getMessage(), e);
                throw e;
            }
        });
    }

    /**
     * 选择合适的调用策略
     *
     * @param context 调用上下文
     * @return 匹配的调用策略
     * @throws IllegalStateException 如果没有找到合适的策略
     */
    private InvocationStrategy selectStrategy(DubboInvocationContext context) {
        for (InvocationStrategy strategy : strategies) {
            if (strategy.supports(context)) {
                return strategy;
            }
        }
        throw new IllegalStateException("没有找到合适的调用策略: paramCount=" + context.getParameterCount());
    }

    /**
     * 创建GenericService实例
     * <p>
     * 根据接口配置创建Dubbo泛化服务引用，支持版本和分组配置
     * </p>
     *
     * @param interfaceName 接口全限定名
     * @param version       版本号（可为空）
     * @param group         分组（可为空，为空时使用默认分组）
     * @param timeout       超时时间（毫秒）
     * @return GenericService实例
     */
    private GenericService createGenericService(String interfaceName, String version, String group, Integer timeout) {
        log.info("[DubboGenericService] 创建GenericService: interface={}, version={}, group={}, timeout={}, registry={}", 
                interfaceName, version, group, timeout, registryAddress);
        
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setApplication(applicationConfig);
        reference.setRegistry(registryConfig);
        reference.setInterface(interfaceName);
        reference.setGeneric("true");

        // 设置版本号
        if (version != null && !version.isEmpty()) {
            reference.setVersion(version);
        }
        
        // 设置分组（优先使用传入的group，为空时使用默认配置）
        String effectiveGroup = (group != null && !group.isEmpty()) ? group : defaultGroup;
        if (effectiveGroup != null && !effectiveGroup.isEmpty()) {
            reference.setGroup(effectiveGroup);
        }
        
        // 设置超时时间
        reference.setTimeout(timeout != null && timeout > 0 ? timeout : 5000);
        reference.setRetries(0);

        try {
            GenericService service = reference.get();
            log.info("[DubboGenericService] GenericService创建成功: interface={}, effectiveGroup={}", 
                    interfaceName, effectiveGroup);
            return service;
        } catch (Exception e) {
            log.error("[DubboGenericService] GenericService创建失败: interface={}, version={}, group={}, error={}", 
                    interfaceName, version, effectiveGroup, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 构建缓存Key
     */
    private String buildCacheKey(String interfaceName, String version, String group) {
        StringBuilder sb = new StringBuilder(interfaceName);
        if (version != null && !version.isEmpty()) {
            sb.append(":").append(version);
        }
        if (group != null && !group.isEmpty()) {
            sb.append(":").append(group);
        }
        return sb.toString();
    }

    /**
     * 清除GenericService缓存
     */
    public void clearCache() {
        genericServiceCache.clear();
        log.info("已清除GenericService缓存");
    }

    /**
     * 移除指定接口的缓存
     */
    public void removeCache(String interfaceName, String version, String group) {
        String cacheKey = buildCacheKey(interfaceName, version, group);
        genericServiceCache.remove(cacheKey);
        log.info("移除GenericService缓存 - key: {}", cacheKey);
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return genericServiceCache.size();
    }
}
