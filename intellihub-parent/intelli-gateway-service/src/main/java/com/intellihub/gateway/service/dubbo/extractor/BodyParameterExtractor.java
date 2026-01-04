package com.intellihub.gateway.service.dubbo.extractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.gateway.filter.CacheBodyFilter;
import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import com.intellihub.gateway.service.dubbo.enums.ParameterSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * 请求体参数提取器
 * <p>
 * 从POST/PUT/PATCH请求的JSON Body中提取参数
 * 依赖 CacheBodyFilter 预先缓存的请求体内容
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class BodyParameterExtractor implements ParameterExtractor {

    private static final int ORDER = 300;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public boolean supports(ServerWebExchange exchange, DubboInvocationContext context) {
        HttpMethod method = exchange.getRequest().getMethod();
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
    }

    @Override
    public void extract(ServerWebExchange exchange, DubboInvocationContext context) {
        String cachedBody = exchange.getAttribute(CacheBodyFilter.ATTR_CACHED_BODY);
        
        if (cachedBody == null || cachedBody.isEmpty()) {
            log.debug("[{}] 请求体为空，跳过提取", ParameterSource.BODY.getDescription());
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyParams = objectMapper.readValue(cachedBody, Map.class);
            
            if (!bodyParams.isEmpty()) {
                context.addParameters(bodyParams);
                log.debug("[{}] 提取成功: count={}, keys={}", 
                        ParameterSource.BODY.getDescription(), bodyParams.size(), bodyParams.keySet());
            }
        } catch (Exception e) {
            String preview = cachedBody.length() > 100 ? cachedBody.substring(0, 100) + "..." : cachedBody;
            log.warn("[{}] JSON解析失败: body={}, error={}", 
                    ParameterSource.BODY.getDescription(), preview, e.getMessage());
        }
    }
}
