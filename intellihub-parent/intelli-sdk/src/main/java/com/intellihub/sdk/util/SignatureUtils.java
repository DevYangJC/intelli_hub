package com.intellihub.sdk.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * 签名工具类
 *
 * @author intellihub
 * @since 1.0.0
 */
public final class SignatureUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private SignatureUtils() {
    }

    /**
     * 生成请求签名
     * <p>
     * 签名算法：HMAC-SHA256(METHOD + PATH + TIMESTAMP + NONCE, AppSecret)
     * </p>
     *
     * @param method    请求方法（GET, POST等）
     * @param path      请求路径
     * @param timestamp 时间戳（毫秒）
     * @param nonce     随机数
     * @param appSecret 应用密钥
     * @return Base64编码的签名字符串
     */
    public static String generateSignature(String method, String path, String timestamp, String nonce, String appSecret) {
        String signData = method.toUpperCase() + path + timestamp + nonce;
        return hmacSha256(signData, appSecret);
    }

    /**
     * HMAC-SHA256 加密
     *
     * @param data   待加密数据
     * @param secret 密钥
     * @return Base64编码的加密结果
     */
    public static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }

    /**
     * 生成随机 Nonce
     *
     * @return UUID字符串（无连字符）
     */
    public static String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 时间戳字符串
     */
    public static String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
}
