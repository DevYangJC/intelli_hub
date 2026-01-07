package com.intellihub.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.event.entity.EventConsumeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 事件消费记录 Mapper 接口
 * <p>
 * 提供事件消费记录的数据库操作方法，继承 MyBatis-Plus 的 BaseMapper，
 * 自动提供基础的 CRUD 操作。
 * </p>
 * <p>
 * 多租户说明：
 * 该表需要租户隔离。在定时任务中使用时，需要通过
 * UserContextHolder.setIgnoreTenant(true) 临时豁免租户隔离。
 * </p>
 *
 * @author IntelliHub
 * @see com.intellihub.event.job.EventRetryJob
 */
@Mapper
public interface EventConsumeRecordMapper extends BaseMapper<EventConsumeRecord> {
}
