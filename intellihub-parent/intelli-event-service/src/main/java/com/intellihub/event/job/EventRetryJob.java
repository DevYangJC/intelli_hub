package com.intellihub.event.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.intellihub.context.UserContextHolder;
import com.intellihub.event.constant.ConsumeStatus;
import com.intellihub.event.entity.EventConsumeRecord;
import com.intellihub.event.entity.EventSubscription;
import com.intellihub.event.handler.HandleResult;
import com.intellihub.event.handler.SubscriptionHandler;
import com.intellihub.event.handler.SubscriptionHandlerFactory;
import com.intellihub.event.mapper.EventConsumeRecordMapper;
import com.intellihub.event.mapper.EventSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件消费失败重试定时任务
 * <p>
 * 功能说明：
 * 1. 定时扫描状态为 RETRYING 且重试时间已到的消费记录
 * 2. 使用策略模式根据订阅类型调用对应的处理器进行重试
 * 3. 支持 FIXED（固定间隔）和 EXPONENTIAL（指数退避）两种重试策略
 * </p>
 * <p>
 * 多租户处理：
 * 定时任务没有用户上下文，无法从 UserContextHolder 获取租户ID，
 * 因此需要使用 UserContextHolder.setIgnoreTenant(true) 临时豁免租户隔离，
 * 以便扫描所有租户的重试记录。
 * </p>
 * <p>
 * 原理说明：
 * IntelliHubTenantLineHandler.ignoreTable() 方法会检查 UserContextHolder.isIgnoreTenant()，
 * 如果为 true，则跳过租户条件拼接，允许查询所有租户的数据。
 * </p>
 *
 * @author IntelliHub
 * @see com.intellihub.mybatis.handler.IntelliHubTenantLineHandler
 * @see com.intellihub.context.UserContextHolder
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRetryJob {

    private final EventConsumeRecordMapper consumeRecordMapper;
    private final EventSubscriptionMapper subscriptionMapper;
    private final SubscriptionHandlerFactory handlerFactory;

    /**
     * 每分钟扫描一次需要重试的消费记录
     * <p>
     * 执行流程：
     * 1. 设置 ignoreTenant = true，豁免多租户隔离
     * 2. 查询所有租户中状态为 RETRYING 且重试时间已到的记录
     * 3. 逐条执行重试逻辑
     * 4. 无论成功失败，最终恢复 ignoreTenant = false
     * </p>
     */
    @Scheduled(fixedRate = 60000)
    public void retryFailedEvents() {
        try {
            // ========== 豁免多租户隔离 ==========
            // 定时任务没有用户上下文，UserContextHolder.getCurrentTenantId() 返回 null，
            // 多租户拦截器会拼接 tenant_id = 'UNKNOWN'，导致查询不到数据。
            // 设置 ignoreTenant = true 后，IntelliHubTenantLineHandler.ignoreTable() 返回 true，
            // 拦截器跳过租户条件，允许查询所有租户的数据。
            UserContextHolder.setIgnoreTenant(true);
            log.debug("事件重试任务开始执行，已豁免多租户隔离");
            
            LocalDateTime now = LocalDateTime.now();
            
            // 查询需要重试的记录：状态为 RETRYING 且重试时间已到
            LambdaQueryWrapper<EventConsumeRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EventConsumeRecord::getStatus, ConsumeStatus.RETRYING.getCode())
                    .le(EventConsumeRecord::getNextRetryTime, now)
                    .last("LIMIT 100");  // 每次最多处理100条，防止单次执行时间过长
            
            List<EventConsumeRecord> records = consumeRecordMapper.selectList(queryWrapper);
            
            if (records.isEmpty()) {
                log.debug("没有需要重试的消费记录");
                return;
            }
            
            log.info("发现 {} 条需要重试的消费记录", records.size());
            
            // 逐条执行重试
            for (EventConsumeRecord record : records) {
                retryRecord(record);
            }
        } catch (Exception e) {
            log.error("事件重试任务执行失败", e);
        } finally {
            // ========== 恢复多租户隔离 ==========
            // 无论任务成功还是失败，都需要恢复租户隔离，
            // 避免影响同一线程后续的其他操作。
            UserContextHolder.setIgnoreTenant(false);
            log.debug("事件重试任务执行完毕，已恢复多租户隔离");
        }
    }

    /**
     * 重试单条消费记录
     * <p>
     * 执行流程：
     * 1. 查询订阅配置，获取重试策略和最大重试次数
     * 2. 检查是否超过最大重试次数
     * 3. 使用策略模式获取对应的处理器（Webhook/MQ/Service）
     * 4. 执行重试并应用结果
     * </p>
     *
     * @param record 需要重试的消费记录
     */
    private void retryRecord(EventConsumeRecord record) {
        try {
            // 查询订阅配置（此时 ignoreTenant = true，可以跨租户查询）
            EventSubscription subscription = subscriptionMapper.selectById(record.getSubscriptionId());
            if (subscription == null) {
                log.warn("订阅配置不存在，跳过重试: subscriptionId={}", record.getSubscriptionId());
                markAsFailed(record, "订阅配置不存在");
                return;
            }
            
            // 检查是否超过最大重试次数
            if (record.getRetryTimes() >= subscription.getMaxRetryTimes()) {
                log.warn("超过最大重试次数，标记为失败: recordId={}, retryTimes={}, maxRetryTimes={}", 
                        record.getId(), record.getRetryTimes(), subscription.getMaxRetryTimes());
                markAsFailed(record, "超过最大重试次数(" + subscription.getMaxRetryTimes() + ")");
                return;
            }
            
            // 增加重试次数
            record.setRetryTimes(record.getRetryTimes() + 1);
            
            // 使用策略模式获取处理器
            // 根据 subscriberType（WEBHOOK/MQ/SERVICE）获取对应的处理器实现
            SubscriptionHandler handler = handlerFactory.getHandler(subscription.getSubscriberType());
            if (handler == null) {
                log.warn("不支持的订阅者类型: {}", subscription.getSubscriberType());
                markAsFailed(record, "不支持的订阅者类型: " + subscription.getSubscriberType());
                return;
            }
            
            // 执行重试，handler.retry() 会返回处理结果
            HandleResult result = handler.retry(record, subscription);
            applyRetryResult(record, result, subscription);
            
        } catch (Exception e) {
            log.error("重试消费记录失败: recordId={}", record.getId(), e);
            handleRetryFailure(record, e.getMessage());
        }
    }

    /**
     * 应用重试结果
     * <p>
     * 根据处理结果更新消费记录的状态：
     * - 成功：标记为 SUCCESS
     * - 失败但需要重试：设置下次重试时间，状态保持 RETRYING
     * - 失败不再重试：标记为 FAILED
     * </p>
     *
     * @param record       消费记录
     * @param result       处理结果
     * @param subscription 订阅配置
     */
    private void applyRetryResult(EventConsumeRecord record, HandleResult result, EventSubscription subscription) {
        if (result.isSuccess()) {
            // 重试成功
            markAsSuccess(record, result.getResponseCode(), result.getResponseBody(), result.getCostTime());
            log.info("重试成功: recordId={}, retryTimes={}, costTime={}ms", 
                    record.getId(), record.getRetryTimes(), result.getCostTime());
        } else {
            // 重试失败，判断是否继续重试
            if (result.isNeedRetry() && record.getRetryTimes() < subscription.getMaxRetryTimes()) {
                // 还可以重试，设置下次重试时间
                handleRetryFailure(record, result.getErrorMessage());
            } else {
                // 不再重试，标记为失败
                markAsFailed(record, result.getErrorMessage());
            }
        }
    }

    /**
     * 标记消费记录为成功
     *
     * @param record       消费记录
     * @param responseCode HTTP 响应码
     * @param responseBody 响应内容
     * @param costTime     耗时（毫秒）
     */
    private void markAsSuccess(EventConsumeRecord record, Integer responseCode, 
                                String responseBody, int costTime) {
        LambdaUpdateWrapper<EventConsumeRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(EventConsumeRecord::getId, record.getId())
                .set(EventConsumeRecord::getStatus, ConsumeStatus.SUCCESS.getCode())
                .set(EventConsumeRecord::getRetryTimes, record.getRetryTimes())
                .set(EventConsumeRecord::getResponseCode, responseCode)
                .set(EventConsumeRecord::getResponseBody, responseBody)
                .set(EventConsumeRecord::getCostTime, costTime)
                .set(EventConsumeRecord::getNextRetryTime, null)  // 清除下次重试时间
                .set(EventConsumeRecord::getUpdatedAt, LocalDateTime.now());
        consumeRecordMapper.update(null, updateWrapper);
    }

    /**
     * 标记消费记录为失败（不再重试）
     *
     * @param record       消费记录
     * @param errorMessage 错误信息
     */
    private void markAsFailed(EventConsumeRecord record, String errorMessage) {
        LambdaUpdateWrapper<EventConsumeRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(EventConsumeRecord::getId, record.getId())
                .set(EventConsumeRecord::getStatus, ConsumeStatus.FAILED.getCode())
                .set(EventConsumeRecord::getErrorMessage, errorMessage)
                .set(EventConsumeRecord::getNextRetryTime, null)  // 清除下次重试时间
                .set(EventConsumeRecord::getUpdatedAt, LocalDateTime.now());
        consumeRecordMapper.update(null, updateWrapper);
    }

    /**
     * 处理重试失败，计算并设置下次重试时间
     * <p>
     * 重试策略说明：
     * - FIXED：固定间隔 60 秒
     * - EXPONENTIAL：指数退避，2^n * 60 秒（n 为当前重试次数）
     *   第1次重试：2^1 * 60 = 120秒后
     *   第2次重试：2^2 * 60 = 240秒后
     *   第3次重试：2^3 * 60 = 480秒后
     * </p>
     *
     * @param record       消费记录
     * @param errorMessage 错误信息
     */
    private void handleRetryFailure(EventConsumeRecord record, String errorMessage) {
        // 查询订阅配置获取重试策略
        EventSubscription subscription = subscriptionMapper.selectById(record.getSubscriptionId());
        
        if (subscription == null || record.getRetryTimes() >= subscription.getMaxRetryTimes()) {
            markAsFailed(record, errorMessage);
            return;
        }
        
        // 根据重试策略计算下次重试时间
        LocalDateTime nextRetryTime = calculateNextRetryTime(subscription.getRetryStrategy(), record.getRetryTimes());
        
        LambdaUpdateWrapper<EventConsumeRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(EventConsumeRecord::getId, record.getId())
                .set(EventConsumeRecord::getRetryTimes, record.getRetryTimes())
                .set(EventConsumeRecord::getErrorMessage, errorMessage)
                .set(EventConsumeRecord::getNextRetryTime, nextRetryTime)
                .set(EventConsumeRecord::getUpdatedAt, LocalDateTime.now());
        consumeRecordMapper.update(null, updateWrapper);
        
        log.info("设置下次重试时间: recordId={}, retryTimes={}, nextRetryTime={}", 
                record.getId(), record.getRetryTimes(), nextRetryTime);
    }

    /**
     * 计算下次重试时间
     *
     * @param retryStrategy 重试策略：FIXED（固定间隔）或 EXPONENTIAL（指数退避）
     * @param retryTimes    当前重试次数
     * @return 下次重试时间
     */
    private LocalDateTime calculateNextRetryTime(String retryStrategy, int retryTimes) {
        int delaySeconds;
        if ("EXPONENTIAL".equals(retryStrategy)) {
            // 指数退避：2^n * 60 秒
            delaySeconds = (int) Math.pow(2, retryTimes) * 60;
        } else {
            // 默认固定间隔：60 秒
            delaySeconds = 60;
        }
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
