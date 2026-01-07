package com.intellihub.aigc.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 日志拦截器
 * 记录请求日志并生成TraceId
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "X-Trace-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        // 生成或获取TraceId
        String traceId = request.getHeader(TRACE_ID);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        
        // 设置到响应头
        response.setHeader(TRACE_ID, traceId);
        
        // 设置到线程变量（可用于MDC）
        request.setAttribute(TRACE_ID, traceId);
        
        // 记录请求日志
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        
        log.info("请求开始 - traceId: {}, method: {}, uri: {}, query: {}, ip: {}", 
                traceId, 
                request.getMethod(), 
                request.getRequestURI(), 
                request.getQueryString(),
                getClientIp(request));
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) throws Exception {
        
        String traceId = (String) request.getAttribute(TRACE_ID);
        Long startTime = (Long) request.getAttribute("startTime");
        
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            if (ex != null) {
                log.error("请求异常 - traceId: {}, duration: {}ms, error: {}", 
                        traceId, duration, ex.getMessage());
            } else {
                log.info("请求完成 - traceId: {}, duration: {}ms, status: {}", 
                        traceId, duration, response.getStatus());
            }
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
