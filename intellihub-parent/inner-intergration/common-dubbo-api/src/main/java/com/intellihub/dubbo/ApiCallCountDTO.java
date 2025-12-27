package com.intellihub.dubbo;

import java.io.Serializable;

/**
 * API调用次数统计DTO
 * <p>
 * 用于跨服务传输API调用统计数据
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public class ApiCallCountDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * API ID
     */
    private String apiId;

    /**
     * 今日调用次数
     */
    private Long todayCalls;

    /**
     * 历史总调用次数
     */
    private Long totalCalls;

    public ApiCallCountDTO() {
    }

    public ApiCallCountDTO(String apiId, Long todayCalls, Long totalCalls) {
        this.apiId = apiId;
        this.todayCalls = todayCalls;
        this.totalCalls = totalCalls;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public Long getTodayCalls() {
        return todayCalls;
    }

    public void setTodayCalls(Long todayCalls) {
        this.todayCalls = todayCalls;
    }

    public Long getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(Long totalCalls) {
        this.totalCalls = totalCalls;
    }

    @Override
    public String toString() {
        return "ApiCallCountDTO{" +
                "apiId='" + apiId + '\'' +
                ", todayCalls=" + todayCalls +
                ", totalCalls=" + totalCalls +
                '}';
    }
}
