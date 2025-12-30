package com.intellihub.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.event.entity.EventDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 事件定义 Mapper 接口
 * 提供事件定义的数据库操作方法
 * 继承 MyBatis-Plus 的 BaseMapper，自动提供基础的 CRUD 操作
 *
 * @author IntelliHub
 */
@Mapper
public interface EventDefinitionMapper extends BaseMapper<EventDefinition> {
}
