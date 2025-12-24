package com.intellihub.interceptor;

import com.intellihub.constants.CommonConstants;
import com.intellihub.context.UserContext;
import com.intellihub.context.UserContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户上下文拦截器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader(CommonConstants.USER_ID_HEADER);
        String tenantIdStr = request.getHeader(CommonConstants.TENANT_ID_HEADER);
        String username = request.getHeader("X-Username");

        if (userIdStr != null && !userIdStr.isEmpty()) {
            UserContext context = new UserContext();
            // 保存String类型ID
            context.setUserIdStr(userIdStr);
            context.setTenantIdStr(tenantIdStr);
            context.setUsername(username);
            
            // 尝试解析为Long类型（兼容数字ID）
            try {
                context.setUserId(Long.parseLong(userIdStr));
            } catch (NumberFormatException ignored) {
                // UUID格式，仅保留String类型
            }
            if (tenantIdStr != null && !tenantIdStr.isEmpty()) {
                try {
                    context.setTenantId(Long.parseLong(tenantIdStr));
                } catch (NumberFormatException ignored) {
                    // UUID格式，仅保留String类型
                }
            }
            
            UserContextHolder.set(context);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
