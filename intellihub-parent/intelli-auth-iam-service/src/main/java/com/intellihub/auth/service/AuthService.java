package com.intellihub.auth.service;

import com.intellihub.auth.dto.request.ChangePasswordRequest;
import com.intellihub.auth.dto.request.LoginRequest;
import com.intellihub.auth.dto.request.RefreshTokenRequest;
import com.intellihub.auth.dto.request.RegisterRequest;
import com.intellihub.auth.dto.request.VerifyTokenRequest;
import com.intellihub.auth.dto.response.*;

/**
 * 认证服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request, String clientIp, String userAgent);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * 退出登录
     */
    void logout(String accessToken);

    /**
     * 获取验证码
     */
    CaptchaResponse getCaptcha();

    /**
     * 校验Token
     */
    TokenVerifyResponse verifyToken(VerifyTokenRequest request);

    /**
     * 获取当前用户信息
     */
    UserInfoResponse getCurrentUser(String accessToken);

    /**
     * 修改密码
     */
    void changePassword(String accessToken, ChangePasswordRequest request);

    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request, String clientIp, String userAgent);
}
