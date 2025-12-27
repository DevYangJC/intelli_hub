package com.intellihub.dubbo;

import java.io.Serializable;

/**
 * App调用次数统计DTO
 * <p>
 * 用于跨服务传输App配额使用统计数据
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public class AppCallCountDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 已使用配额（总调用次数）
     */
    private Long quotaUsed;

    public AppCallCountDTO() {
    }

    public AppCallCountDTO(String appId, Long quotaUsed) {
        this.appId = appId;
        this.quotaUsed = quotaUsed;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Long getQuotaUsed() {
        return quotaUsed;
    }

    public void setQuotaUsed(Long quotaUsed) {
        this.quotaUsed = quotaUsed;
    }

    @Override
    public String toString() {
        return "AppCallCountDTO{" +
                "appId='" + appId + '\'' +
                ", quotaUsed=" + quotaUsed +
                '}';
    }
}
