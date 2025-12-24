package com.intellihub.gateway.service;

import com.intellihub.dubbo.ApiRouteDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
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

    private ApplicationConfig applicationConfig;
    private RegistryConfig registryConfig;

    /**
     * GenericService缓存（接口名 -> GenericService）
     */
    private final Map<String, GenericService> genericServiceCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化Dubbo应用配置
        applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName + "-generic");

        // 初始化注册中心配置
        registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);

        log.info("Dubbo泛化调用服务初始化完成 - registry: {}", registryAddress);
    }

    /**
     * 执行Dubbo泛化调用
     *
     * @param route          路由配置
     * @param parameterTypes 参数类型数组
     * @param args           参数值数组
     * @return 调用结果
     */
    public Mono<Object> invoke(ApiRouteDTO route, String[] parameterTypes, Object[] args) {
        return Mono.fromCallable(() -> {
            try {
                String interfaceName = route.getDubboInterface();
                String methodName = route.getDubboMethod();
                String version = route.getDubboVersion();
                String group = route.getDubboGroup();

                log.debug("执行Dubbo泛化调用 - interface: {}, method: {}, version: {}, group: {}",
                        interfaceName, methodName, version, group);

                // 获取或创建GenericService
                String cacheKey = buildCacheKey(interfaceName, version, group);
                GenericService genericService = genericServiceCache.computeIfAbsent(cacheKey,
                        k -> createGenericService(interfaceName, version, group, route.getTimeout()));

                // 执行泛化调用
                Object result = genericService.$invoke(methodName, parameterTypes, args);

                log.debug("Dubbo泛化调用成功 - interface: {}, method: {}", interfaceName, methodName);
                return result;

            } catch (Exception e) {
                log.error("Dubbo泛化调用失败 - interface: {}, method: {}",
                        route.getDubboInterface(), route.getDubboMethod(), e);
                throw e;
            }
        });
    }

    /**
     * 执行Dubbo泛化调用（简化版，无参数类型）
     *
     * @param route 路由配置
     * @param args  参数值（Map形式）
     * @return 调用结果
     */
    public Mono<Object> invoke(ApiRouteDTO route, Map<String, Object> args) {
        return Mono.fromCallable(() -> {
            try {
                String interfaceName = route.getDubboInterface();
                String methodName = route.getDubboMethod();
                String version = route.getDubboVersion();
                String group = route.getDubboGroup();

                log.debug("执行Dubbo泛化调用（Map参数） - interface: {}, method: {}",
                        interfaceName, methodName);

                // 获取或创建GenericService
                String cacheKey = buildCacheKey(interfaceName, version, group);
                GenericService genericService = genericServiceCache.computeIfAbsent(cacheKey,
                        k -> createGenericService(interfaceName, version, group, route.getTimeout()));

                // 执行泛化调用（使用Map作为参数）
                Object result;
                if (args == null || args.isEmpty()) {
                    result = genericService.$invoke(methodName, new String[]{}, new Object[]{});
                } else {
                    result = genericService.$invoke(methodName,
                            new String[]{"java.util.Map"},
                            new Object[]{args});
                }

                log.debug("Dubbo泛化调用成功 - interface: {}, method: {}", interfaceName, methodName);
                return result;

            } catch (Exception e) {
                log.error("Dubbo泛化调用失败 - interface: {}, method: {}",
                        route.getDubboInterface(), route.getDubboMethod(), e);
                throw e;
            }
        });
    }

    /**
     * 创建GenericService
     */
    private GenericService createGenericService(String interfaceName, String version, String group, Integer timeout) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setApplication(applicationConfig);
        reference.setRegistry(registryConfig);
        reference.setInterface(interfaceName);
        reference.setGeneric("true");

        if (version != null && !version.isEmpty()) {
            reference.setVersion(version);
        }
        if (group != null && !group.isEmpty()) {
            reference.setGroup(group);
        }
        if (timeout != null && timeout > 0) {
            reference.setTimeout(timeout);
        } else {
            reference.setTimeout(5000);
        }

        // 设置重试次数
        reference.setRetries(0);

        log.info("创建GenericService - interface: {}, version: {}, group: {}, timeout: {}",
                interfaceName, version, group, timeout);

        return reference.get();
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
