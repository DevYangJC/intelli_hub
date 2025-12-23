package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface IamLoginLogMapper extends BaseMapper<IamLoginLog> {
}
