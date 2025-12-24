package com.intellihub.governance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.governance.entity.AlertRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警规则Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {
}
