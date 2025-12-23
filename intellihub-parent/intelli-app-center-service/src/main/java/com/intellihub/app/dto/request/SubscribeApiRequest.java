package com.intellihub.app.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 订阅API请求DTO
 * <p>
 * 用于接收应用订阅API时的请求参数
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class SubscribeApiRequest {

    /**
     * API ID，必填
     */
    @NotBlank(message = "API ID不能为空")
    private String apiId;

    /**
     * 单独的调用限额，null表示使用应用配额
     */
    private Long quotaLimit;

    /**
     * 订阅过期时间，null表示永不过期
     */
    private LocalDateTime expireTime;
}
