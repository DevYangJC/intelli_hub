package com.intellihub.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件发布记录实体
 * 用于记录每次事件发布的详细信息，包括事件数据、发布状态、错误信息等
 * 便于追踪事件的发布历史和排查问题
 *
 * @author IntelliHub
 */
@Data
@TableName("event_publish_record")
public class EventPublishRecord {

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
     * 事件编码，标识事件类型
     */
    private String eventCode;

    /**
     * 事件唯一ID，用于追踪单个事件的完整生命周期
     * 格式：UUID（去除横线）
     */
    private String eventId;

    /**
     * 事件数据（JSON格式）
     * 存储事件的完整数据内容
     */
    private String eventData;

    /**
     * 事件源，标识事件的发布来源
     * 例如：user-service, order-service
     */
    private String source;

    /**
     * 发布时间，事件实际发布到消息队列的时间
     */
    private LocalDateTime publishTime;

    /**
     * 发布状态
     * PUBLISHED - 发布成功
     * FAILED - 发布失败
     */
    private String status;

    /**
     * 错误信息，发布失败时记录具体的错误原因
     */
    private String errorMessage;

    /**
     * 创建时间，记录入库时间
     */
    private LocalDateTime createdAt;
}
