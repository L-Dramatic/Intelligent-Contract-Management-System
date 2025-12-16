package com.software.contract_system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.software.contract_system.common.Result;
import com.software.contract_system.dto.ContractDTO;
import com.software.contract_system.entity.Contract;
import com.software.contract_system.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contract")
@Tag(name = "合同管理", description = "合同的全生命周期管理")
public class ContractController {

    @Autowired
    private ContractService contractService;

    // ==========================================
    // 增 (Create)
    // ==========================================
    @PostMapping("/create")
    @Operation(summary = "创建合同", description = "创建新合同或保存草稿")
    @PreAuthorize("hasAuthority('contract:add')") // 只有拥有创建权限的人(员工/经理)能调
    public Result<Long> create(@RequestBody ContractDTO contractDTO) {
        Long contractId = contractService.createContract(contractDTO);
        return Result.success(contractId);
    }

    // ==========================================
    // 改 (Update)
    // ==========================================
    @PutMapping("/update")
    @Operation(summary = "修改合同", description = "修改草稿或被驳回的合同")
    @PreAuthorize("hasAuthority('contract:add')")
    public Result<Boolean> update(@RequestBody ContractDTO contractDTO) {
        Boolean success = contractService.updateContract(contractDTO);
        return Result.success(success);
    }

    // ==========================================
    // 查 (Read - Page)
    // ==========================================
    @GetMapping("/list")
    @Operation(summary = "分页查询合同列表", description = "支持按名称模糊搜索和类型筛选")
    @PreAuthorize("hasAuthority('contract:view')") // 所有人都可查看
    public Result<IPage<Contract>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type
    ) {
        IPage<Contract> page = contractService.getContractPage(pageNum, pageSize, name, type);
        return Result.success(page);
    }

    // ==========================================
    // 查 (Read - Detail)
    // ==========================================
    @GetMapping("/{id}")
    @Operation(summary = "获取合同详情")
    @PreAuthorize("hasAuthority('contract:view')")
    public Result<Contract> getDetail(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        if (contract == null) {
            return Result.error("合同不存在");
        }
        return Result.success(contract);
    }

    // ==========================================
    // 删 (Delete)
    // ==========================================
    @DeleteMapping("/{id}")
    @Operation(summary = "删除合同", description = "仅草稿或已驳回状态可删除")
    @PreAuthorize("hasAuthority('contract:add')")
    public Result<Boolean> delete(@PathVariable Long id) {
        // 1. 先查出来看看状态
        Contract contract = contractService.getById(id);
        if (contract == null) {
            return Result.error("合同不存在");
        }
        
        // 2. 检查状态 (0:草稿, 3:已驳回)
        if (contract.getStatus() != 0 && contract.getStatus() != 3) {
            return Result.error("只能删除草稿或已驳回的合同，当前状态不可删除");
        }

        // 3. 执行物理删除 (也可以做逻辑删除，MyBatis-Plus 配置 logic-delete 即可)
        boolean success = contractService.removeById(id);
        return Result.success(success);
    }
}