package com.intellihub.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.auth.dto.request.CreateTenantRequest;
import com.intellihub.auth.dto.request.TenantQuotaRequest;
import com.intellihub.auth.dto.request.TenantQueryRequest;
import com.intellihub.auth.dto.request.UpdateTenantRequest;
import com.intellihub.auth.dto.response.TenantResponse;
import com.intellihub.auth.entity.IamRole;
import com.intellihub.auth.entity.IamTenant;
import com.intellihub.auth.entity.IamUser;
import com.intellihub.auth.entity.IamUserRole;
import com.intellihub.auth.mapper.IamRoleMapper;
import com.intellihub.auth.mapper.IamTenantMapper;
import com.intellihub.auth.mapper.IamUserMapper;
import com.intellihub.auth.mapper.IamUserRoleMapper;
import com.intellihub.auth.service.TenantService;
import com.intellihub.constants.ResponseStatus;
import com.intellihub.exception.BusinessException;
import com.intellihub.dubbo.ApiPlatformDubboService;
import com.intellihub.dubbo.AppCenterDubboService;
import com.intellihub.page.PageData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final IamTenantMapper tenantMapper;
    private final IamUserMapper userMapper;
    private final IamRoleMapper roleMapper;
    private final IamUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @DubboReference(check = false)
    private AppCenterDubboService appCenterDubboService;

    @DubboReference(check = false)
    private ApiPlatformDubboService apiPlatformDubboService;

    @Override
    public PageData<TenantResponse> listTenants(TenantQueryRequest request) {
        LambdaQueryWrapper<IamTenant> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(IamTenant::getName, request.getKeyword())
                    .or()
                    .like(IamTenant::getCode, request.getKeyword())
            );
        }

        // 状态筛选
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(IamTenant::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(IamTenant::getCreatedAt);

        IPage<IamTenant> page = tenantMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                wrapper
        );

        List<TenantResponse> records = page.getRecords().stream()
                .map(this::convertToTenantResponse)
                .collect(Collectors.toList());

        PageData<TenantResponse> pageData = new PageData<>(page.getCurrent(), page.getSize());
        pageData.setTotal(page.getTotal());
        pageData.setRecords(records);
        return pageData;
    }

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        // 检查租户编码是否已存在
        LambdaQueryWrapper<IamTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamTenant::getCode, request.getCode());
        if (tenantMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResponseStatus.DATA_EXISTS.getCode(), "租户编码已存在");
        }

        // 创建租户
        IamTenant tenant = IamTenant.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .status("active")
                .maxUsers(request.getQuota() != null ? request.getQuota().getMaxUsers() : 100)
                .maxApps(request.getQuota() != null ? request.getQuota().getMaxApps() : 50)
                .maxApis(request.getQuota() != null ? request.getQuota().getMaxApis() : 200)
                .maxCallsPerDay(request.getQuota() != null ? request.getQuota().getMaxCallsPerDay() : 1000000L)
                .maxCallsPerMonth(request.getQuota() != null ? request.getQuota().getMaxCallsPerMonth() : 30000000L)
                .build();

        tenantMapper.insert(tenant);

        // 创建租户管理员
        IamUser adminUser = IamUser.builder()
                .tenantId(tenant.getId())
                .username(request.getAdminUsername())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .nickname("租户管理员")
                .email(request.getAdminEmail())
                .phone(request.getAdminPhone())
                .status("active")
                .loginFailCount(0)
                .build();

        userMapper.insert(adminUser);

        // 分配租户管理员角色
        LambdaQueryWrapper<IamRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(IamRole::getCode, "tenant_admin");
        IamRole tenantAdminRole = roleMapper.selectOne(roleWrapper);
        if (tenantAdminRole != null) {
            IamUserRole userRole = IamUserRole.builder()
                    .userId(adminUser.getId())
                    .roleId(tenantAdminRole.getId())
                    .build();
            userRoleMapper.insert(userRole);
        }

        return convertToTenantResponse(tenant);
    }

    @Override
    public TenantResponse getTenantById(String id) {
        IamTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }
        return convertToTenantResponse(tenant);
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(String id, UpdateTenantRequest request) {
        IamTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        if (StringUtils.hasText(request.getName())) {
            tenant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tenant.setDescription(request.getDescription());
        }
        if (request.getLogo() != null) {
            tenant.setLogo(request.getLogo());
        }
        if (request.getContactName() != null) {
            tenant.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null) {
            tenant.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            tenant.setContactEmail(request.getContactEmail());
        }

        tenantMapper.updateById(tenant);
        return convertToTenantResponse(tenant);
    }

    @Override
    @Transactional
    public void deleteTenant(String id) {
        IamTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        // 软删除
        tenant.setDeletedAt(LocalDateTime.now());
        tenantMapper.updateById(tenant);
    }

    @Override
    public void enableTenant(String id) {
        IamTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        tenant.setStatus("active");
        tenantMapper.updateById(tenant);
    }

    @Override
    public void disableTenant(String id) {
        IamTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        tenant.setStatus("inactive");
        tenantMapper.updateById(tenant);
    }

    @Override
    public TenantResponse.TenantQuotaInfo getTenantQuota(String id) {
        IamTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        return TenantResponse.TenantQuotaInfo.builder()
                .maxUsers(tenant.getMaxUsers())
                .maxApps(tenant.getMaxApps())
                .maxApis(tenant.getMaxApis())
                .maxCallsPerDay(tenant.getMaxCallsPerDay())
                .maxCallsPerMonth(tenant.getMaxCallsPerMonth())
                .build();
    }

    @Override
    @Transactional
    public void updateTenantQuota(String id, TenantQuotaRequest request) {
        IamTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        if (request.getMaxUsers() != null) {
            tenant.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxApps() != null) {
            tenant.setMaxApps(request.getMaxApps());
        }
        if (request.getMaxApis() != null) {
            tenant.setMaxApis(request.getMaxApis());
        }
        if (request.getMaxCallsPerDay() != null) {
            tenant.setMaxCallsPerDay(request.getMaxCallsPerDay());
        }
        if (request.getMaxCallsPerMonth() != null) {
            tenant.setMaxCallsPerMonth(request.getMaxCallsPerMonth());
        }

        tenantMapper.updateById(tenant);
    }

    @Override
    public TenantResponse getCurrentTenant(String tenantId) {
        return getTenantById(tenantId);
    }

    /**
     * 转换为租户响应
     */
    private TenantResponse convertToTenantResponse(IamTenant tenant) {
        // 查询租户用户数
        LambdaQueryWrapper<IamUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(IamUser::getTenantId, tenant.getId());
        Long userCount = userMapper.selectCount(userWrapper);

        // 通过 Dubbo 获取应用数和API数
        int appCount = 0;
        int apiCount = 0;
        long todayCalls = 0L;
        
        try {
            if (appCenterDubboService != null) {
                appCount = appCenterDubboService.countAppsByTenantId(tenant.getId());
            }
        } catch (Exception e) {
            log.warn("[租户统计] 获取应用数量失败: tenantId={}, error={}", tenant.getId(), e.getMessage());
        }
        
        try {
            if (apiPlatformDubboService != null) {
                apiCount = apiPlatformDubboService.countApisByTenantId(tenant.getId());
                todayCalls = apiPlatformDubboService.countTodayCallsByTenantId(tenant.getId());
            }
        } catch (Exception e) {
            log.warn("[租户统计] 获取API统计失败: tenantId={}, error={}", tenant.getId(), e.getMessage());
        }

        // 计算使用率百分比
        Double userUsagePercent = calculatePercent(userCount.intValue(), tenant.getMaxUsers());
        Double appUsagePercent = calculatePercent(appCount, tenant.getMaxApps());
        Double apiUsagePercent = calculatePercent(apiCount, tenant.getMaxApis());
        Double todayCallsPercent = calculatePercent(todayCalls, tenant.getMaxCallsPerDay());

        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .code(tenant.getCode())
                .description(tenant.getDescription())
                .logo(tenant.getLogo())
                .status(tenant.getStatus())
                .quota(TenantResponse.TenantQuotaInfo.builder()
                        .maxUsers(tenant.getMaxUsers())
                        .maxApps(tenant.getMaxApps())
                        .maxApis(tenant.getMaxApis())
                        .maxCallsPerDay(tenant.getMaxCallsPerDay())
                        .maxCallsPerMonth(tenant.getMaxCallsPerMonth())
                        .build())
                .usage(TenantResponse.TenantUsageInfo.builder()
                        .userCount(userCount.intValue())
                        .appCount(appCount)
                        .apiCount(apiCount)
                        .todayCalls(todayCalls)
                        .userUsagePercent(userUsagePercent)
                        .appUsagePercent(appUsagePercent)
                        .apiUsagePercent(apiUsagePercent)
                        .todayCallsPercent(todayCallsPercent)
                        .build())
                .contactName(tenant.getContactName())
                .contactPhone(tenant.getContactPhone())
                .contactEmail(tenant.getContactEmail())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }

    /**
     * 计算使用率百分比
     */
    private Double calculatePercent(Number used, Number max) {
        if (max == null || max.doubleValue() <= 0) {
            return 0.0;
        }
        if (used == null) {
            return 0.0;
        }
        double percent = (used.doubleValue() / max.doubleValue()) * 100;
        // 保留两位小数，最大100%
        return Math.min(100.0, Math.round(percent * 100) / 100.0);
    }
}
