package com.intellihub.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {

    /**
     * 验证码Key
     */
    private String captchaKey;

    /**
     * 验证码图片（Base64）
     */
    private String captchaImage;

    /**
     * 过期时间（秒）
     */
    private Integer expiresIn;
}
