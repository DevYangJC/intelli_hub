package com.intellihub.event.service;

import com.intellihub.event.entity.EventSubscription;

import java.util.List;

/**
 * 事件订阅服务接口
 * 提供事件订阅的完整管理功能，包括订阅的创建、更新、删除、暂停、恢复等
 * 支持多种订阅类型：Webhook、MQ、内部服务
 *
 * @author IntelliHub
 */
public interface EventSubscriptionService {

    /**
     * 创建订阅
     * 为指定事件创建一个新的订阅，订阅创建后会自动激活
     *
     * @param subscription 订阅信息，包含订阅者类型、回调配置、重试策略等
     * @return 订阅ID
     */
    String createSubscription(EventSubscription subscription);

    /**
     * 更新订阅
     * 更新订阅的配置信息，如回调地址、重试策略等
     *
     * @param subscription 订阅信息，必须包含订阅ID
     * @return true-更新成功，false-更新失败
     */
    boolean updateSubscription(EventSubscription subscription);

    /**
     * 删除订阅
     * 物理删除订阅记录，删除后该订阅将不再接收事件
     *
     * @param subscriptionId 订阅ID
     * @return true-删除成功，false-删除失败
     */
    boolean deleteSubscription(String subscriptionId);

    /**
     * 暂停订阅
     * 将订阅状态设置为 PAUSED，暂停后该订阅将不再接收事件
     * 可以通过恢复订阅重新激活
     *
     * @param subscriptionId 订阅ID
     * @return true-暂停成功，false-暂停失败
     */
    boolean pauseSubscription(String subscriptionId);

    /**
     * 恢复订阅
     * 将订阅状态设置为 ACTIVE，恢复后该订阅将重新接收事件
     *
     * @param subscriptionId 订阅ID
     * @return true-恢复成功，false-恢复失败
     */
    boolean resumeSubscription(String subscriptionId);

    /**
     * 获取事件的所有激活订阅
     * 查询指定租户和事件的所有激活状态的订阅
     * 结果按优先级降序、创建时间升序排列
     *
     * @param tenantId  租户ID
     * @param eventCode 事件编码
     * @return 订阅列表，按优先级和创建时间排序
     */
    List<EventSubscription> getSubscriptionsByEvent(String tenantId, String eventCode);

    /**
     * 获取订阅详情
     * 根据订阅ID查询订阅的完整信息
     *
     * @param subscriptionId 订阅ID
     * @return 订阅信息，不存在时返回 null
     */
    EventSubscription getSubscription(String subscriptionId);
}
