package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.dto.ContractTypeDTO;
import com.software.contract_system.entity.ContractType;
import com.software.contract_system.service.ContractTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合同类型控制器
 * 提供合同类型选择功能
 */
@RestController
@RequestMapping("/contract-type")
@RequiredArgsConstructor
@Tag(name = "合同类型", description = "合同类型查询")
public class ContractTypeController {

    private final ContractTypeService typeService;

    /**
     * 获取所有合同类型（分组）
     * 前端起草页面使用，显示类型选择树
     */
    @GetMapping("/grouped")
    @Operation(summary = "获取分组的合同类型", description = "按主类型分组，包含子类型列表")
    public Result<List<ContractTypeDTO>> getGroupedTypes() {
        List<ContractTypeDTO> types = typeService.getAllGrouped();
        return Result.success(types);
    }

    /**
     * 获取所有合同类型（扁平）
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有合同类型", description = "扁平列表")
    public Result<List<ContractType>> getAllTypes() {
        List<ContractType> types = typeService.getAll();
        return Result.success(types);
    }

    /**
     * 根据主类型获取子类型
     */
    @GetMapping("/subtypes/{typeCode}")
    @Operation(summary = "获取子类型列表", description = "根据主类型代码获取子类型")
    public Result<List<ContractType>> getSubTypes(@PathVariable String typeCode) {
        List<ContractType> subTypes = typeService.getByTypeCode(typeCode);
        return Result.success(subTypes);
    }

    /**
     * 获取子类型详情
     */
    @GetMapping("/detail/{subTypeCode}")
    @Operation(summary = "获取子类型详情")
    public Result<ContractType> getSubTypeDetail(@PathVariable String subTypeCode) {
        ContractType type = typeService.getBySubTypeCode(subTypeCode);
        if (type == null) {
            return Result.error("合同类型不存在");
        }
        return Result.success(type);
    }
}
