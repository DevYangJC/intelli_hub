package com.intellihub.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * AppKey生成工具类
 *
 * @author intellihub
 * @since 1.0.0
 */
public final class AppKeyGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private AppKeyGenerator() {
    }

    /**
     * 生成AppKey
     * 格式：IH + 时间戳后6位 + 随机16位字符
     */
    public static String generateAppKey() {
        long timestamp = System.currentTimeMillis();
        String timePart = String.valueOf(timestamp).substring(7);
        String randomPart = generateRandomString(16);
        return "IH" + timePart + randomPart;
    }

    /**
     * 生成AppSecret
     * 格式：32位随机字符
     */
    public static String generateAppSecret() {
        return generateRandomString(32);
    }

    /**
     * 生成指定长度的随机字符串
     */
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 生成UUID格式的ID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
