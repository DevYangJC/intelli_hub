package com.intellihub.event.handler;

import com.intellihub.event.entity.EventConsumeRecord;
import com.intellihub.event.entity.EventSubscription;
import com.intellihub.event.model.EventMessage;

/**
 * 订阅处理器接口 - 策略模式
 * 定义处理不同类型订阅的统一接口
 *
 * @author IntelliHub
 */
public interface SubscriptionHandler {

    /**
     * 获取处理器支持的订阅类型
     */
    String getType();

    /**
     * 处理事件消息
     *
     * @param eventMessage 事件消息
     * @param subscription 订阅配置
     * @return 处理结果
     */
    HandleResult handle(EventMessage eventMessage, EventSubscription subscription);

    /**
     * 重试处理
     *
     * @param record       消费记录（包含事件数据）
     * @param subscription 订阅配置
     * @return 处理结果
     */
    HandleResult retry(EventConsumeRecord record, EventSubscription subscription);
}
