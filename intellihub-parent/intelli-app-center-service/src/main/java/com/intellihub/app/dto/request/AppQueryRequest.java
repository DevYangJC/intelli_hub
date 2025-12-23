package com.intellihub.app.dto.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 应用查询请求DTO
 * <p>
 * 用于接收应用列表查询时的筛选和分页参数
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AppQueryRequest {

    /**
     * 应用名称，模糊匹配
     */
    private String name;

    /**
     * 应用编码，模糊匹配
     */
    private String code;

    /**
     * 应用类型：internal-内部应用，external-外部应用
     */
    private String appType;

    /**
     * 应用状态：active-正常，disabled-禁用，expired-过期
     */
    private String status;

    /**
     * 当前页码，默认1
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    /**
     * 每页大小，默认10
     */
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 10;
}
