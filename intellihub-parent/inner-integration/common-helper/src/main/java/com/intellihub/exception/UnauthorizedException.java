package com.intellihub.exception;

import com.intellihub.constants.ResultCode;

/**
 * 未授权异常
 *
 * @author intellihub
 * @since 1.0.0
 */
public class UnauthorizedException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super(ResultCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ResultCode.UNAUTHORIZED, message);
    }
}
