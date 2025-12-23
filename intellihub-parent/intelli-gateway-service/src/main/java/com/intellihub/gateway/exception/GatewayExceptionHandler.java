package com.intellihub.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.common.ApiResponse;
import io.netty.channel.ConnectTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

/**
 * 网关全局异常处理器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@Order(-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<?> errorResponse = ApiResponse.failed(5000, "网关异常");

        // 根据异常类型设置不同的错误信息
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            int statusCode = rse.getStatus().value();
            response.setStatusCode(rse.getStatus());

            switch (statusCode) {
                case 404:
                    errorResponse = ApiResponse.failed(4001, "请求的服务不存在");
                    break;
                case 504:
                    errorResponse = ApiResponse.failed(5000, "服务调用超时");
                    break;
                case 502:
                    errorResponse = ApiResponse.failed(5000, "服务不可用");
                    break;
                case 429:
                    errorResponse = ApiResponse.failed(5101, "请求过于频繁");
                    break;
                default:
                    errorResponse = ApiResponse.failed(statusCode, rse.getReason());
            }
        } else if (ex instanceof IllegalArgumentException && ex.getMessage() != null 
                && ex.getMessage().contains("invalid version format")) {
            // 处理非标准 HTTP 请求（如 HTTPS 请求发到 HTTP 端口）
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            errorResponse = ApiResponse.failed(4000, "无效的请求格式，请检查请求协议");
            log.warn("收到非标准HTTP请求: {}", ex.getMessage());
        } else if (ex instanceof ConnectTimeoutException) {
            response.setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
            errorResponse = ApiResponse.failed(5040, "连接下游服务超时");
            log.error("连接超时: {}", ex.getMessage());
        } else if (ex instanceof PrematureCloseException) {
            response.setStatusCode(HttpStatus.BAD_GATEWAY);
            errorResponse = ApiResponse.failed(5020, "下游服务连接异常关闭");
            log.error("连接异常关闭: {}", ex.getMessage());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            log.error("网关异常:", ex);
        }

        try {
            // 将响应写入输出流
            String result = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(result.getBytes());
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("写入响应失败:", e);
            return Mono.error(e);
        }
    }
}