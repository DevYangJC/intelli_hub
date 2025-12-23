package com.intellihub.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要登录注解
 * 用于标记需要用户登录才能访问的接口
 *
 * @author intellihub
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {

    /**
     * 是否需要管理员权限
     */
    boolean admin() default false;

    /**
     * 所需的角色
     */
    String[] roles() default {};

    /**
     * 所需的权限
     */
    String[] permissions() default {};
}