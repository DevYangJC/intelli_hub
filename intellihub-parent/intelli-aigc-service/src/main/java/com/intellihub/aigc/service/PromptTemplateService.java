package com.intellihub.aigc.service;

import com.intellihub.aigc.entity.AigcPromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * Prompt模板服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface PromptTemplateService {

    /**
     * 创建模板
     *
     * @param template 模板信息
     * @return 模板ID
     */
    String createTemplate(AigcPromptTemplate template);

    /**
     * 更新模板
     *
     * @param template 模板信息
     */
    void updateTemplate(AigcPromptTemplate template);

    /**
     * 删除模板
     *
     * @param id 模板ID
     */
    void deleteTemplate(String id);

    /**
     * 根据ID查询模板
     *
     * @param id 模板ID
     * @return 模板信息
     */
    AigcPromptTemplate getById(String id);

    /**
     * 根据代码查询模板
     *
     * @param code 模板代码
     * @return 模板信息
     */
    AigcPromptTemplate getByCode(String code);

    /**
     * 查询模板列表
     *
     * @param tenantId 租户ID
     * @param type 模板类型
     * @return 模板列表
     */
    List<AigcPromptTemplate> listTemplates(String tenantId, String type);

    /**
     * 渲染模板（替换变量）
     *
     * @param templateId 模板ID
     * @param variables 变量值
     * @return 渲染后的内容
     */
    String renderTemplate(String templateId, Map<String, Object> variables);

    /**
     * 根据代码渲染模板
     *
     * @param code 模板代码
     * @param variables 变量值
     * @return 渲染后的内容
     */
    String renderTemplateByCode(String code, Map<String, Object> variables);

    /**
     * 增加使用次数
     *
     * @param templateId 模板ID
     */
    void incrementUseCount(String templateId);
}
