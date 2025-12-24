package com.intellihub.governance.job;

import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.service.AlertRecordService;
import com.intellihub.governance.service.AlertNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 告警通知定时任务
 * <p>
 * 定期发送未通知的告警
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertNotifyJob {

    private final AlertRecordService alertRecordService;
    private final AlertNotifyService alertNotifyService;

    /**
     * 每30秒检查一次未通知的告警
     */
    @Scheduled(fixedRate = 30000)
    public void notifyAlerts() {
        try {
            List<AlertRecord> unnotifiedRecords = alertRecordService.getUnnotifiedRecords();
            if (unnotifiedRecords.isEmpty()) {
                return;
            }

            log.info("发现 {} 条未通知的告警", unnotifiedRecords.size());

            for (AlertRecord record : unnotifiedRecords) {
                try {
                    alertNotifyService.notify(record);
                    alertRecordService.markNotified(record.getId());
                } catch (Exception e) {
                    log.error("发送告警通知失败 - recordId: {}", record.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("告警通知任务执行失败", e);
        }
    }
}
