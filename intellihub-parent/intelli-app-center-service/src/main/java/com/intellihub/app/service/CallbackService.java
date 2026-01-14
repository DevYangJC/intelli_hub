package com.intellihub.app.service;

import com.intellihub.app.entity.AppInfo;
import com.intellihub.app.mapper.AppInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 回调通知服务
 * <p>
 * 功能：
 * 1. 配额预警通知
 * 2. 应用状态变更通知
 * 3. API调用失败通知
 * 4. 应用过期提醒
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    private final AppInfoMapper appInfoMapper;
    private final RestTemplate restTemplate;

    /**
     * 发送配额预警通知
     * 当配额使用超过80%时触发
     *
     * @param appId 应用ID
     * @param quotaUsed 已使用配额
     * @param quotaLimit 配额限制
     */
    @Async
    public void sendQuotaWarning(String appId, Long quotaUsed, Long quotaLimit) {
        AppInfo app = appInfoMapper.selectById(appId);
        if (app == null || app.getCallbackUrl() == null || app.getCallbackUrl().isEmpty()) {
            return;
        }
        
        double usagePercent = (double) quotaUsed / quotaLimit * 100;
        
        Map<String, Object> data = new HashMap<>();
        data.put("eventType", "QUOTA_WARNING");
        data.put("appId", appId);
        data.put("appName", app.getName());
        data.put("quotaUsed", quotaUsed);
        data.put("quotaLimit", quotaLimit);
        data.put("usagePercent", String.format("%.2f%%", usagePercent));
        data.put("message", String.format("应用配额使用已达%.2f%%，请注意", usagePercent));
        data.put("timestamp", LocalDateTime.now().toString());
        
        sendCallback(app.getCallbackUrl(), data);
    }
    
    /**
     * 发送配额耗尽通知
     *
     * @param appId 应用ID
     */
    @Async
    public void sendQuotaExhausted(String appId) {
        AppInfo app = appInfoMapper.selectById(appId);
        if (app == null || app.getCallbackUrl() == null || app.getCallbackUrl().isEmpty()) {
            return;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("eventType", "QUOTA_EXHAUSTED");
        data.put("appId", appId);
        data.put("appName", app.getName());
        data.put("quotaLimit", app.getQuotaLimit());
        data.put("message", "应用配额已用完，API调用将被拒绝");
        data.put("timestamp", LocalDateTime.now().toString());
        
        sendCallback(app.getCallbackUrl(), data);
    }
    
    /**
     * 发送应用状态变更通知
     *
     * @param appId 应用ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    @Async
    public void sendAppStatusChange(String appId, String oldStatus, String newStatus) {
        AppInfo app = appInfoMapper.selectById(appId);
        if (app == null || app.getCallbackUrl() == null || app.getCallbackUrl().isEmpty()) {
            return;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("eventType", "APP_STATUS_CHANGE");
        data.put("appId", appId);
        data.put("appName", app.getName());
        data.put("oldStatus", oldStatus);
        data.put("newStatus", newStatus);
        data.put("message", String.format("应用状态从%s变更为%s", oldStatus, newStatus));
        data.put("timestamp", LocalDateTime.now().toString());
        
        sendCallback(app.getCallbackUrl(), data);
    }
    
    /**
     * 发送应用即将过期提醒
     * 在过期前3天、1天发送提醒
     *
     * @param appId 应用ID
     * @param daysRemaining 剩余天数
     */
    @Async
    public void sendAppExpirationReminder(String appId, int daysRemaining) {
        AppInfo app = appInfoMapper.selectById(appId);
        if (app == null || app.getCallbackUrl() == null || app.getCallbackUrl().isEmpty()) {
            return;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("eventType", "APP_EXPIRATION_REMINDER");
        data.put("appId", appId);
        data.put("appName", app.getName());
        data.put("daysRemaining", daysRemaining);
        data.put("expireTime", app.getExpireTime().toString());
        data.put("message", String.format("应用将在%d天后过期，请及时续期", daysRemaining));
        data.put("timestamp", LocalDateTime.now().toString());
        
        sendCallback(app.getCallbackUrl(), data);
    }
    
    /**
     * 发送API调用失败通知
     *
     * @param appId 应用ID
     * @param apiPath API路径
     * @param errorMessage 错误信息
     */
    @Async
    public void sendApiCallFailure(String appId, String apiPath, String errorMessage) {
        AppInfo app = appInfoMapper.selectById(appId);
        if (app == null || app.getCallbackUrl() == null || app.getCallbackUrl().isEmpty()) {
            return;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("eventType", "API_CALL_FAILURE");
        data.put("appId", appId);
        data.put("appName", app.getName());
        data.put("apiPath", apiPath);
        data.put("errorMessage", errorMessage);
        data.put("timestamp", LocalDateTime.now().toString());
        
        sendCallback(app.getCallbackUrl(), data);
    }
    
    /**
     * 发送回调请求
     *
     * @param callbackUrl 回调URL
     * @param data 回调数据
     */
    private void sendCallback(String callbackUrl, Map<String, Object> data) {
        try {
            log.info("[回调通知] 发送回调 - URL: {}, EventType: {}", 
                    callbackUrl, data.get("eventType"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.USER_AGENT, "IntelliHub-Callback/1.0");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
            
            // 使用RestTemplate发送POST请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    callbackUrl, request, String.class);
            
            log.info("[回调通知] 回调成功 - URL: {}, Status: {}", 
                    callbackUrl, response.getStatusCode());
        } catch (Exception e) {
            log.error("[回调通知] 回调失败 - URL: {}, Error: {}", 
                    callbackUrl, e.getMessage());
        }
    }
    
    /**
     * 同步发送回调（用于需要等待结果的场景）
     *
     * @param callbackUrl 回调URL
     * @param data 回调数据
     * @return 是否成功
     */
    public boolean sendCallbackSync(String callbackUrl, Map<String, Object> data) {
        try {
            log.info("[回调通知] 同步发送回调 - URL: {}, EventType: {}", 
                    callbackUrl, data.get("eventType"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.USER_AGENT, "IntelliHub-Callback/1.0");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
            
            // 使用RestTemplate发送POST请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    callbackUrl, request, String.class);
            
            log.info("[回调通知] 同步回调成功 - URL: {}, Status: {}", 
                    callbackUrl, response.getStatusCode());
            return true;
        } catch (Exception e) {
            log.error("[回调通知] 同步回调失败 - URL: {}, Error: {}", 
                    callbackUrl, e.getMessage());
            return false;
        }
    }
}

