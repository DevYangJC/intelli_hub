package com.intellihub.common.interceptor;

import com.intellihub.common.context.UserContextHolder;
import com.intellihub.common.dto.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户上下文拦截器
 * 从请求头中提取用户信息并设置到上下文中
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取用户信息
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String nickname = request.getHeader("X-User-Nickname");
        String email = request.getHeader("X-User-Email");
        String phone = request.getHeader("X-User-Phone");
        String avatar = request.getHeader("X-User-Avatar");
        String userStatus = request.getHeader("X-User-Status");

        // 获取租户信息
        String tenantId = request.getHeader("X-Tenant-Id");
        String tenantCode = request.getHeader("X-Tenant-Code");

        // 如果没有用户ID，说明是未认证请求或白名单接口
        if (!StringUtils.hasText(userId)) {
            return true;
        }

        // 构建用户上下文
        UserContext userContext = UserContext.builder()
                .userId(userId)
                .username(username)
                .nickname(nickname)
                .email(email)
                .phone(phone)
                .avatar(avatar)
                .status(StringUtils.hasText(userStatus) ? Integer.parseInt(userStatus) : null)
                .tenantId(tenantId)
                .tenantCode(tenantCode)
                .build();

        // 设置到上下文
        UserContextHolder.setUserContext(userContext);

        log.debug("设置用户上下文成功 - UserId: {}, TenantId: {}", userId, tenantId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除用户上下文
        UserContextHolder.clear();
        log.debug("清除用户上下文");
    }
}