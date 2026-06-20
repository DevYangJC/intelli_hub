package com.intellihub.constants;

/**
 * 响应状态常量类
 * <p>
 * 提供与ResultCode兼容的静态常量访问方式
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public final class ResponseStatus {


    private ResponseStatus() {
    }

    public static final ResultCode SUCCESS = ResultCode.SUCCESS;
    public static final ResultCode CREATED = ResultCode.CREATED;
    public static final ResultCode BAD_REQUEST = ResultCode.BAD_REQUEST;
    public static final ResultCode UNAUTHORIZED = ResultCode.UNAUTHORIZED;
    public static final ResultCode FORBIDDEN = ResultCode.FORBIDDEN;
    public static final ResultCode NOT_FOUND = ResultCode.NOT_FOUND;
    public static final ResultCode DATA_NOT_FOUND = ResultCode.DATA_NOT_FOUND;
    public static final ResultCode DATA_EXISTS = ResultCode.DATA_EXISTS;
    public static final ResultCode OPERATION_FAILED = ResultCode.OPERATION_FAILED;
    public static final ResultCode VALIDATION_ERROR = ResultCode.VALIDATION_ERROR;
    public static final ResultCode INTERNAL_ERROR = ResultCode.INTERNAL_ERROR;
    public static final ResultCode SERVICE_UNAVAILABLE = ResultCode.SERVICE_UNAVAILABLE;


    public static final ResultCode OPERATION_NOT_ALLOWED = ResultCode.OPERATION_NOT_ALLOWED;

    // 认证相关
    public static final ResultCode ACCOUNT_INCORRECT = ResultCode.ACCOUNT_INCORRECT;
    public static final ResultCode ACCOUNT_DISABLED = ResultCode.ACCOUNT_DISABLED;
    public static final ResultCode ACCOUNT_LOCKED = ResultCode.ACCOUNT_LOCKED;
    public static final ResultCode PASSWORD_INCORRECT = ResultCode.PASSWORD_INCORRECT;
    public static final ResultCode TOKEN_INVALID = ResultCode.TOKEN_INVALID;
    public static final ResultCode TOKEN_EXPIRED = ResultCode.TOKEN_EXPIRED;
    public static final ResultCode CAPTCHA_INCORRECT = ResultCode.CAPTCHA_INCORRECT;
}
