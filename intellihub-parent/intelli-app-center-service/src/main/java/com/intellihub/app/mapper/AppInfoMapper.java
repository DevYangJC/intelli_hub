package com.intellihub.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.app.entity.AppInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用信息Mapper接口
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AppInfoMapper extends BaseMapper<AppInfo> {
}
