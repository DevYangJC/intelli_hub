package com.intellihub.event.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 事件消息模型
 * 用于封装事件的完整信息，包括事件ID、租户、事件编码、数据等
 * 这是事件在系统中流转的标准格式
 *
 * @author IntelliHub
 */
@Data
public class EventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一ID，自动生成的UUID（去除横线）
     * 用于追踪事件的完整生命周期
     */
    private String eventId;

    /**
     * 租户ID，标识事件所属的租户
     * 用于多租户隔离
     */
    private String tenantId;

    /**
     * 事件编码，标识事件类型
     * 例如：user.created, order.paid
     */
    private String eventCode;

    /**
     * 事件源，标识事件的发布来源
     * 例如：user-service, order-service
     */
    private String source;

    /**
     * 事件数据，存储事件的业务数据
     * 使用Map结构，支持灵活的数据格式
     */
    private Map<String, Object> data;

    /**
     * 事件时间戳，记录事件发生的时间
     * 在构造函数中自动设置为当前时间
     */
    private LocalDateTime timestamp;

    /**
     * 默认构造函数
     * 自动生成事件ID和时间戳
     */
    public EventMessage() {
        this.eventId = UUID.randomUUID().toString().replace("-", "");
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数
     *
     * @param tenantId  租户ID
     * @param eventCode 事件编码
     * @param data      事件数据
     */
    public EventMessage(String tenantId, String eventCode, Map<String, Object> data) {
        this();
        this.tenantId = tenantId;
        this.eventCode = eventCode;
        this.data = data;
    }

    /**
     * 构造函数（带事件源）
     *
     * @param tenantId  租户ID
     * @param eventCode 事件编码
     * @param source    事件源
     * @param data      事件数据
     */
    public EventMessage(String tenantId, String eventCode, String source, Map<String, Object> data) {
        this(tenantId, eventCode, data);
        this.source = source;
    }
}
