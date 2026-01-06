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
            @RequestParam(required = false) String type) {
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
            @RequestParam(required = false) Integer status) {
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
    // AI 风险审查 (直接调用 DeepSeek)
    // ==========================================
    @Autowired
    private org.springframework.web.reactive.function.client.WebClient aiWebClient;

    @PostMapping("/{id}/ai/review")
    @Operation(summary = "AI风险审查", description = "调用DeepSeek AI对合同进行风险审查")
    public Result<?> aiReview(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        if (contract == null) {
            return Result.error("合同不存在");
        }

        try {
            // 直接调用 Python AI 服务的 /api/check 端点
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("clause_content", contract.getContent());
            requestBody.put("contract_type", contract.getType());

            var response = aiWebClient.post()
                    .uri("/api/check")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .timeout(java.time.Duration.ofSeconds(120))
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                String analysis = (String) response.get("analysis");

                // 解析 AI 返回的分析结果，构建前端需要的格式
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("riskLevel", extractRiskLevel(analysis));
                result.put("score", extractScore(analysis));
                result.put("highRiskItems", extractRiskItems(analysis, "高风险"));
                result.put("mediumRiskItems", extractRiskItems(analysis, "中风险"));
                result.put("lowRiskItems", extractRiskItems(analysis, "低风险"));
                result.put("goodClauses", extractGoodClauses(analysis));
                result.put("rawAnalysis", analysis); // 原始分析内容

                return Result.success(result);
            } else {
                String error = response != null ? (String) response.get("error") : "AI服务无响应";
                return Result.error("AI审查失败: " + error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI服务调用失败: " + e.getMessage());
        }
    }

    // 提取风险等级
    private String extractRiskLevel(String analysis) {
        if (analysis.contains("HIGH") || analysis.contains("高风险"))
            return "HIGH";
        if (analysis.contains("MEDIUM") || analysis.contains("🟡"))
            return "MEDIUM";
        return "LOW";
    }

    // 提取合规评分
    private int extractScore(String analysis) {
        // 尝试从文本中提取分数
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{1,3})\\s*分");
        java.util.regex.Matcher matcher = pattern.matcher(analysis);
        if (matcher.find()) {
            try {
                int score = Integer.parseInt(matcher.group(1));
                return Math.min(100, Math.max(0, score));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        // 默认根据风险等级估算
        if (analysis.contains("HIGH"))
            return 60;
        if (analysis.contains("MEDIUM"))
            return 75;
        return 85;
    }

    // 提取风险项
    private java.util.List<java.util.Map<String, String>> extractRiskItems(String analysis, String level) {
        java.util.List<java.util.Map<String, String>> items = new java.util.ArrayList<>();
        // 简化处理：根据关键词拆分
        String[] lines = analysis.split("\n");
        boolean inSection = false;

        for (String line : lines) {
            if (line.contains(level)) {
                inSection = true;
                continue;
            }
            if (inSection && (line.startsWith("##") || line.startsWith("🔴") || line.startsWith("🟡")
                    || line.startsWith("🟢") || line.startsWith("✅") || line.startsWith("📊"))) {
                inSection = false;
            }
            if (inSection && line.startsWith("-") && line.length() > 3) {
                java.util.Map<String, String> item = new java.util.HashMap<>();
                item.put("issue", line.substring(1).trim());
                item.put("suggestion", "请参阅详细分析");
                items.add(item);
            }
        }
        return items;
    }

    // 提取优质条款
    private java.util.List<String> extractGoodClauses(String analysis) {
        java.util.List<String> clauses = new java.util.ArrayList<>();
        String[] lines = analysis.split("\n");
        boolean inSection = false;

        for (String line : lines) {
            if (line.contains("优质条款") || line.contains("✅")) {
                inSection = true;
                continue;
            }
            if (inSection && (line.startsWith("##") || line.startsWith("📊"))) {
                inSection = false;
            }
            if (inSection && line.startsWith("-") && line.length() > 3) {
                clauses.add(line.substring(1).trim());
            }
        }
        return clauses;
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
            System.out.println(
                    "  - ID:" + c.getId() + " 创建人:" + c.getCreatorId() + " 状态:" + c.getStatus() + " 名称:" + c.getName());
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