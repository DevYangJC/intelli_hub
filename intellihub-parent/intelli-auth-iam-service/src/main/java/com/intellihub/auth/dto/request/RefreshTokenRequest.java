package com.intellihub.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 刷新Token请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
