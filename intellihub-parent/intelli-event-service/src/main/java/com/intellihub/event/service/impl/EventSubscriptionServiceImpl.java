package com.intellihub.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.intellihub.event.constant.EventStatus;
import com.intellihub.event.entity.EventSubscription;
import com.intellihub.event.mapper.EventSubscriptionMapper;
import com.intellihub.event.service.EventSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件订阅服务实现类
 * 实现事件订阅的完整管理功能，包括：
 * 1. 订阅的创建、更新、删除
 * 2. 订阅的暂停和恢复
 * 3. 订阅的查询（按事件、按ID）
 * 所有操作都会记录日志便于追踪
 *
 * @author IntelliHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventSubscriptionServiceImpl implements EventSubscriptionService {

    private final EventSubscriptionMapper subscriptionMapper;

    /**
     * 创建订阅
     * 设置订阅状态为 ACTIVE，并记录创建时间
     *
     * @param subscription 订阅信息
     * @return 订阅ID
     */
    @Override
    public String createSubscription(EventSubscription subscription) {
        subscription.setStatus(EventStatus.ACTIVE.getCode());
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionMapper.insert(subscription);
        log.info("创建事件订阅成功: id={}, eventCode={}", subscription.getId(), subscription.getEventCode());
        return subscription.getId();
    }

    /**
     * 更新订阅
     * 更新订阅的配置信息，并更新更新时间
     *
     * @param subscription 订阅信息
     * @return true-更新成功，false-更新失败
     */
    @Override
    public boolean updateSubscription(EventSubscription subscription) {
        subscription.setUpdatedAt(LocalDateTime.now());
        int rows = subscriptionMapper.updateById(subscription);
        log.info("更新事件订阅: id={}, rows={}", subscription.getId(), rows);
        return rows > 0;
    }

    /**
     * 删除订阅
     * 物理删除订阅记录
     *
     * @param subscriptionId 订阅ID
     * @return true-删除成功，false-删除失败
     */
    @Override
    public boolean deleteSubscription(String subscriptionId) {
        int rows = subscriptionMapper.deleteById(subscriptionId);
        log.info("删除事件订阅: id={}, rows={}", subscriptionId, rows);
        return rows > 0;
    }

    /**
     * 暂停订阅
     * 将订阅状态设置为 PAUSED
     *
     * @param subscriptionId 订阅ID
     * @return true-暂停成功，false-暂停失败
     */
    @Override
    public boolean pauseSubscription(String subscriptionId) {
        LambdaUpdateWrapper<EventSubscription> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(EventSubscription::getId, subscriptionId)
                .set(EventSubscription::getStatus, EventStatus.PAUSED.getCode())
                .set(EventSubscription::getUpdatedAt, LocalDateTime.now());
        int rows = subscriptionMapper.update(null, updateWrapper);
        log.info("暂停事件订阅: id={}, rows={}", subscriptionId, rows);
        return rows > 0;
    }

    /**
     * 恢复订阅
     * 将订阅状态设置为 ACTIVE
     *
     * @param subscriptionId 订阅ID
     * @return true-恢复成功，false-恢复失败
     */
    @Override
    public boolean resumeSubscription(String subscriptionId) {
        LambdaUpdateWrapper<EventSubscription> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(EventSubscription::getId, subscriptionId)
                .set(EventSubscription::getStatus, EventStatus.ACTIVE.getCode())
                .set(EventSubscription::getUpdatedAt, LocalDateTime.now());
        int rows = subscriptionMapper.update(null, updateWrapper);
        log.info("恢复事件订阅: id={}, rows={}", subscriptionId, rows);
        return rows > 0;
    }

    /**
     * 获取事件的所有激活订阅
     * 查询结果按优先级降序、创建时间升序排列
     *
     * @param tenantId  租户ID
     * @param eventCode 事件编码
     * @return 订阅列表
     */
    @Override
    public List<EventSubscription> getSubscriptionsByEvent(String tenantId, String eventCode) {
        LambdaQueryWrapper<EventSubscription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EventSubscription::getTenantId, tenantId)
                .eq(EventSubscription::getEventCode, eventCode)
                .eq(EventSubscription::getStatus, EventStatus.ACTIVE.getCode())
                .orderByDesc(EventSubscription::getPriority)
                .orderByAsc(EventSubscription::getCreatedAt);
        return subscriptionMapper.selectList(queryWrapper);
    }

    /**
     * 获取订阅详情
     *
     * @param subscriptionId 订阅ID
     * @return 订阅信息
     */
    @Override
    public EventSubscription getSubscription(String subscriptionId) {
        return subscriptionMapper.selectById(subscriptionId);
    }
}
