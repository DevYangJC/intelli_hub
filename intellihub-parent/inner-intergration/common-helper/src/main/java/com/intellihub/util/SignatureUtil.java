package com.intellihub.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 签名工具类
 *
 * @author intellihub
 * @since 1.0.0
 */
public class SignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private SignatureUtil() {
    }

    /**
     * 生成签名
     *
     * @param params    请求参数
     * @param appSecret 应用密钥
     * @return 签名字符串
     */
    public static String generateSignature(Map<String, String> params, String appSecret) {
        // 按key排序
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        
        // 拼接参数
        String paramString = sortedParams.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        
        return hmacSha256(paramString, appSecret);
    }

    /**
     * 验证签名
     *
     * @param params    请求参数
     * @param signature 待验证的签名
     * @param appSecret 应用密钥
     * @return 是否验证通过
     */
    public static boolean verifySignature(Map<String, String> params, String signature, String appSecret) {
        String expectedSignature = generateSignature(params, appSecret);
        return expectedSignature.equals(signature);
    }

    /**
     * HMAC-SHA256加密
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
     * 验证网关签名（开放API使用）
     * <p>
     * 签名算法：HMAC-SHA256(method + path + timestamp + nonce, appSecret)
     * </p>
     *
     * @param signature 待验证的签名
     * @param method    请求方法
     * @param path      请求路径
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param appSecret 应用密钥
     * @return 是否验证通过
     */
    public static boolean verifySignature(String signature, String method, String path, 
            String timestamp, String nonce, String appSecret) {
        String signData = method.toUpperCase() + path + timestamp + nonce;
        String expectedSignature = hmacSha256(signData, appSecret);
        return expectedSignature.equals(signature);
    }

    /**
     * 生成网关签名（客户端使用）
     *
     * @param method    请求方法
     * @param path      请求路径
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param appSecret 应用密钥
     * @return 签名字符串
     */
    public static String generateSignature(String method, String path, 
            String timestamp, String nonce, String appSecret) {
        String signData = method.toUpperCase() + path + timestamp + nonce;
        return hmacSha256(signData, appSecret);
    }
}
