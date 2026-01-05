package com.intellihub.governance.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.governance.entity.AlertRequestDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 告警请求详情Mapper
 * 跳过租户拦截器：alert_request_detail 表无 tenant_id
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface AlertRequestDetailMapper extends BaseMapper<AlertRequestDetail> {

    /**
     * 根据告警记录ID查询请求详情
     * 跳过租户拦截器：alert_request_detail 表无 tenant_id
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM alert_request_detail WHERE alert_record_id = #{alertRecordId} ORDER BY request_time DESC")
    List<AlertRequestDetail> selectByAlertRecordId(@Param("alertRecordId") Long alertRecordId);
}
