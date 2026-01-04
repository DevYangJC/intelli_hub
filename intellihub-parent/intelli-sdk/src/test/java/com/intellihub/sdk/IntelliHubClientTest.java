package com.intellihub.sdk;

import com.intellihub.sdk.model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * IntelliHub SDK 测试类
 * 
 * @author intellihub
 */
public class IntelliHubClientTest {

    private IntelliHubClient client;

    @BeforeEach
    public void setUp() {
        // 配置 SDK 客户端
        IntelliHubConfig config = IntelliHubConfig.builder()
                .baseUrl("http://localhost:8080")
                .appKey("IH4315340gtRfKPamTHVU4GE")  // 替换为你的 AppKey
                .appSecret("your-app-secret")  // 替换为你的 AppSecret
                .connectTimeout(5000)
                .readTimeout(10000)
                .build();

        client = IntelliHubClient.create(config);
    }

    /**
     * 测试 GET 请求 - 获取用户信息
     */
    @Test
    public void testGetUser() {
        try {
            // 调用 API
            ApiResponse<Map> response = client.get("/open/user/1", Map.class);

            // 打印结果
            System.out.println("=== 获取用户信息 ===");
            System.out.println("状态码: " + response.getCode());
            System.out.println("消息: " + response.getMessage());
            System.out.println("成功: " + response.isSuccess());
            System.out.println("数据: " + response.getData());
            System.out.println();

            // 断言
            assert response.isSuccess() : "API 调用失败: " + response.getMessage();
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试 GET 请求 - 带查询参数
     */
    @Test
    public void testGetWithQueryParams() {
        try {
            // 构建查询参数
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("page", "1");
            queryParams.put("size", "10");

            // 调用 API
            ApiResponse<Map> response = client.get("/open/users", queryParams, Map.class);

            // 打印结果
            System.out.println("=== 获取用户列表 ===");
            System.out.println("状态码: " + response.getCode());
            System.out.println("消息: " + response.getMessage());
            System.out.println("成功: " + response.isSuccess());
            System.out.println("数据: " + response.getData());
            System.out.println();

            // 断言
            assert response.isSuccess() : "API 调用失败: " + response.getMessage();
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试 POST 请求 - 创建用户
     */
    @Test
    public void testPostRequest() {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("username", "testuser");
            requestBody.put("email", "test@example.com");
            requestBody.put("phone", "13800138000");

            // 调用 API
            ApiResponse<Map> response = client.post("/open/users", requestBody, Map.class);

            // 打印结果
            System.out.println("=== 创建用户 ===");
            System.out.println("状态码: " + response.getCode());
            System.out.println("消息: " + response.getMessage());
            System.out.println("成功: " + response.isSuccess());
            System.out.println("数据: " + response.getData());
            System.out.println();

            // 断言
            assert response.isSuccess() : "API 调用失败: " + response.getMessage();
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试 PUT 请求 - 更新用户
     */
    @Test
    public void testPutRequest() {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("username", "updateduser");
            requestBody.put("email", "updated@example.com");

            // 调用 API
            ApiResponse<Map> response = client.put("/open/user/1", requestBody, Map.class);

            // 打印结果
            System.out.println("=== 更新用户 ===");
            System.out.println("状态码: " + response.getCode());
            System.out.println("消息: " + response.getMessage());
            System.out.println("成功: " + response.isSuccess());
            System.out.println("数据: " + response.getData());
            System.out.println();

            // 断言
            assert response.isSuccess() : "API 调用失败: " + response.getMessage();
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试 DELETE 请求 - 删除用户
     */
    @Test
    public void testDeleteRequest() {
        try {
            // 调用 API
            ApiResponse<Map> response = client.delete("/open/user/1", Map.class);

            // 打印结果
            System.out.println("=== 删除用户 ===");
            System.out.println("状态码: " + response.getCode());
            System.out.println("消息: " + response.getMessage());
            System.out.println("成功: " + response.isSuccess());
            System.out.println("数据: " + response.getData());
            System.out.println();

            // 断言
            assert response.isSuccess() : "API 调用失败: " + response.getMessage();
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 主方法 - 用于快速测试
     */
    public static void main(String[] args) {
        IntelliHubClientTest test = new IntelliHubClientTest();
        test.setUp();

        System.out.println("========================================");
        System.out.println("IntelliHub SDK 测试");
        System.out.println("========================================");
        System.out.println();

        // 运行测试
        test.testGetUser();
        test.testGetWithQueryParams();
        test.testPostRequest();
        test.testPutRequest();
        test.testDeleteRequest();

        System.out.println("========================================");
        System.out.println("所有测试完成");
        System.out.println("========================================");
    }
}
