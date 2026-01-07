package com.intellihub.event.handler.impl;

import com.intellihub.event.constant.SubscriberType;
import com.intellihub.event.entity.EventConsumeRecord;
import com.intellihub.event.entity.EventSubscription;
import com.intellihub.event.handler.HandleResult;
import com.intellihub.event.handler.SubscriptionHandler;
import com.intellihub.event.model.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内部服务订阅处理器（预留）
 * 通过 Dubbo/Feign 调用内部服务
 *
 * @author IntelliHub
 */
@Slf4j
@Component
public class ServiceHandler implements SubscriptionHandler {

    @Override
    public String getType() {
        return SubscriberType.SERVICE.getCode();
    }

    @Override
    public HandleResult handle(EventMessage eventMessage, EventSubscription subscription) {
        // TODO: 实现内部服务调用，通过 Dubbo 或 Feign
        log.info("内部服务调用: subscriberName={}, eventCode={}",
                subscription.getSubscriberName(), eventMessage.getEventCode());
        return HandleResult.success(null, "Service handler not implemented yet", 0);
    }

    @Override
    public HandleResult retry(EventConsumeRecord record, EventSubscription subscription) {
        // TODO: 实现重试逻辑
        log.info("内部服务重试: subscriberName={}, eventCode={}",
                subscription.getSubscriberName(), record.getEventCode());
        return HandleResult.success(null, "Service handler not implemented yet", 0);
    }
}
