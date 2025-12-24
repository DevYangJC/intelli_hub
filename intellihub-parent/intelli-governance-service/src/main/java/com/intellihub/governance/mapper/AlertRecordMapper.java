package com.intellihub.governance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.governance.entity.AlertRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警记录Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AlertRecordMapper extends BaseMapper<AlertRecord> {
}
