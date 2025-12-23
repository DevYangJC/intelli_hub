package com.intellihub.auth.service.impl;

import com.intellihub.auth.config.AuthProperties;
import com.intellihub.auth.dto.request.ChangePasswordRequest;
import com.intellihub.auth.dto.request.LoginRequest;
import com.intellihub.auth.dto.request.RefreshTokenRequest;
import com.intellihub.auth.dto.request.RegisterRequest;
import com.intellihub.auth.dto.request.VerifyTokenRequest;
import com.intellihub.auth.dto.response.*;
import com.intellihub.auth.entity.*;
import com.intellihub.auth.mapper.*;
import com.intellihub.auth.service.AuthService;
import com.intellihub.auth.util.CaptchaUtil;
import com.intellihub.auth.util.JwtUtil;
import com.intellihub.exception.BusinessException;
import com.intellihub.constants.ResponseStatus;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final IamUserMapper userMapper;
    private final IamTenantMapper tenantMapper;
    private final IamRoleMapper roleMapper;
    private final IamPermissionMapper permissionMapper;
    private final IamLoginLogMapper loginLogMapper;
    private final IamUserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;
    private final CaptchaUtil captchaUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    private static final String CAPTCHA_KEY_PREFIX = "iam:captcha:";
    private static final String TOKEN_BLACKLIST_PREFIX = "iam:token:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "iam:refresh:";

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String clientIp, String userAgent) {
        // 验证验证码
        if (captchaUtil.isEnabled()) {
            validateCaptcha(request.getCaptchaKey(), request.getCaptcha());
        }

        // 查询用户
        IamUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            saveLoginLog(null, request.getUsername(), null, "fail", "用户不存在", clientIp, userAgent);
            throw new BusinessException(ResponseStatus.ACCOUNT_INCORRECT);
        }

        // 检查账户状态
        checkUserStatus(user);

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 增加登录失败次数
            handleLoginFailure(user);
            saveLoginLog(user.getId(), user.getUsername(), user.getTenantId(), "fail", "密码错误", clientIp, userAgent);
            throw new BusinessException(ResponseStatus.ACCOUNT_INCORRECT);
        }

        // 重置登录失败次数
        user.setLoginFailCount(0);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        userMapper.updateById(user);

        // 查询租户信息
        IamTenant tenant = tenantMapper.selectById(user.getTenantId());

        // 查询角色和权限
        List<IamRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<IamPermission> permissions = permissionMapper.selectPermissionsByUserId(user.getId());

        List<String> roleNames = roles.stream().map(IamRole::getName).collect(Collectors.toList());
        List<String> roleCodes = roles.stream().map(IamRole::getCode).collect(Collectors.toList());
        List<String> permissionCodes = permissions.stream().map(IamPermission::getCode).collect(Collectors.toList());

        // 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId(), roleCodes);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 存储refreshToken到Redis
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                authProperties.getJwt().getRefreshTokenExpiration(),
                TimeUnit.SECONDS
        );

        // 记录登录日志
        saveLoginLog(user.getId(), user.getUsername(), user.getTenantId(), "success", null, clientIp, userAgent);

        // 构建响应
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(roleCodes.isEmpty() ? null : roleCodes.get(0))
                .roleNames(roleNames)
                .permissions(permissionCodes)
                .tenantId(user.getTenantId())
                .tenantName(tenant != null ? tenant.getName() : null)
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .user(userInfo)
                .build();
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 验证refreshToken
        String userId;
        try {
            userId = jwtUtil.getUserId(refreshToken);
            String tokenType = jwtUtil.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                throw new BusinessException(ResponseStatus.TOKEN_INVALID);
            }
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ResponseStatus.TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new BusinessException(ResponseStatus.TOKEN_INVALID);
        }

        // 检查Redis中的refreshToken
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ResponseStatus.TOKEN_INVALID);
        }

        // 查询用户
        IamUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseStatus.TOKEN_INVALID);
        }

        // 查询角色
        List<IamRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream().map(IamRole::getCode).collect(Collectors.toList());

        // 生成新Token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId(), roleCodes);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 更新Redis中的refreshToken
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                newRefreshToken,
                authProperties.getJwt().getRefreshTokenExpiration(),
                TimeUnit.SECONDS
        );

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public void logout(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return;
        }

        // 移除Bearer前缀
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        try {
            String userId = jwtUtil.getUserId(accessToken);
            Date expiration = jwtUtil.getExpiration(accessToken);

            // 将Token加入黑名单
            long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + accessToken,
                        "1",
                        ttl,
                        TimeUnit.SECONDS
                );
            }

            // 删除refreshToken
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        } catch (Exception e) {
            log.warn("退出登录时解析Token失败: {}", e.getMessage());
        }
    }

    @Override
    public CaptchaResponse getCaptcha() {
        String code = captchaUtil.generateCode();
        String image = captchaUtil.generateImage(code);
        String key = UUID.randomUUID().toString().replace("-", "");

        // 存储验证码到Redis
        redisTemplate.opsForValue().set(
                CAPTCHA_KEY_PREFIX + key,
                code.toLowerCase(),
                captchaUtil.getExpiration(),
                TimeUnit.SECONDS
        );

        return CaptchaResponse.builder()
                .captchaKey(key)
                .captchaImage(image)
                .expiresIn(captchaUtil.getExpiration())
                .build();
    }

    @Override
    public TokenVerifyResponse verifyToken(VerifyTokenRequest request) {
        String token = request.getToken();

        // 检查黑名单
        Boolean isBlacklisted = redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token);
        if (Boolean.TRUE.equals(isBlacklisted)) {
            return TokenVerifyResponse.builder().valid(false).build();
        }

        try {
            String userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            String tenantId = jwtUtil.getTenantId(token);
            List<String> roles = jwtUtil.getRoles(token);
            Date expiration = jwtUtil.getExpiration(token);

            // 查询权限
            List<IamPermission> permissions = permissionMapper.selectPermissionsByUserId(userId);
            List<String> permissionCodes = permissions.stream()
                    .map(IamPermission::getCode)
                    .collect(Collectors.toList());

            return TokenVerifyResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .username(username)
                    .tenantId(tenantId)
                    .role(roles != null && !roles.isEmpty() ? roles.get(0) : null)
                    .permissions(permissionCodes)
                    .expiresAt(LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault()))
                    .build();
        } catch (ExpiredJwtException e) {
            return TokenVerifyResponse.builder().valid(false).build();
        } catch (Exception e) {
            return TokenVerifyResponse.builder().valid(false).build();
        }
    }

    @Override
    public UserInfoResponse getCurrentUser(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw new BusinessException(ResponseStatus.TOKEN_INVALID);
        }
        
        // 移除Bearer前缀
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        String userId = jwtUtil.getUserId(accessToken);
        IamUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseStatus.TOKEN_INVALID);
        }

        // 查询租户
        IamTenant tenant = tenantMapper.selectById(user.getTenantId());

        // 查询角色和权限
        List<IamRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<IamPermission> permissions = permissionMapper.selectPermissionsByUserId(user.getId());

        List<String> roleNames = roles.stream().map(IamRole::getName).collect(Collectors.toList());
        List<String> roleCodes = roles.stream().map(IamRole::getCode).collect(Collectors.toList());
        List<String> permissionCodes = permissions.stream().map(IamPermission::getCode).collect(Collectors.toList());

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(roleCodes.isEmpty() ? null : roleCodes.get(0))
                .roleNames(roleNames)
                .permissions(permissionCodes)
                .tenantId(user.getTenantId())
                .tenantName(tenant != null ? tenant.getName() : null)
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .build();
    }

    @Override
    @Transactional
    public void changePassword(String accessToken, ChangePasswordRequest request) {
        // 验证新密码和确认密码
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(4000, "两次输入的密码不一致");
        }

        // 移除Bearer前缀
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        String userId = jwtUtil.getUserId(accessToken);
        IamUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseStatus.TOKEN_INVALID);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResponseStatus.PASSWORD_INCORRECT);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        // 使当前Token失效
        logout(accessToken);
    }

    /**
     * 验证验证码
     */
    private void validateCaptcha(String captchaKey, String captcha) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captcha)) {
            throw new BusinessException(ResponseStatus.CAPTCHA_INCORRECT);
        }

        String storedCaptcha = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + captchaKey);
        if (storedCaptcha == null) {
            throw new BusinessException(ResponseStatus.CAPTCHA_INCORRECT);
        }

        // 删除验证码（一次性使用）
        redisTemplate.delete(CAPTCHA_KEY_PREFIX + captchaKey);

        if (!storedCaptcha.equalsIgnoreCase(captcha)) {
            throw new BusinessException(ResponseStatus.CAPTCHA_INCORRECT);
        }
    }

    /**
     * 检查用户状态
     */
    private void checkUserStatus(IamUser user) {
        if ("inactive".equals(user.getStatus())) {
            throw new BusinessException(ResponseStatus.ACCOUNT_DISABLED);
        }
        if ("locked".equals(user.getStatus())) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new BusinessException(ResponseStatus.ACCOUNT_LOCKED);
            }
            // 锁定时间已过，解锁账户
            user.setStatus("active");
            user.setLoginFailCount(0);
            user.setLockedUntil(null);
            userMapper.updateById(user);
        }
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(IamUser user) {
        int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
        user.setLoginFailCount(failCount);

        // 失败5次锁定30分钟
        if (failCount >= 5) {
            user.setStatus("locked");
            user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        }

        userMapper.updateById(user);
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(String userId, String username, String tenantId, String result, String failReason, String ip, String userAgent) {
        IamLoginLog log = IamLoginLog.builder()
                .userId(userId)
                .username(username)
                .tenantId(tenantId)
                .loginType("password")
                .loginResult(result)
                .failReason(failReason)
                .ip(ip)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .build();
        loginLogMapper.insert(log);
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request, String clientIp, String userAgent) {
        // 验证验证码
        if (captchaUtil.isEnabled()) {
            validateCaptcha(request.getCaptchaKey(), request.getCaptcha());
        }

        // 验证两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(4000, "两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        IamUser existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(4001, "用户名已存在");
        }

        // 检查邮箱是否已存在
/*        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            IamUser existingEmail = userMapper.selectByEmail(request.getEmail());
            if (existingEmail != null) {
                throw new BusinessException(4002, "邮箱已被注册");
            }
        }*/

        // 使用配置的默认租户
        String defaultTenantId = authProperties.getRegister().getDefaultTenantId();

        // 创建用户
        IamUser user = IamUser.builder()
                .tenantId(defaultTenantId)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status("active")
                .loginFailCount(0)
                .build();
        userMapper.insert(user);

        // 分配默认角色（普通用户）
        IamUserRole userRole = IamUserRole.builder()
                .userId(user.getId())
                .roleId(authProperties.getRegister().getDefaultRoleId())
                .build();
        userRoleMapper.insert(userRole);

        // 查询租户信息
        IamTenant tenant = tenantMapper.selectById(defaultTenantId);

        // 查询角色和权限
        List<IamRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<IamPermission> permissions = permissionMapper.selectPermissionsByUserId(user.getId());

        List<String> roleNames = roles.stream().map(IamRole::getName).collect(Collectors.toList());
        List<String> roleCodes = roles.stream().map(IamRole::getCode).collect(Collectors.toList());
        List<String> permissionCodes = permissions.stream().map(IamPermission::getCode).collect(Collectors.toList());

        // 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId(), roleCodes);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 存储refreshToken到Redis
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                authProperties.getJwt().getRefreshTokenExpiration(),
                TimeUnit.SECONDS
        );

        // 记录登录日志
        saveLoginLog(user.getId(), user.getUsername(), user.getTenantId(), "success", null, clientIp, userAgent);

        // 构建响应
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(roleCodes.isEmpty() ? "user" : roleCodes.get(0))
                .roleNames(roleNames.isEmpty() ? Collections.singletonList("普通用户") : roleNames)
                .permissions(permissionCodes)
                .tenantId(user.getTenantId())
                .tenantName(tenant != null ? tenant.getName() : null)
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .user(userInfo)
                .build();
    }
}
