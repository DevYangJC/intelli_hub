package com.intellihub.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.api.entity.SysConfig;
import com.intellihub.api.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    public Map<String, Object> getAllConfigs(String tenantId) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getTenantId, tenantId);
        List<SysConfig> configs = sysConfigMapper.selectList(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        for (SysConfig config : configs) {
            result.put(config.getConfigKey(), parseValue(config));
        }
        return result;
    }

    public String getConfig(String tenantId, String key) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getTenantId, tenantId)
               .eq(SysConfig::getConfigKey, key);
        SysConfig config = sysConfigMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    public void setConfig(String tenantId, String key, String value, String type, String description) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getTenantId, tenantId)
               .eq(SysConfig::getConfigKey, key);
        SysConfig config = sysConfigMapper.selectOne(wrapper);
        
        if (config == null) {
            config = new SysConfig();
            config.setTenantId(tenantId);
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType(type != null ? type : "string");
            config.setDescription(description);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            sysConfigMapper.insert(config);
        } else {
            config.setConfigValue(value);
            if (type != null) {
                config.setConfigType(type);
            }
            if (description != null) {
                config.setDescription(description);
            }
            config.setUpdatedAt(LocalDateTime.now());
            sysConfigMapper.updateById(config);
        }
    }

    public void batchSetConfigs(String tenantId, Map<String, Object> configs) {
        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String strValue = value != null ? String.valueOf(value) : null;
            String type = inferType(value);
            setConfig(tenantId, key, strValue, type, null);
        }
    }

    public void deleteConfig(String tenantId, String key) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getTenantId, tenantId)
               .eq(SysConfig::getConfigKey, key);
        sysConfigMapper.delete(wrapper);
    }

    private Object parseValue(SysConfig config) {
        String type = config.getConfigType();
        String value = config.getConfigValue();
        
        if (value == null) return null;
        
        try {
            switch (type) {
                case "number":
                    return Double.parseDouble(value);
                case "boolean":
                    return Boolean.parseBoolean(value);
                case "json":
                    return value;
                default:
                    return value;
            }
        } catch (Exception e) {
            return value;
        }
    }

    private String inferType(Object value) {
        if (value == null) return "string";
        if (value instanceof Number) return "number";
        if (value instanceof Boolean) return "boolean";
        return "string";
    }
}
