package com.intellihub.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件消费记录实体
 * 用于记录每次事件消费的详细信息，包括消费状态、重试次数、响应结果等
 * 支持消费失败后的重试机制和问题排查
 *
 * @author IntelliHub
 */
@Data
@TableName("event_consume_record")
public class EventConsumeRecord {

    /**
     * 记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID，用于多租户隔离
     */
    private String tenantId;

    /**
     * 订阅ID，关联到具体的订阅配置
     */
    private String subscriptionId;

    /**
     * 事件唯一ID，用于关联发布记录
     */
    private String eventId;

    /**
     * 事件编码，标识事件类型
     */
    private String eventCode;

    /**
     * 事件数据（JSON格式）
     * 存储消费时的事件数据快照
     */
    private String eventData;

    /**
     * 消费时间，事件被消费的时间
     */
    private LocalDateTime consumeTime;

    /**
     * 消费状态
     * PENDING - 待处理
     * SUCCESS - 成功
     * FAILED - 失败
     * RETRYING - 重试中
     */
    private String status;

    /**
     * 重试次数，记录已经重试的次数
     */
    private Integer retryTimes;

    /**
     * 下次重试时间，用于定时任务扫描并重试
     */
    private LocalDateTime nextRetryTime;

    /**
     * 响应状态码（Webhook类型）
     * HTTP响应的状态码，如200、500等
     */
    private Integer responseCode;

    /**
     * 响应内容（Webhook类型）
     * HTTP响应的Body内容
     */
    private String responseBody;

    /**
     * 错误信息，消费失败时记录具体的错误原因
     */
    private String errorMessage;

    /**
     * 耗时（毫秒）
     * 记录消费处理的耗时，用于性能分析
     */
    private Integer costTime;

    /**
     * 创建时间，记录入库时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间，记录最后更新时间（重试时会更新）
     */
    private LocalDateTime updatedAt;
}
