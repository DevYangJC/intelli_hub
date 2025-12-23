package com.intellihub.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码Key
     */
    private String captchaKey;
}
