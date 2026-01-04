package com.intellihub.gateway.service.dubbo.extractor;

import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import com.intellihub.gateway.service.dubbo.enums.ParameterSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * Query参数提取器
 * <p>
 * 从URL Query参数中提取参数，如 ?name=test&age=18
 * 单值参数直接存储，多值参数存储为List
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class QueryParameterExtractor implements ParameterExtractor {

    private static final int ORDER = 200;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public void extract(ServerWebExchange exchange, DubboInvocationContext context) {
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        
        if (queryParams.isEmpty()) {
            return;
        }

        int extractedCount = 0;
        for (String key : queryParams.keySet()) {
            List<String> values = queryParams.get(key);
            if (values != null && !values.isEmpty()) {
                if (values.size() == 1) {
                    context.addParameter(key, values.get(0));
                } else {
                    context.addParameter(key, values);
                }
                extractedCount++;
            }
        }

        if (extractedCount > 0) {
            log.debug("[{}] 提取成功: count={}, keys={}", 
                    ParameterSource.QUERY.getDescription(), extractedCount, queryParams.keySet());
        }
    }
}
