package com.intellihub.common.dubbo;

import lombok.Data;

import java.io.Serializable;

/**
 * Token 验证响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ValidateTokenResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否有效
     */
    private Boolean valid;

    /**
     * 用户信息
     */
    private UserInfoDTO userInfo;

    /**
     * 错误消息
     */
    private String errorMessage;

    public static ValidateTokenResponse success(UserInfoDTO userInfo) {
        ValidateTokenResponse response = new ValidateTokenResponse();
        response.setValid(true);
        response.setUserInfo(userInfo);
        return response;
    }

    public static ValidateTokenResponse fail(String errorMessage) {
        ValidateTokenResponse response = new ValidateTokenResponse();
        response.setValid(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
