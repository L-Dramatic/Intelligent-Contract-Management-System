package com.software.contract_system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.software.contract_system.common.Result;
import com.software.contract_system.dto.ContractChangeDTO;
import com.software.contract_system.dto.ContractChangeVO;
import com.software.contract_system.entity.ContractChange;
import com.software.contract_system.service.ContractChangeService;
import com.software.contract_system.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 合同变更管理控制器
 * 
 * 实现UC-07用例：
 * - 发起变更申请
 * - 变更对比视图
 * - 重大变更判定（金额>20%）
 * - 变更审批流程
 * - 版本历史追踪
 */
@Slf4j
@RestController
@RequestMapping("/contract-change")
@RequiredArgsConstructor
@Tag(name = "合同变更管理", description = "合同变更申请、审批、历史追踪")
public class ContractChangeController {

    private final ContractChangeService changeService;
    private final SecurityUtils securityUtils;

    /**
     * 创建变更申请（草稿）
     */
    @PostMapping("/create")
    @Operation(summary = "创建变更申请", description = "创建合同变更申请草稿")
    public Result<ContractChange> createChange(@RequestBody ContractChangeDTO dto) {
        Long userId = securityUtils.getCurrentUserId();
        ContractChange change = changeService.createChange(dto, userId);
        return Result.success(change);
    }

    /**
     * 提交变更审批
     */
    @PostMapping("/{changeId}/submit")
    @Operation(summary = "提交变更审批", description = "将变更申请提交审批流程")
    public Result<Void> submitChange(@PathVariable Long changeId) {
        Long userId = securityUtils.getCurrentUserId();
        changeService.submitChange(changeId, userId);
        return Result.success();
    }

    /**
     * 撤销变更申请
     */
    @PostMapping("/{changeId}/cancel")
    @Operation(summary = "撤销变更申请", description = "撤销草稿或审批中的变更申请")
    public Result<Void> cancelChange(@PathVariable Long changeId) {
        Long userId = securityUtils.getCurrentUserId();
        changeService.cancelChange(changeId, userId);
        return Result.success();
    }

    /**
     * 获取变更详情
     */
    @GetMapping("/{changeId}")
    @Operation(summary = "获取变更详情", description = "获取变更申请的详细信息，包含对比数据")
    public Result<ContractChangeVO> getChangeDetail(@PathVariable Long changeId) {
        ContractChangeVO vo = changeService.getChangeDetail(changeId);
        return Result.success(vo);
    }

    /**
     * 获取合同的变更历史
     */
    @GetMapping("/history/{contractId}")
    @Operation(summary = "获取变更历史", description = "获取指定合同的所有变更历史记录")
    public Result<List<ContractChangeVO>> getChangeHistory(@PathVariable Long contractId) {
        List<ContractChangeVO> history = changeService.getChangeHistory(contractId);
        return Result.success(history);
    }

    /**
     * 分页查询变更申请
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询变更申请", description = "支持按合同ID、状态、发起人筛选")
    public Result<IPage<ContractChangeVO>> listChanges(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long initiatorId) {
        Page<ContractChange> page = new Page<>(pageNum, pageSize);
        IPage<ContractChangeVO> result = changeService.pageChanges(page, contractId, status, initiatorId);
        return Result.success(result);
    }

    /**
     * 我发起的变更申请
     */
    @GetMapping("/my")
    @Operation(summary = "我的变更申请", description = "查询当前用户发起的变更申请")
    public Result<IPage<ContractChangeVO>> myChanges(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Long userId = securityUtils.getCurrentUserId();
        Page<ContractChange> page = new Page<>(pageNum, pageSize);
        IPage<ContractChangeVO> result = changeService.pageChanges(page, null, status, userId);
        return Result.success(result);
    }

    /**
     * 检查合同是否可以发起变更
     */
    @GetMapping("/can-change/{contractId}")
    @Operation(summary = "检查是否可变更", description = "检查合同当前是否允许发起变更申请")
    public Result<Map<String, Object>> canCreateChange(@PathVariable Long contractId) {
        boolean canChange = changeService.canCreateChange(contractId);
        return Result.success(Map.of(
                "canChange", canChange,
                "message", canChange ? "可以发起变更" : "当前不可变更，可能已有变更在审批中或合同状态不允许"
        ));
    }

    /**
     * 预检：判断是否为重大变更
     */
    @PostMapping("/check-major")
    @Operation(summary = "重大变更预检", description = "检查变更是否会被判定为重大变更（金额>20%或技术方案变更）")
    public Result<Map<String, Object>> checkMajorChange(@RequestBody ContractChangeDTO dto) {
        boolean isMajor = changeService.isMajorChange(dto, dto.getContractId());
        return Result.success(Map.of(
                "isMajorChange", isMajor,
                "message", isMajor ? "此变更将被判定为重大变更，需要增加法务会签" : "普通变更"
        ));
    }
    
    /**
     * 获取当前用户可变更的合同列表
     * 返回用户有权限变更的已生效合同（合同创建者或审批流程参与者）
     */
    @GetMapping("/changeable-contracts")
    @Operation(summary = "可变更合同列表", description = "获取当前用户有权限发起变更的合同列表")
    public Result<List<com.software.contract_system.entity.Contract>> getChangeableContracts() {
        Long userId = securityUtils.getCurrentUserId();
        List<com.software.contract_system.entity.Contract> contracts = changeService.getChangeableContracts(userId);
        return Result.success(contracts);
    }
}

