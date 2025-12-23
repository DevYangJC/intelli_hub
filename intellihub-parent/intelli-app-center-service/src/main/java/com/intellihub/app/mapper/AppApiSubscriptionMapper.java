package com.intellihub.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.app.entity.AppApiSubscription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用API订阅关系Mapper接口
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AppApiSubscriptionMapper extends BaseMapper<AppApiSubscription> {
}
