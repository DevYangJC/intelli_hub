package com.intellihub.event.handler.http;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 请求策略工厂
 * 根据 HTTP 方法获取对应的请求策略
 *
 * @author IntelliHub
 */
@Component
public class HttpRequestStrategyFactory {

    private final List<HttpRequestStrategy> strategies;
    private final Map<String, HttpRequestStrategy> strategyMap = new HashMap<>();

    public HttpRequestStrategyFactory(List<HttpRequestStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void init() {
        for (HttpRequestStrategy strategy : strategies) {
            strategyMap.put(strategy.getMethod().toUpperCase(), strategy);
        }
    }

    /**
     * 获取 HTTP 请求策略
     *
     * @param method HTTP 方法（POST/PUT/PATCH/GET）
     * @return 请求策略，默认返回 POST 策略
     */
    public HttpRequestStrategy getStrategy(String method) {
        if (method == null) {
            return strategyMap.get("POST");
        }
        return strategyMap.getOrDefault(method.toUpperCase(), strategyMap.get("POST"));
    }
}
