package com.intellihub.aigc.controller;

import com.intellihub.aigc.entity.AigcPromptTemplate;
import com.intellihub.aigc.service.PromptTemplateService;
import com.intellihub.context.UserContextHolder;
import com.intellihub.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Prompt模板控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/aigc/templates")
@RequiredArgsConstructor
@Tag(name = "Prompt模板管理", description = "管理AI Prompt模板")
public class PromptTemplateController {

    private final PromptTemplateService templateService;

    /**
     * 创建模板
     */
    @PostMapping
    @Operation(summary = "创建模板", description = "创建新的Prompt模板")
    public ApiResponse<String> createTemplate(@Valid @RequestBody AigcPromptTemplate template) {
        String id = templateService.createTemplate(template);
        return ApiResponse.success(id);
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新模板", description = "更新已有的Prompt模板")
    public ApiResponse<Void> updateTemplate(@PathVariable String id,
                                            @Valid @RequestBody AigcPromptTemplate template) {
        template.setId(id);
        templateService.updateTemplate(template);
        return ApiResponse.success(null);
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板", description = "删除指定的Prompt模板")
    public ApiResponse<Void> deleteTemplate(@PathVariable String id) {
        templateService.deleteTemplate(id);
        return ApiResponse.success(null);
    }

    /**
     * 查询模板详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询模板详情", description = "根据ID查询Prompt模板")
    public ApiResponse<AigcPromptTemplate> getTemplate(@PathVariable String id) {
        AigcPromptTemplate template = templateService.getById(id);
        return ApiResponse.success(template);
    }

    /**
     * 查询模板列表
     */
    @GetMapping
    @Operation(summary = "查询模板列表", description = "查询当前租户的Prompt模板列表")
    public ApiResponse<List<AigcPromptTemplate>> listTemplates(@RequestParam(required = false) String type) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        List<AigcPromptTemplate> templates = templateService.listTemplates(tenantId, type);
        return ApiResponse.success(templates);
    }

    /**
     * 渲染模板
     */
    @PostMapping("/{id}/render")
    @Operation(summary = "渲染模板", description = "使用变量渲染Prompt模板")
    public ApiResponse<String> renderTemplate(@PathVariable String id,
                                              @RequestBody Map<String, Object> variables) {
        String rendered = templateService.renderTemplate(id, variables);
        return ApiResponse.success(rendered);
    }

    /**
     * 根据代码渲染模板
     */
    @PostMapping("/render/{code}")
    @Operation(summary = "根据代码渲染模板", description = "使用模板代码和变量渲染Prompt模板")
    public ApiResponse<String> renderTemplateByCode(@PathVariable String code,
                                                    @RequestBody Map<String, Object> variables) {
        String rendered = templateService.renderTemplateByCode(code, variables);
        return ApiResponse.success(rendered);
    }
}
