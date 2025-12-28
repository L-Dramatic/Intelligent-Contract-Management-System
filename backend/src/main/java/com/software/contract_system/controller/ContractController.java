package com.software.contract_system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.software.contract_system.common.BusinessException;
import com.software.contract_system.common.ContractStatus;
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
    // @PreAuthorize("hasAuthority('contract:add')") // 开发环境临时注释，允许所有已登录用户创建
    public Result<Long> create(@RequestBody ContractDTO contractDTO) {
        Long contractId = contractService.createContract(contractDTO);
        return Result.success(contractId);
    }

    // ==========================================
    // 改 (Update)
    // ==========================================
    @PutMapping("/update")
    @Operation(summary = "修改合同", description = "修改草稿或被驳回的合同")
    // @PreAuthorize("hasAuthority('contract:add')") // 开发环境临时注释，允许所有已登录用户更新
    public Result<Boolean> update(@RequestBody ContractDTO contractDTO) {
        Boolean success = contractService.updateContract(contractDTO);
        return Result.success(success);
    }

    // ==========================================
    // 查 (Read - Page)
    // ==========================================
    @GetMapping("/list")
    @Operation(summary = "分页查询合同列表", description = "支持按名称模糊搜索和类型筛选")
    // @PreAuthorize("hasAuthority('contract:view')") // 开发环境临时注释，允许所有已登录用户查看
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
    // 查 (Read - My Contracts)
    // ==========================================
    @GetMapping("/my")
    @Operation(summary = "获取我的合同列表", description = "获取当前登录用户创建的合同")
    // @PreAuthorize("hasAuthority('contract:view')") // 开发环境临时注释
    public Result<IPage<Contract>> myContracts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status
    ) {
        IPage<Contract> page = contractService.getMyContracts(pageNum, pageSize, status);
        return Result.success(page);
    }

    // ==========================================
    // 查 (Read - Detail)
    // ==========================================
    @GetMapping("/{id}")
    @Operation(summary = "获取合同详情")
    // @PreAuthorize("hasAuthority('contract:view')") // 开发环境临时注释
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
    // @PreAuthorize("hasAuthority('contract:add')") // 开发环境临时注释
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = contractService.deleteContract(id);
        return Result.success(success);
    }
    
    // ==========================================
    // 调试接口（仅开发环境使用）
    // ==========================================
    @GetMapping("/debug/all")
    @Operation(summary = "[调试]查询所有合同", description = "无需登录，用于调试数据")
    public Result<?> debugAllContracts() {
        var allContracts = contractService.list();
        System.out.println("[DEBUG] 数据库中共有 " + allContracts.size() + " 条合同");
        for (var c : allContracts) {
            System.out.println("  - ID:" + c.getId() + " 创建人:" + c.getCreatorId() + " 状态:" + c.getStatus() + " 名称:" + c.getName());
        }
        return Result.success(allContracts);
    }
    
    @Autowired
    private com.software.contract_system.mapper.WfInstanceMapper wfInstanceMapper;
    
    @Autowired
    private com.software.contract_system.mapper.WfTaskMapper wfTaskMapper;
    
    @Autowired
    private com.software.contract_system.mapper.SysUserMapper sysUserMapper;
    
    @GetMapping("/debug/workflow")
    @Operation(summary = "[调试]查询所有流程数据", description = "无需登录，用于调试数据")
    public Result<?> debugWorkflow() {
        var instances = wfInstanceMapper.selectList(null);
        var tasks = wfTaskMapper.selectList(null);
        
        System.out.println("[DEBUG] 流程实例数: " + instances.size());
        for (var inst : instances) {
            System.out.println("  Instance: id=" + inst.getId() + " contractId=" + inst.getContractId() + 
                " status=" + inst.getStatus() + " requesterId=" + inst.getRequesterId());
        }
        
        System.out.println("[DEBUG] 任务数: " + tasks.size());
        for (var task : tasks) {
            System.out.println("  Task: id=" + task.getId() + " instanceId=" + task.getInstanceId() + 
                " assigneeId=" + task.getAssigneeId() + " status=" + task.getStatus());
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("instances", instances);
        result.put("tasks", tasks);
        return Result.success(result);
    }
    
    @GetMapping("/debug/users/{ids}")
    @Operation(summary = "[调试]查询指定用户", description = "无需登录，用于调试数据")
    public Result<?> debugUsers(@PathVariable String ids) {
        var idList = java.util.Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(java.util.stream.Collectors.toList());
        var users = sysUserMapper.selectBatchIds(idList);
        return Result.success(users.stream().map(u -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("realName", u.getRealName());
            m.put("deptId", u.getDeptId());
            return m;
        }).collect(java.util.stream.Collectors.toList()));
    }
}