package com.intellihub.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 签名工具类
 * <p>
 * 用于生成和验证API调用签名
 * 采用 HMAC-SHA256 算法
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public class SignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * 生成签名
     * <p>
     * 签名规则：
     * 签名字符串 = HTTP_METHOD + "\n" + REQUEST_PATH + "\n" + TIMESTAMP + "\n" + NONCE
     * 签名 = Base64(HMAC-SHA256(签名字符串, AppSecret))
     * </p>
     *
     * @param method    HTTP方法
     * @param path      请求路径
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param appSecret 应用密钥
     * @return Base64编码的签名
     */
    public static String generateSignature(String method, String path, String timestamp, String nonce, String appSecret) {
        String signString = buildSignString(method, path, timestamp, nonce);
        return hmacSha256(signString, appSecret);
    }

    /**
     * 验证签名
     *
     * @param signature 待验证的签名
     * @param method    HTTP方法
     * @param path      请求路径
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param appSecret 应用密钥
     * @return 是否验证通过
     */
    public static boolean verifySignature(String signature, String method, String path, 
                                          String timestamp, String nonce, String appSecret) {
        String expectedSignature = generateSignature(method, path, timestamp, nonce, appSecret);
        return expectedSignature.equals(signature);
    }

    /**
     * 构建签名字符串
     */
    public static String buildSignString(String method, String path, String timestamp, String nonce) {
        return method.toUpperCase() + "\n" + path + "\n" + timestamp + "\n" + nonce;
    }

    /**
     * HMAC-SHA256签名
     *
     * @param data   待签名数据
     * @param secret 密钥
     * @return Base64编码的签名
     */
    public static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256签名失败", e);
        }
    }

    private SignatureUtil() {
        // 私有构造函数，防止实例化
    }
}
