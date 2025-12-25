package com.intellihub.governance.notify;

import com.intellihub.governance.entity.AlertRecord;

/**
 * 通知渠道策略接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface NotifyChannel {

    /**
     * 获取渠道类型标识
     * @return 渠道标识（如 email, webhook, dingtalk, kafka）
     */
    String getChannelType();

    /**
     * 发送通知
     * @param record 告警记录
     * @param target 通知目标（邮箱地址/webhook URL/机器人URL等）
     * @return 是否发送成功
     */
    boolean send(AlertRecord record, String target);
}
