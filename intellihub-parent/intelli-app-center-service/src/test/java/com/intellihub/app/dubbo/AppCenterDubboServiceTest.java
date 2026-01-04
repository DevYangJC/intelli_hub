package com.intellihub.app.dubbo;

import com.intellihub.dubbo.AppKeyInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * AppCenterDubboService 测试类
 * 用于验证 Dubbo 服务是否正常工作
 */
@Slf4j
@SpringBootTest
public class AppCenterDubboServiceTest {

    @Autowired
    private AppCenterDubboServiceImpl appCenterDubboService;

    /**
     * 测试获取 AppKey 信息
     */
    @Test
    public void testGetAppKeyInfo() {
        String appKey = "IH4315340gtRfKPamTHVU4GE";
        
        log.info("========================================");
        log.info("测试获取 AppKey 信息");
        log.info("AppKey: {}", appKey);
        log.info("========================================");
        
        AppKeyInfoDTO dto = appCenterDubboService.getAppKeyInfo(appKey);
        
        if (dto == null) {
            log.error("✗ AppKey 不存在或查询失败");
            log.error("请检查：");
            log.error("1. 数据库中是否存在该 AppKey");
            log.error("2. app_info 表中的 app_key 字段值是否正确");
            log.error("3. 应用状态是否正常");
        } else {
            log.info("✓ 成功获取 AppKey 信息");
            log.info("应用ID: {}", dto.getAppId());
            log.info("应用名称: {}", dto.getAppName());
            log.info("AppKey: {}", dto.getAppKey());
            log.info("AppSecret: {}", dto.getAppSecret());
            log.info("状态: {}", dto.getStatus());
            log.info("租户ID: {}", dto.getTenantId());
            log.info("过期时间: {}", dto.getExpireTime());
        }
        
        log.info("========================================");
    }

    /**
     * 主方法 - 用于快速测试
     */
    public static void main(String[] args) {
        System.out.println("请使用 JUnit 运行此测试类");
        System.out.println("或在 IDE 中右键点击 testGetAppKeyInfo() 方法 → Run");
    }
}
