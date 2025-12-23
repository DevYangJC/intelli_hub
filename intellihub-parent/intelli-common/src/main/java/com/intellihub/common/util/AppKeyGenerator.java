package com.intellihub.common.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * AppKey/AppSecret生成工具类
 * <p>
 * 用于生成应用的AppKey和AppSecret
 * AppKey: 32位，用于标识应用身份
 * AppSecret: 64位，用于签名验证
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public class AppKeyGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String APP_KEY_PREFIX = "AK";
    private static final String APP_SECRET_PREFIX = "AS";

    /**
     * 生成AppKey
     * <p>
     * 格式：AK + 30位随机字符串，共32位
     * </p>
     *
     * @return AppKey
     */
    public static String generateAppKey() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return APP_KEY_PREFIX + uuid.substring(0, 30).toUpperCase();
    }

    /**
     * 生成AppSecret
     * <p>
     * 格式：AS + 62位Base64编码随机字符串，共64位
     * </p>
     *
     * @return AppSecret
     */
    public static String generateAppSecret() {
        byte[] randomBytes = new byte[48];  // 48 bytes -> 64 characters in Base64
        SECURE_RANDOM.nextBytes(randomBytes);
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return APP_SECRET_PREFIX + base64.substring(0, 62);
    }

    /**
     * 生成随机Nonce
     * <p>
     * 用于防重放攻击
     * </p>
     *
     * @return 32位随机字符串
     */
    public static String generateNonce() {
        byte[] randomBytes = new byte[24];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private AppKeyGenerator() {
        // 私有构造函数，防止实例化
    }
}
