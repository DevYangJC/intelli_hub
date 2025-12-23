package com.intellihub.exception;

/**
 * 未授权异常
 *
 * @author intellihub
 * @since 1.0.0
 */
public class UnauthorizedException extends RuntimeException {

    private final Integer code;

    public UnauthorizedException(String message) {
        super(message);
        this.code = 401;
    }

    public UnauthorizedException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.code = 401;
    }

    public Integer getCode() {
        return code;
    }
}
