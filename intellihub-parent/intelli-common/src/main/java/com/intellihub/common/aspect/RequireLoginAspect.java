package com.intellihub.common.aspect;

import com.intellihub.common.annotation.RequireLogin;
import com.intellihub.common.context.UserContextHolder;
import com.intellihub.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 登录验证切面
 * 处理@RequireLogin注解的验证逻辑
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class RequireLoginAspect {

    @Before("@within(requireLogin) || @annotation(requireLogin)")
    public void checkLogin(JoinPoint joinPoint, RequireLogin requireLogin) {
        // 如果方法上没有注解，尝试从类上获取
        if (requireLogin == null) {
            requireLogin = joinPoint.getTarget().getClass().getAnnotation(RequireLogin.class);
        }

        if (requireLogin == null) {
            // 从方法签名获取注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            requireLogin = signature.getMethod().getAnnotation(RequireLogin.class);
        }

        if (requireLogin == null) {
            return;
        }

        // 检查是否已登录
        if (UserContextHolder.getCurrentUserId() == null) {
            throw new UnauthorizedException("用户未登录");
        }

        // 检查是否需要管理员权限
        if (requireLogin.admin() && !UserContextHolder.isAdmin()) {
            throw new UnauthorizedException("需要管理员权限");
        }

        // TODO: 检查角色权限（需要集成RBAC系统）
        if (requireLogin.roles().length > 0) {
            String[] requiredRoles = requireLogin.roles();
            // 角色验证逻辑
            log.debug("检查角色权限，所需角色: {}", Arrays.toString(requiredRoles));
        }

        // TODO: 检查具体权限
        if (requireLogin.permissions().length > 0) {
            String[] requiredPermissions = requireLogin.permissions();
            // 权限验证逻辑
            log.debug("检查具体权限，所需权限: {}", Arrays.toString(requiredPermissions));
        }
    }
}