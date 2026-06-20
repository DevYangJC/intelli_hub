package com.intellihub;

import com.intellihub.constants.ResultCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> ApiResponse<T> error(ResultCode status) {
        return new ApiResponse<>(status.getCode(), status.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(ResultCode status, String message) {
        return new ApiResponse<>(status.getCode(), message, null);
    }

    /**
     * 失败响应 (兼容网关使用)
     */
    public static <T> ApiResponse<T> failed(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> failed(ResultCode status) {
        return new ApiResponse<>(status.getCode(), status.getMessage(), null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> failed(ResultCode status, String message) {
        return new ApiResponse<>(status.getCode(), message, null);
    }

    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
