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
    /**
     * INSERT INTO intelli_hub_app_center.app_api_subscription
     * (id, app_id, api_id, api_name, api_path, status, quota_limit, effective_time, expire_time, created_at, updated_at)
     * VALUES('802c19ae85f4534b976e498d59257d19', '315203d0386c8f6bafebcaafd259e44d', '2007729662223376385', '根据AppKey获取应用信息', '/open/app/{appkey}', 'ACTIVE', NULL, '2026-01-04 16:24:53', NULL, '2026-01-04 16:24:53', '2026-01-04 16:24:53');
     *
     * INSERT INTO intelli_hub_api_platform.api_backend
     * (id, api_id, `type`, protocol, `method`, host, `path`, timeout, connect_timeout, registry, interface_name, method_name, dubbo_version, dubbo_group, ref_api_id, mock_delay, created_at, updated_at)
     * VALUES('2007725269700763650', '2007696330425032706', 'dubbo', 'HTTP', NULL, NULL, NULL, 5000, 5000, 'nacos://127.0.0.1:8848', 'com.intellihub.dubbo.AuthDubboService', 'getUserById', '', 'intellihub', NULL, 0, '2026-01-04 16:06:21', '2026-01-04 08:42:24');
     * INSERT INTO intelli_hub_api_platform.api_backend
     * (id, api_id, `type`, protocol, `method`, host, `path`, timeout, connect_timeout, registry, interface_name, method_name, dubbo_version, dubbo_group, ref_api_id, mock_delay, created_at, updated_at)
     * VALUES('2007729662290485251', '2007729662223376385', 'dubbo', 'HTTP', NULL, NULL, NULL, 5000, 5000, 'nacos://127.0.0.1:8848', 'com.intellihub.dubbo.AppCenterDubboService', 'getAppKeyInfo', '', 'intellihub', NULL, 0, '2026-01-04 16:23:48', '2026-01-04 08:42:24');
     *
    * @param args
     */

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
