package com.intellihub.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件订阅实体
 * 用于管理事件的订阅关系，包括订阅者信息、回调配置、重试策略等
 *
 * @author IntelliHub
 */
@Data
@TableName("event_subscription")
public class EventSubscription {

    /**
     * 订阅ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID，用于多租户隔离
     */
    private String tenantId;

    /**
     * 事件编码，订阅的事件类型
     */
    private String eventCode;

    /**
     * 订阅者类型
     * SERVICE - 服务订阅（内部服务）
     * WEBHOOK - Webhook回调（HTTP调用）
     * MQ - 消息队列（转发到MQ）
     */
    private String subscriberType;

    /**
     * 订阅者名称，用于标识订阅者
     */
    private String subscriberName;

    /**
     * 回调地址（Webhook类型使用）
     * 例如：http://example.com/webhook/event
     */
    private String callbackUrl;

    /**
     * 回调方法（Webhook类型使用）
     * POST 或 PUT
     */
    private String callbackMethod;

    /**
     * 回调请求头（JSON格式）
     * 用于自定义HTTP请求头
     */
    private String callbackHeaders;

    /**
     * MQ主题（MQ类型使用）
     */
    private String mqTopic;

    /**
     * MQ标签（MQ类型使用）
     */
    private String mqTag;

    /**
     * 过滤表达式（SpEL表达式）
     * 用于过滤不需要的事件
     */
    private String filterExpression;

    /**
     * 重试策略
     * NONE - 不重试
     * FIXED - 固定间隔重试
     * EXPONENTIAL - 指数退避重试
     */
    private String retryStrategy;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 订阅状态
     * ACTIVE - 激活
     * PAUSED - 暂停
     * INACTIVE - 未激活
     */
    private String status;

    /**
     * 优先级（数字越大优先级越高）
     * 用于控制多个订阅者的执行顺序
     */
    private Integer priority;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
