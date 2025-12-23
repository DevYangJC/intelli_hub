package com.intellihub.auth.controller;

import com.intellihub.auth.dto.request.ChangePasswordRequest;
import com.intellihub.auth.dto.request.LoginRequest;
import com.intellihub.auth.dto.request.RefreshTokenRequest;
import com.intellihub.auth.dto.request.RegisterRequest;
import com.intellihub.auth.dto.request.VerifyTokenRequest;
import com.intellihub.auth.dto.response.*;
import com.intellihub.auth.service.AuthService;
import com.intellihub.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/iam/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = authService.login(request, clientIp, userAgent);
        return ApiResponse.success("登录成功", response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = authService.register(request, clientIp, userAgent);
        return ApiResponse.success("注册成功", response);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ApiResponse.success(response);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return ApiResponse.success("退出成功", null);
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> getCaptcha() {
        CaptchaResponse response = authService.getCaptcha();
        return ApiResponse.success(response);
    }

    /**
     * 校验Token
     */
    @PostMapping("/verify")
    public ApiResponse<TokenVerifyResponse> verifyToken(@Valid @RequestBody VerifyTokenRequest request) {
        TokenVerifyResponse response = authService.verifyToken(request);
        return ApiResponse.success(response);
    }

    /**
     * 验证Token（供网关调用）
     */
    @GetMapping("/validate")
    public ApiResponse<TokenVerifyResponse> validateToken(@RequestHeader("Authorization") String authorization) {
        String token = authorization;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        VerifyTokenRequest request = new VerifyTokenRequest();
        request.setToken(token);
        TokenVerifyResponse response = authService.verifyToken(request);
        return ApiResponse.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        UserInfoResponse response = authService.getCurrentUser(authorization);
        return ApiResponse.success(response);
    }



    /**
     * 修改密码
     */
    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@RequestHeader("Authorization") String authorization,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authorization, request);
        return ApiResponse.success("密码修改成功", null);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
