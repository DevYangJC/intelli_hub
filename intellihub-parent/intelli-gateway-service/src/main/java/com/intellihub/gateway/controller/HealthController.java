package com.intellihub.gateway.controller;

import com.intellihub.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 网关健康检查控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/actuator")
public class HealthController {

    /**
     * 网关健康检查
     */
    @GetMapping("/gateway-health")
    public Mono<ApiResponse<String>> health() {
        return Mono.just(ApiResponse.success("Gateway is running"));
    }

    /**
     * 获取网关信息
     */
    @GetMapping("/gateway-info")
    public Mono<ApiResponse<String>> info() {
        return Mono.just(ApiResponse.success("IntelliHub Gateway Service v1.0.0"));
    }
}