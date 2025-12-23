package com.intellihub.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.intellihub.gateway.config.FilterOrderConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 请求Body缓存过滤器
 * <p>
 * 在最早期缓存请求Body，使其可以被多次读取
 * 解决Dubbo泛化调用等场景需要读取Body的问题
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class CacheBodyFilter implements GlobalFilter, Ordered {

    /**
     * Exchange属性Key - 缓存的请求Body
     */
    public static final String ATTR_CACHED_BODY = "gateway.cached.body";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();

        // 只对可能有Body的请求方法进行缓存
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
            return DataBufferUtils.join(request.getBody())
                    .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
                    .flatMap(dataBuffer -> {
                        // 读取Body内容
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        String bodyString = new String(bytes, StandardCharsets.UTF_8);
                        
                        // 缓存Body到exchange属性
                        exchange.getAttributes().put(ATTR_CACHED_BODY, bodyString);
                        
                        log.debug("缓存请求Body - path: {}, length: {}", request.getPath(), bytes.length);

                        // 创建新的Request，使Body可以重复读取
                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                if (bytes.length > 0) {
                                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                                    return Flux.just(buffer);
                                }
                                return Flux.empty();
                            }
                        };

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    });
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return FilterOrderConfig.CACHE_BODY_FILTER;
    }
}
