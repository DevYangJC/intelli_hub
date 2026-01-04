package com.intellihub.auth.dubbo;

import com.intellihub.auth.entity.IamTenant;
import com.intellihub.auth.mapper.IamTenantMapper;
import com.intellihub.dubbo.TenantInfoDTO;
import com.intellihub.dubbo.TenantDubboService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 租户Dubbo服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@DubboService
@RequiredArgsConstructor
public class TenantDubboServiceImpl implements TenantDubboService {

    private final IamTenantMapper tenantMapper;

    @Override
    public boolean isValidTenant(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return false;
        }

        // 默认租户始终有效
        if ("default".equals(tenantId)) {
            return true;
        }

        try {
            IamTenant tenant = tenantMapper.selectById(tenantId);
            if (tenant == null) {
                log.debug("租户不存在 - tenantId: {}", tenantId);
                return false;
            }

            // 检查状态
            if (!"active".equals(tenant.getStatus())) {
                log.debug("租户已禁用 - tenantId: {}, status: {}", tenantId, tenant.getStatus());
                return false;
            }

            // 检查过期时间
//            if (tenant.getExpireTime() != null &&
//                tenant.getExpireTime().isBefore(java.time.LocalDateTime.now())) {
//                log.debug("租户已过期 - tenantId: {}", tenantId);
//                return false;
//            }

            return true;
        } catch (Exception e) {
            log.error("验证租户失败 - tenantId: {}", tenantId, e);
            return false;
        }
    }

    @Override
    public TenantInfoDTO getTenantInfo(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return null;
        }

        try {
            IamTenant tenant = tenantMapper.selectById(tenantId);
            return convertToDTO(tenant);
        } catch (Exception e) {
            log.error("获取租户信息失败 - tenantId: {}", tenantId, e);
            return null;
        }
    }

    @Override
    public TenantInfoDTO getTenantByCode(String tenantCode) {
        if (tenantCode == null || tenantCode.isEmpty()) {
            return null;
        }

        try {
            IamTenant tenant = tenantMapper.selectByCode(tenantCode);
            return convertToDTO(tenant);
        } catch (Exception e) {
            log.error("获取租户信息失败 - tenantCode: {}", tenantCode, e);
            return null;
        }
    }

    private TenantInfoDTO convertToDTO(IamTenant tenant) {
        if (tenant == null) {
            return null;
        }

        TenantInfoDTO dto = new TenantInfoDTO();
        dto.setTenantId(tenant.getId());
        dto.setTenantCode(tenant.getCode());
        dto.setTenantName(tenant.getName());
        dto.setStatus(tenant.getStatus());
//        dto.setType(tenant.getType());
//        if (tenant.getExpireTime() != null) {
//            dto.setExpireTime(tenant.getExpireTime()
//                    .atZone(java.time.ZoneId.systemDefault())
//                    .toInstant().toEpochMilli());
//        }
        return dto;
    }
}
