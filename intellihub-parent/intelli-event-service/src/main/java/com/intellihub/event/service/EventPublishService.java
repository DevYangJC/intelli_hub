package com.intellihub.event.service;

import com.intellihub.event.model.EventMessage;

/**
 * 事件发布服务接口
 * 提供事件发布的核心功能，支持同步和异步两种发布方式
 * 负责将事件发布到消息队列（Kafka），并记录发布日志
 *
 * @author IntelliHub
 */
public interface EventPublishService {

    /**
     * 同步发布事件
     * 将事件发布到消息队列，等待发布完成后返回结果
     * 发布前会校验事件定义是否存在且已激活
     * 发布后会记录发布日志到数据库
     *
     * @param eventMessage 事件消息，包含事件的完整信息
     * @return true-发布成功，false-发布失败
     */
    boolean publish(EventMessage eventMessage);

    /**
     * 异步发布事件
     * 使用 @Async 注解实现异步发布，不阻塞调用线程
     * 适用于对发布结果不敏感的场景
     *
     * @param eventMessage 事件消息，包含事件的完整信息
     */
    void publishAsync(EventMessage eventMessage);
}
