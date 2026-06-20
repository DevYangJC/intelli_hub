package com.intellihub.aop.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * API日志切面
 * 记录所有Controller接口的请求路径、参数和响应结果
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class ApiLogAspect {

    private final ObjectMapper objectMapper;

    /**
     * 切入点：所有Controller类的public方法
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：记录请求和响应
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        
        // 请求路径和方法
        String requestPath = request != null ? request.getRequestURI() : "unknown";
        String httpMethod = request != null ? request.getMethod() : "unknown";
        
        // 请求头
        Map<String, String> headers = new HashMap<>();
        if (request != null) {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // 只记录关键请求头，避免日志过长
                if (headerName.toLowerCase().startsWith("x-") || 
                    headerName.equalsIgnoreCase("authorization") ||
                    headerName.equalsIgnoreCase("content-type")) {
                    String headerValue = request.getHeader(headerName);
                    // 隐藏敏感信息
                    if (headerName.equalsIgnoreCase("authorization") && headerValue != null) {
                        headerValue = headerValue.length() > 20 ? headerValue.substring(0, 20) + "..." : headerValue;
                    }
                    headers.put(headerName, headerValue);
                }
            }
        }
        
        // 请求参数
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            // 跳过HttpServletRequest、HttpServletResponse等特殊参数
            if (arg != null && !arg.getClass().getName().startsWith("javax.servlet") 
                && !arg.getClass().getName().startsWith("org.springframework")) {
                String paramName = paramNames != null && i < paramNames.length ? paramNames[i] : "arg" + i;
                // 隐藏密码字段
                if (paramName.toLowerCase().contains("password")) {
                    params.put(paramName, "******");
                } else {
                    params.put(paramName, arg);
                }
            }
        }
        
        // 打印请求日志
        log.info("========== API请求开始 ==========");
        log.info("请求路径: {} {}", httpMethod, requestPath);
        log.info("控制器: {}.{}", className, methodName);
        log.info("请求头: {}", headers);
        try {
            log.info("请求参数: {}", objectMapper.writeValueAsString(params));
        } catch (Exception e) {
            log.info("请求参数: {}", params);
        }
        
        Object result = null;
        Exception exception = null;
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            
            // 打印响应日志
            if (exception != null) {
                log.error("响应异常: {}", exception.getMessage());
            } else {
                try {
                    String resultJson = objectMapper.writeValueAsString(result);
                    // 如果响应太长，截断显示
                    if (resultJson.length() > 1000) {
                        resultJson = resultJson.substring(0, 1000) + "...(truncated)";
                    }
                    log.info("响应结果: {}", resultJson);
                } catch (Exception e) {
                    log.info("响应结果: {}", result);
                }
            }
            log.info("耗时: {}ms", costTime);
            log.info("========== API请求结束 ==========\n");
        }
    }
}
