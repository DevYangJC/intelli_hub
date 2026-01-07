package com.intellihub.event.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订阅处理器工厂
 * 根据订阅类型获取对应的处理器
 *
 * @author IntelliHub
 */
@Slf4j
@Component
public class SubscriptionHandlerFactory {

    private final List<SubscriptionHandler> handlers;
    private final Map<String, SubscriptionHandler> handlerMap = new HashMap<>();

    public SubscriptionHandlerFactory(List<SubscriptionHandler> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    public void init() {
        for (SubscriptionHandler handler : handlers) {
            handlerMap.put(handler.getType(), handler);
            log.info("注册订阅处理器: type={}, handler={}", handler.getType(), handler.getClass().getSimpleName());
        }
    }

    /**
     * 获取订阅处理器
     *
     * @param type 订阅类型
     * @return 处理器，如果类型不支持则返回 null
     */
    public SubscriptionHandler getHandler(String type) {
        return handlerMap.get(type);
    }

    /**
     * 检查是否支持该订阅类型
     */
    public boolean supports(String type) {
        return handlerMap.containsKey(type);
    }
}
