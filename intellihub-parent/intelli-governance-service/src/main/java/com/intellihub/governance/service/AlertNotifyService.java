package com.intellihub.governance.service;

import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.entity.AlertRule;
import com.intellihub.governance.mapper.AlertRuleMapper;
import com.intellihub.governance.notify.NotifyChannel;
import com.intellihub.governance.notify.NotifyChannelFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 告警通知服务
 * <p>
 * 使用策略模式支持多种通知渠道，新增渠道只需实现 NotifyChannel 接口即可
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotifyService {

    private final AlertRuleMapper alertRuleMapper;
    private final NotifyChannelFactory notifyChannelFactory;

    /**
     * 发送告警通知
     */
    public void notify(AlertRecord record) {
        AlertRule rule = alertRuleMapper.selectById(record.getRuleId());
        if (rule == null) {
            log.warn("告警规则不存在 - ruleId: {}", record.getRuleId());
            return;
        }

        String channels = rule.getNotifyChannels();
        String targets = rule.getNotifyTargets();

        if (channels == null || channels.isEmpty()) {
            log.debug("告警规则未配置通知渠道 - ruleId: {}", rule.getId());
            return;
        }

        String[] channelArray = channels.split(",");
        for (String channelType : channelArray) {
            try {
                NotifyChannel channel = notifyChannelFactory.getChannel(channelType);
                if (channel != null) {
                    channel.send(record, targets);
                } else {
                    log.warn("未知的通知渠道: {}", channelType);
                }
            } catch (Exception e) {
                log.error("发送告警通知失败 - channel: {}, recordId: {}", channelType, record.getId(), e);
            }
        }
    }
}
