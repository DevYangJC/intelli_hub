package com.intellihub.app.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.app.entity.AppApiSubscription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用API订阅关系Mapper接口
 * 跳过租户拦截器：app_api_subscription 表无 tenant_id
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface AppApiSubscriptionMapper extends BaseMapper<AppApiSubscription> {
}
