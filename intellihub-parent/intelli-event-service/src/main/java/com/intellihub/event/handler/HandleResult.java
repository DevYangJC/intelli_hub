package com.intellihub.event.handler;

import lombok.Builder;
import lombok.Data;

/**
 * 处理结果
 *
 * @author IntelliHub
 */
@Data
@Builder
public class HandleResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 是否需要重试（失败时）
     */
    private boolean needRetry;

    /**
     * HTTP 响应码（Webhook 类型）
     */
    private Integer responseCode;

    /**
     * 响应内容
     */
    private String responseBody;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 耗时（毫秒）
     */
    private int costTime;

    /**
     * 创建成功结果
     */
    public static HandleResult success(Integer responseCode, String responseBody, int costTime) {
        return HandleResult.builder()
                .success(true)
                .needRetry(false)
                .responseCode(responseCode)
                .responseBody(responseBody)
                .costTime(costTime)
                .build();
    }

    /**
     * 创建失败结果（需要重试）
     */
    public static HandleResult failWithRetry(String errorMessage, int costTime) {
        return HandleResult.builder()
                .success(false)
                .needRetry(true)
                .errorMessage(errorMessage)
                .costTime(costTime)
                .build();
    }

    /**
     * 创建失败结果（不重试）
     */
    public static HandleResult fail(String errorMessage, int costTime) {
        return HandleResult.builder()
                .success(false)
                .needRetry(false)
                .errorMessage(errorMessage)
                .costTime(costTime)
                .build();
    }
}
