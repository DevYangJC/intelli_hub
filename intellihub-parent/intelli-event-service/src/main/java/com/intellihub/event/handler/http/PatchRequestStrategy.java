package com.intellihub.event.handler.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * PATCH 请求策略
 *
 * @author IntelliHub
 */
@Component
public class PatchRequestStrategy implements HttpRequestStrategy {

    @Override
    public String getMethod() {
        return "PATCH";
    }

    @Override
    public ResponseEntity<String> execute(RestTemplate restTemplate, String url, HttpEntity<?> request) {
        return restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);
    }
}
