package com.intellihub.common;


import com.intellihub.constants.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应格式
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "成功", null);
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "成功", data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 失败响应（默认错误码）
     */
    public static <T> ApiResponse<T> failed(String message) {
        return new ApiResponse<>(ResponseStatus.SERVER_ERROR.getCode(), message, null);
    }

    /**
     * 失败响应（自定义错误码）
     */
    public static <T> ApiResponse<T> failed(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 失败响应（使用ResponseStatus枚举）
     */
    public static <T> ApiResponse<T> failed(ResponseStatus status) {
        return new ApiResponse<>(status.getCode(), status.getMessage(), null);
    }

    /**
     * 失败响应（使用ResponseStatus枚举，带数据）
     */
    public static <T> ApiResponse<T> failed(ResponseStatus status, T data) {
        return new ApiResponse<>(status.getCode(), status.getMessage(), data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}