package com.intellihub.constants;

import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    CREATED(2001, "创建成功"),

    BAD_REQUEST(4000, "请求参数错误"),
    UNAUTHORIZED(4001, "未授权"),
    FORBIDDEN(4003, "禁止访问"),
    NOT_FOUND(4004, "资源不存在"),
    DATA_NOT_FOUND(4010, "找不到目标数据"),
    DATA_EXISTS(4011, "记录已存在"),
    OPERATION_FAILED(4020, "操作失败"),
    VALIDATION_ERROR(4022, "数据验证失败"),
    THE_ALARM_RULE_DOES_NOT_EXIST(4023, "告警规则不存在"),

    API_NOT_EXIST(4300, "API 不存在"),

    // 事件中心相关
    EVENT_NOT_FOUND(4400, "事件定义不存在"),
    EVENT_PUBLISH_FAILED(4401, "事件发布失败"),
    SUBSCRIPTION_NOT_FOUND(4402, "订阅不存在"),

    // 认证相关
    ACCOUNT_INCORRECT(4100, "用户名或密码错误"),
    ACCOUNT_DISABLED(4101, "账户已禁用"),
    ACCOUNT_LOCKED(4102, "账户已锁定"),
    PASSWORD_INCORRECT(4103, "密码错误"),
    TOKEN_INVALID(4110, "无效的Token"),
    TOKEN_EXPIRED(4111, "Token已过期"),
    CAPTCHA_INCORRECT(4120, "验证码错误"),

    OPERATION_NOT_ALLOWED(4200, "系统角色不能删除"),

    INTERNAL_ERROR(5000, "服务器内部错误"),
    SERVICE_UNAVAILABLE(5003, "服务不可用");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
