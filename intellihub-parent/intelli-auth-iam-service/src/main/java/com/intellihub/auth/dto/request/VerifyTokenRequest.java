package com.intellihub.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 校验Token请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class VerifyTokenRequest {

    @NotBlank(message = "token不能为空")
    private String token;
}
