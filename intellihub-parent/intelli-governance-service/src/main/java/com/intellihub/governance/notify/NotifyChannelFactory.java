package com.intellihub.governance.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 通知渠道工厂
 * 自动收集所有 NotifyChannel 实现类，根据渠道类型获取对应实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class NotifyChannelFactory {

    private final List<NotifyChannel> channels;
    private final Map<String, NotifyChannel> channelMap = new HashMap<>();

    public NotifyChannelFactory(List<NotifyChannel> channels) {
        this.channels = channels;
    }

    @PostConstruct
    public void init() {
        for (NotifyChannel channel : channels) {
            String type = channel.getChannelType().toLowerCase();
            channelMap.put(type, channel);
            log.info("注册通知渠道: {}", type);
        }
        log.info("共注册 {} 个通知渠道", channelMap.size());
    }

    /**
     * 根据渠道类型获取通知渠道
     * @param channelType 渠道类型（如 email, webhook, dingtalk, kafka）
     * @return 通知渠道实现，不存在则返回 null
     */
    public NotifyChannel getChannel(String channelType) {
        if (channelType == null) {
            return null;
        }
        return channelMap.get(channelType.toLowerCase().trim());
    }

    /**
     * 检查渠道是否存在
     */
    public boolean hasChannel(String channelType) {
        return getChannel(channelType) != null;
    }

    /**
     * 获取所有支持的渠道类型
     */
    public List<String> getSupportedChannelTypes() {
        return Collections.unmodifiableList(new ArrayList<>(channelMap.keySet()));
    }
}
