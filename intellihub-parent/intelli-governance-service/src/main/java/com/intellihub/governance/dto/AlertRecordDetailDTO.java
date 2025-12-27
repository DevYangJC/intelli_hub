package com.intellihub.governance.dto;

import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.entity.AlertRequestDetail;
import lombok.Data;

import java.util.List;

/**
 * 告警详情DTO
 * <p>
 * 包含告警记录和触发告警的请求详情列表
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AlertRecordDetailDTO {

    /**
     * 告警记录
     */
    private AlertRecord record;

    /**
     * 触发告警的请求详情列表
     */
    private List<AlertRequestDetail> requestDetails;

    /**
     * 请求详情数量
     */
    private int requestCount;
}
