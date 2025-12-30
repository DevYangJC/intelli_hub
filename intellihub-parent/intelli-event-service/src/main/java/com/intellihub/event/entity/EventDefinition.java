package com.intellihub.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件定义实体
 * 用于定义系统中的事件类型，包括事件的基本信息、数据结构定义等
 *
 * @author IntelliHub
 */
@Data
@TableName("event_definition")
public class EventDefinition {

    /**
     * 事件定义ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID，用于多租户隔离
     */
    private String tenantId;

    /**
     * 事件编码，唯一标识一个事件类型
     * 例如：user.created, order.paid
     */
    private String eventCode;

    /**
     * 事件名称，便于理解的事件描述
     */
    private String eventName;

    /**
     * 事件类型
     * SYSTEM - 系统事件
     * BUSINESS - 业务事件
     * CUSTOM - 自定义事件
     */
    private String eventType;

    /**
     * 事件描述，详细说明事件的用途和触发场景
     */
    private String description;

    /**
     * 事件数据结构定义（JSON Schema格式）
     * 用于验证事件数据的格式是否符合要求
     */
    private String schemaDefinition;

    /**
     * 事件状态
     * ACTIVE - 激活
     * INACTIVE - 未激活
     */
    private String status;

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
