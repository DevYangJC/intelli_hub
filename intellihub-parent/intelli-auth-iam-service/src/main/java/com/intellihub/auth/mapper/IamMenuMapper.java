package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单Mapper
 * 跳过租户拦截器：iam_menu 表无 tenant_id
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface IamMenuMapper extends BaseMapper<IamMenu> {
}
