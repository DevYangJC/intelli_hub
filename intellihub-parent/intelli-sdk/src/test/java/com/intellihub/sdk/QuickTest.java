package com.intellihub.sdk;

import com.intellihub.sdk.model.ApiResponse;

import java.util.Map;

/**
 * SDK 快速测试类
 * 用于快速验证 SDK 功能
 * 
 * @author intellihub
 */
public class QuickTest {


    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("IntelliHub SDK 快速测试");
        System.out.println("========================================");
        System.out.println();

        // 1. 配置 SDK 客户端
        System.out.println("1. 配置 SDK 客户端...");
        IntelliHubConfig config = IntelliHubConfig.builder()
                .baseUrl("http://localhost:8080")
                .appKey("IH4315340gtRfKPamTHVU4GE")
                .appSecret("l8AhxFW5SIQJ5L4IDrxiVNU7jlZAplsF")
                .connectTimeout(5000)
                .readTimeout(10000)
                .build();

        IntelliHubClient client = IntelliHubClient.create(config);
        System.out.println("✓ SDK 客户端配置完成");
        System.out.println();

        // 2. 测试 GET 请求
        System.out.println("2. 测试 GET 请求...");
        try {
            ApiResponse<Map> response = client.get("/open/app/IH4315340gtRfKPamTHVU4GE", Map.class);
            
            System.out.println("响应状态码: " + response.getCode());
            System.out.println("响应消息: " + response.getMessage());
            System.out.println("是否成功: " + response.isSuccess());
            
            if (response.isSuccess()) {
                System.out.println("✓ GET 请求成功");
                System.out.println("响应数据: " + response.getData());
            } else {
                System.out.println("✗ GET 请求失败: " + response.getMessage());
            }
        } catch (Exception e) {
            System.out.println("✗ GET 请求异常: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();

        // 3. 完成
        System.out.println("========================================");
        System.out.println("测试完成");
        System.out.println("========================================");
    }
}
