package com.intellihub.exception;

import com.intellihub.constants.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode status) {
        super(status.getMessage());
        this.code = status.getCode();
    }

    public BusinessException(ResultCode status, String message) {
        super(message);
        this.code = status.getCode();
    }
}
