package com.intellihub.sdk.exception;

/**
 * IntelliHub SDK 异常类
 *
 * @author intellihub
 * @since 1.0.0
 */
public class IntelliHubException extends RuntimeException {

    private final int code;
    private final String errorMessage;

    public IntelliHubException(String message) {
        super(message);
        this.code = -1;
        this.errorMessage = message;
    }

    public IntelliHubException(int code, String message) {
        super(message);
        this.code = code;
        this.errorMessage = message;
    }

    public IntelliHubException(String message, Throwable cause) {
        super(message, cause);
        this.code = -1;
        this.errorMessage = message;
    }

    public int getCode() {
        return code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
