package com.intellihub.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;



/**
 * 响应状态码枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ResponseStatus {

    // ==================== 成功状态 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 客户端错误 (4xxx) ====================
    BAD_REQUEST(4000, "参数错误"),
    UNAUTHORIZED(4001, "未授权访问"),
    FORBIDDEN(4003, "禁止访问"),
    NOT_FOUND(4004, "资源不存在"),
    DATA_NOT_FOUND(4010, "找不到目标数据"),
    DATA_EXISTS(4011, "记录已存在"),
    DATA_DUPLICATE(4012, "数据重复"),
    OPERATION_FAILED(4013, "已发布的API不能删除，请先下线"),

    // 认证相关 (41xx)
    ACCOUNT_INCORRECT(4100, "账号或密码不正确"),
    PASSWORD_INCORRECT(4101, "密码不正确"),
    CAPTCHA_INCORRECT(4102, "验证码不正确或已过期"),
    SMS_CODE_INCORRECT(4103, "短信验证码不正确或已过期"),
    EMAIL_CODE_INCORRECT(4104, "邮箱验证码不正确或已过期"),
    TOKEN_INVALID(4105, "Token无效或已过期"),
    TOKEN_EXPIRED(4106, "Token已过期"),

    // 账号状态 (42xx)
    ACCOUNT_LOCKED(4200, "账号已被锁定"),
    ACCOUNT_DISABLED(4201, "账号已被禁用"),
    ACCOUNT_EXPIRED(4202, "账号已过期"),
    PASSWORD_EXPIRED(4203, "密码已过期，请修改密码"),
    TOO_MANY_LOGIN_FAILURES(4204, "登录失败次数过多，请稍后再试"),

    // ==================== 服务端错误 (5xxx) ====================
    SERVER_ERROR(5000, "系统繁忙，请稍后重试"),
    SERVICE_UNAVAILABLE(5001, "服务不可用"),
    DATABASE_ERROR(5002, "数据库操作失败"),

    // 文件操作 (51xx)
    FILE_UPLOAD_ERROR(5100, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(5101, "文件下载失败"),
    FILE_NOT_FOUND(5102, "文件不存在"),
    FILE_DELETE_ERROR(5103, "文件删除失败"),
    EXPORT_ERROR(5110, "导出失败"),
    IMPORT_ERROR(5111, "导入失败"),

    // 限流相关 (52xx)
    REPEAT_REQUEST(5200, "请勿重复提交"),
    RATE_LIMIT_EXCEEDED(5201, "请求过于频繁"),
    OPERATION_NOT_ALLOWED(5202, "不允许的操作");

    private final int code;
    private final String message;
}
