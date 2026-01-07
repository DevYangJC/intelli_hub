package com.intellihub.aigc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.intellihub.aigc.entity.AigcPromptTemplate;
import com.intellihub.aigc.mapper.AigcPromptTemplateMapper;
import com.intellihub.aigc.service.PromptTemplateService;
import com.intellihub.context.UserContextHolder;
import com.intellihub.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prompt模板服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final AigcPromptTemplateMapper templateMapper;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    @Override
    public String createTemplate(AigcPromptTemplate template) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();

        // 检查代码唯一性
        LambdaQueryWrapper<AigcPromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcPromptTemplate::getCode, template.getCode())
                .eq(AigcPromptTemplate::getTenantId, tenantId);
        Long count = templateMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("模板代码已存在");
        }

        template.setTenantId(tenantId);
        template.setCreatedBy(userId);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedBy(userId);
        template.setUpdatedAt(LocalDateTime.now());
        template.setUseCount(0L);
        template.setStatus(1);
        template.setDeleted(0);

        templateMapper.insert(template);
        log.info("创建Prompt模板 - code: {}, name: {}", template.getCode(), template.getName());

        return template.getId();
    }

    @Override
    public void updateTemplate(AigcPromptTemplate template) {
        String userId = UserContextHolder.getCurrentUserId();

        AigcPromptTemplate existing = templateMapper.selectById(template.getId());
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }

        template.setUpdatedBy(userId);
        template.setUpdatedAt(LocalDateTime.now());

        templateMapper.updateById(template);
        log.info("更新Prompt模板 - id: {}, code: {}", template.getId(), template.getCode());
    }

    @Override
    public void deleteTemplate(String id) {
        AigcPromptTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        templateMapper.deleteById(id);
        log.info("删除Prompt模板 - id: {}, code: {}", id, template.getCode());
    }

    @Override
    public AigcPromptTemplate getById(String id) {
        return templateMapper.selectById(id);
    }

    @Override
    public AigcPromptTemplate getByCode(String code) {
        String tenantId = UserContextHolder.getCurrentTenantId();

        LambdaQueryWrapper<AigcPromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcPromptTemplate::getCode, code)
                .eq(AigcPromptTemplate::getTenantId, tenantId)
                .eq(AigcPromptTemplate::getStatus, 1);

        return templateMapper.selectOne(wrapper);
    }

    @Override
    public List<AigcPromptTemplate> listTemplates(String tenantId, String type) {
        LambdaQueryWrapper<AigcPromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcPromptTemplate::getTenantId, tenantId)
                .eq(AigcPromptTemplate::getStatus, 1);

        if (StringUtils.hasText(type)) {
            wrapper.eq(AigcPromptTemplate::getType, type);
        }

        wrapper.orderByDesc(AigcPromptTemplate::getCreatedAt);

        return templateMapper.selectList(wrapper);
    }

    @Override
    public String renderTemplate(String templateId, Map<String, Object> variables) {
        AigcPromptTemplate template = getById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        if (template.getStatus() != 1) {
            throw new BusinessException("模板已禁用");
        }

        String rendered = replaceVariables(template.getContent(), variables);

        // 异步增加使用次数
        incrementUseCount(templateId);

        return rendered;
    }

    @Override
    public String renderTemplateByCode(String code, Map<String, Object> variables) {
        AigcPromptTemplate template = getByCode(code);
        if (template == null) {
            throw new BusinessException("模板不存在: " + code);
        }

        String rendered = replaceVariables(template.getContent(), variables);

        // 异步增加使用次数
        incrementUseCount(template.getId());

        return rendered;
    }

    @Override
    public void incrementUseCount(String templateId) {
        LambdaUpdateWrapper<AigcPromptTemplate> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AigcPromptTemplate::getId, templateId)
                .setSql("use_count = use_count + 1");

        templateMapper.update(null, wrapper);
    }

    /**
     * 替换模板中的变量
     */
    private String replaceVariables(String content, Map<String, Object> variables) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        if (variables == null || variables.isEmpty()) {
            return content;
        }

        String result = content;
        Matcher matcher = VARIABLE_PATTERN.matcher(content);

        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = variables.get(varName);

            if (value != null) {
                result = result.replace("{" + varName + "}", String.valueOf(value));
            }
        }

        return result;
    }
}
