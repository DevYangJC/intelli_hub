package com.intellihub.event.handler.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 请求策略接口 - 策略模式
 * 定义不同 HTTP 方法的请求策略
 *
 * @author IntelliHub
 */
public interface HttpRequestStrategy {

    /**
     * 获取支持的 HTTP 方法
     */
    String getMethod();

    /**
     * 执行 HTTP 请求
     *
     * @param restTemplate RestTemplate 客户端
     * @param url          请求 URL
     * @param request      请求实体
     * @return 响应实体
     */
    ResponseEntity<String> execute(RestTemplate restTemplate, String url, HttpEntity<?> request);
}
