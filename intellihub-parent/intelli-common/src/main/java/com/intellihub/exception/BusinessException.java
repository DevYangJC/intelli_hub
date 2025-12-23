package com.intellihub.exception;


import com.intellihub.constants.CommonConstants;
import com.intellihub.constants.ResponseStatus;
import lombok.Data;



/**
 * 业务异常对象
 * @author Cordr.Mr
 * @date 2022/02/15 20:09
 */
@Data
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = CommonConstants.UNIVERSAL_ERROR_CODE;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(Integer code, String message, Throwable e) {
        super(message, e);
        this.code = code;
    }

    public BusinessException(String message, Throwable e) {
        super(message, e);
        this.code = CommonConstants.UNIVERSAL_ERROR_CODE;
    }

    public BusinessException(ResponseStatus status) {
        super(status.getMessage());
        this.code = status.getCode();
    }

    public BusinessException(ResponseStatus status, Throwable e) {
        super(status.getMessage(), e);
        this.code = status.getCode();
    }
}
