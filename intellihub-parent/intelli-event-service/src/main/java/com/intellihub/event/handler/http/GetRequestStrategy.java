package com.intellihub.event.handler.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * GET 请求策略
 *
 * @author IntelliHub
 */
@Component
public class GetRequestStrategy implements HttpRequestStrategy {

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public ResponseEntity<String> execute(RestTemplate restTemplate, String url, HttpEntity<?> request) {
        return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }
}
