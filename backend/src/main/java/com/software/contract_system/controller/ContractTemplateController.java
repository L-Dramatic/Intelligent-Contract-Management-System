package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.entity.ContractTemplate;
import com.software.contract_system.service.ContractTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合同模板控制器
 */
@RestController
@RequestMapping("/template")
@RequiredArgsConstructor
@Tag(name = "合同模板", description = "合同模板管理")
public class ContractTemplateController {

    private final ContractTemplateService templateService;

    /**
     * 获取默认模板
     * 前端选择合同类型后调用此接口加载模板
     */
    @GetMapping("/default/{subTypeCode}")
    @Operation(summary = "获取默认模板", description = "根据子类型代码获取默认合同模板")
    public Result<ContractTemplate> getDefaultTemplate(@PathVariable String subTypeCode) {
        ContractTemplate template = templateService.getDefaultTemplate(subTypeCode);
        return Result.success(template);
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情")
    public Result<ContractTemplate> getById(@PathVariable Long id) {
        ContractTemplate template = templateService.getById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        return Result.success(template);
    }

    /**
     * 根据主类型获取模板列表
     */
    @GetMapping("/list/type/{type}")
    @Operation(summary = "按主类型获取模板", description = "获取某主类型下的所有模板")
    public Result<List<ContractTemplate>> getByType(@PathVariable String type) {
        List<ContractTemplate> templates = templateService.getByType(type);
        return Result.success(templates);
    }

    /**
     * 根据子类型获取模板列表
     */
    @GetMapping("/list/subtype/{subTypeCode}")
    @Operation(summary = "按子类型获取模板", description = "获取某子类型的所有版本模板")
    public Result<List<ContractTemplate>> getBySubType(@PathVariable String subTypeCode) {
        List<ContractTemplate> templates = templateService.getBySubType(subTypeCode);
        return Result.success(templates);
    }

    /**
     * 获取所有模板
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有模板")
    public Result<List<ContractTemplate>> getAllTemplates() {
        List<ContractTemplate> templates = templateService.getAllActive();
        return Result.success(templates);
    }
}
