package com.intellihub.event.handler.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * POST 请求策略
 *
 * @author IntelliHub
 */
@Component
public class PostRequestStrategy implements HttpRequestStrategy {

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public ResponseEntity<String> execute(RestTemplate restTemplate, String url, HttpEntity<?> request) {
        return restTemplate.postForEntity(url, request, String.class);
    }
}
