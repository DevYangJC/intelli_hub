package com.intellihub.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import java.util.List;

/**
 * 更新用户请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UpdateUserRequest {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色ID列表
     */
    private List<String> roleIds;
}
