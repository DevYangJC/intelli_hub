package com.intellihub.dubbo;

import lombok.Data;

import java.io.Serializable;

/**
 * Token验证响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ValidateTokenResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean valid;
    private String message;
    private UserInfoDTO userInfo;

    public static ValidateTokenResponse success(UserInfoDTO userInfo) {
        ValidateTokenResponse response = new ValidateTokenResponse();
        response.setValid(true);
        response.setUserInfo(userInfo);
        return response;
    }

    public static ValidateTokenResponse fail(String message) {
        ValidateTokenResponse response = new ValidateTokenResponse();
        response.setValid(false);
        response.setMessage(message);
        return response;
    }
}
