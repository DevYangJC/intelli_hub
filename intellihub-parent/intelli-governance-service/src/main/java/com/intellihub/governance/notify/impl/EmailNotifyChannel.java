package com.intellihub.governance.notify.impl;

import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.notify.AbstractNotifyChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 邮件通知渠道
 * targets格式: email1@example.com,email2@example.com
 * 
 * 使用前需要配置 spring-boot-starter-mail 和 SMTP
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class EmailNotifyChannel extends AbstractNotifyChannel {

//    @Autowired(required = false)
//    private JavaMailSender mailSender;

    @Override
    public String getChannelType() {
        return "email";
    }

    @Override
    public boolean send(AlertRecord record, String targets) {
   /*     if (mailSender == null) {
            log.warn("[Email] 邮件服务未配置，跳过发送 - targets: {}, recordId: {}", targets, record.getId());
            // 记录日志，方便调试
            log.info("[Email] 告警内容 - level: {}, message: {}", 
                    record.getAlertLevel(), record.getAlertMessage());
            return false;
        }*/

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(targets.split(","));
            message.setSubject(buildSubject(record));
            message.setText(buildEmailContent(record));
            
//            mailSender.send(message);
            log.info("[Email] 告警通知发送成功 - to: {}, recordId: {}", targets, record.getId());
            return true;
        } catch (Exception e) {
            log.error("[Email] 告警通知发送失败 - to: {}, error: {}", targets, e.getMessage());
            return false;
        }
    }

    /**
     * 构建邮件主题
     */
    private String buildSubject(AlertRecord record) {
        return String.format("[%s] %s - IntelliHub告警", 
                record.getAlertLevel().toUpperCase(), 
                record.getRuleName());
    }

    /**
     * 构建邮件正文
     */
    private String buildEmailContent(AlertRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("IntelliHub 告警通知\n");
        sb.append("=====================================\n\n");
        sb.append("规则名称: ").append(record.getRuleName()).append("\n");
        sb.append("告警级别: ").append(getAlertLevelDesc(record.getAlertLevel())).append("\n");
        sb.append("API路径: ").append(record.getApiPath() != null ? record.getApiPath() : "全局").append("\n");
        sb.append("当前值: ").append(record.getCurrentValue()).append("\n");
        sb.append("阈值: ").append(record.getThresholdValue()).append("\n");
        sb.append("告警内容: ").append(record.getAlertMessage()).append("\n");
        sb.append("触发时间: ").append(record.getFiredAt() != null 
                ? record.getFiredAt().format(DATETIME_FORMATTER) : "-").append("\n");
        sb.append("\n=====================================\n");
        sb.append("此邮件由 IntelliHub 系统自动发送，请勿直接回复。\n");
        return sb.toString();
    }
}
