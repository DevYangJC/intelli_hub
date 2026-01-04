package com.intellihub.sdk;

import com.intellihub.sdk.util.SignatureUtils;

/**
 * 签名调试测试类
 * 用于验证签名生成是否正确
 */
public class SignatureDebugTest {

    public static void main(String[] args) {
        // 测试参数
        String method = "GET";
        String path = "/open";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = "testnonce123456789012345678901234";
        String appSecret = "l8AhxFW5SIQJ5L4IDrxiVNU7jlZAplsF";

        System.out.println("========================================");
        System.out.println("签名调试测试");
        System.out.println("========================================");
        System.out.println();

        // 打印参数
        System.out.println("签名参数：");
        System.out.println("  Method: " + method);
        System.out.println("  Path: " + path);
        System.out.println("  Timestamp: " + timestamp);
        System.out.println("  Nonce: " + nonce);
        System.out.println("  AppSecret: " + appSecret);
        System.out.println();

        // 生成签名内容
        String signData = method.toUpperCase() + path + timestamp + nonce;
        System.out.println("签名内容（拼接）：");
        System.out.println("  " + signData);
        System.out.println();

        // 生成签名
        String signature = SignatureUtils.generateSignature(method, path, timestamp, nonce, appSecret);
        System.out.println("生成的签名：");
        System.out.println("  " + signature);
        System.out.println();

        // 完整的请求头
        System.out.println("========================================");
        System.out.println("完整的请求头（复制到 Postman）：");
        System.out.println("========================================");
        System.out.println("X-App-Key: IH4315340gtRfKPamTHVU4GE");
        System.out.println("X-Timestamp: " + timestamp);
        System.out.println("X-Nonce: " + nonce);
        System.out.println("X-Signature: " + signature);
        System.out.println();

        System.out.println("========================================");
        System.out.println("测试完成");
        System.out.println("========================================");
    }
}
