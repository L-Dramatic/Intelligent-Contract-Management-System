package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.dto.ApproveDTO;
import com.software.contract_system.dto.PreFlightCheckResult;
import com.software.contract_system.entity.*;
import com.software.contract_system.mapper.*;
import com.software.contract_system.service.ContractReviewRuleEngine;
import com.software.contract_system.service.ContractReviewService;
import com.software.contract_system.service.ScenarioMatchService;
import com.software.contract_system.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 工作流服务实现类
 * 
 * 【重要】本类已切换到新版场景审批引擎
 * 
 * 新版引擎特点：
 * 1. 基于 wf_scenario_config 和 wf_scenario_node 表
 * 2. 根据合同子类型 + 金额自动匹配审批场景
 * 3. 支持县→市自动升级（单一法人实体规则）
 * 4. 包含兜底场景 FALLBACK-DEFAULT
 */
@Slf4j
@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Autowired private ContractMapper contractMapper;
    @Autowired private WfInstanceMapper instanceMapper;
    @Autowired private WfTaskMapper taskMapper;
    @Autowired private SysUserMapper userMapper;
    
    // 审查规则引擎（Pre-Flight Check）
    @Autowired private ContractReviewRuleEngine reviewRuleEngine;
    
    // AI审查服务
    @Autowired private ContractReviewService contractReviewService;
    
    // 【新版引擎】场景匹配服务
    @Autowired private ScenarioMatchService scenarioMatchService;

    // 兜底场景ID
    private static final String FALLBACK_SCENARIO_ID = "FALLBACK-DEFAULT";

    // ==========================================
    // 1. 提交合同 (启动流程) - 【新版引擎】
    // ==========================================
    @Override
    @Transactional
    public void submitContract(Long contractId, Long userId) {
        submitContract(contractId, userId, Collections.emptyList(), false);
    }

    /**
     * 提交合同（带附件列表和用户确认标志）
     * 
     * 【新版引擎】完整流程：
     * 1. Pre-Flight Check
     * 2. 匹配审批场景（根据子类型+金额）
     * 3. 创建流程实例
     * 4. 跳过发起节点，直接派发第一个审批任务
     * 5. 触发AI审查
     */
    @Override
    @Transactional
    public void submitContract(Long contractId, Long userId, List<String> attachments, boolean userConfirmed) {
        log.info("【新版引擎】提交合同审批: contractId={}, userId={}", contractId, userId);
        
        // 1.1 检查合同状态
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) throw new RuntimeException("合同不存在");
        if (contract.getStatus() != 0 && contract.getStatus() != 3) {
            throw new RuntimeException("只有草稿或已驳回的合同可以提交");
        }

        // ========================================
        // 1.2 ★ Pre-Flight Check (审查规则引擎) ★
        // ========================================
        PreFlightCheckResult checkResult = reviewRuleEngine.preFlightCheck(contract, attachments);
        
        if (!checkResult.isPassed()) {
            log.warn("Pre-Flight Check失败: contractId={}, blockingErrors={}", 
                    contractId, checkResult.getBlockingErrors().size());
            StringBuilder errorMsg = new StringBuilder("合同检查未通过，存在以下问题：\n");
            for (PreFlightCheckResult.CheckItem error : checkResult.getBlockingErrors()) {
                errorMsg.append("- ").append(error.getMessage()).append("\n");
            }
            throw new RuntimeException(errorMsg.toString());
        }
        
        if (checkResult.isRequiresConfirmation() && !userConfirmed) {
            log.info("Pre-Flight Check需要用户确认: contractId={}", contractId);
            StringBuilder warningMsg = new StringBuilder("存在以下警告，请确认后再提交：\n");
            for (PreFlightCheckResult.CheckItem warning : checkResult.getWarnings()) {
                warningMsg.append("- ").append(warning.getMessage()).append("\n");
            }
            throw new RuntimeException("REQUIRES_CONFIRMATION:" + warningMsg.toString());
        }
        
        log.info("Pre-Flight Check通过: contractId={}", contractId);

        // ========================================
        // 1.3 ★ 【新版引擎】匹配审批场景 ★
        // ========================================
        String subTypeCode = extractSubTypeCode(contract);
        BigDecimal amount = contract.getAmount() != null ? contract.getAmount() : BigDecimal.ZERO;
        
        WfScenarioConfig scenario = scenarioMatchService.matchScenario(subTypeCode, amount);
        
        // 如果没有匹配到场景，使用兜底场景
        if (scenario == null) {
            log.warn("未匹配到审批场景，使用兜底流程: subTypeCode={}, amount={}", subTypeCode, amount);
            scenario = scenarioMatchService.getScenarioWithNodes(FALLBACK_SCENARIO_ID);
            
            if (scenario == null) {
                throw new RuntimeException("系统错误：未找到兜底审批流程，请联系管理员");
            }
        }
        
        log.info("匹配审批场景成功: scenarioId={}, subTypeName={}", 
                scenario.getScenarioId(), scenario.getSubTypeName());

        // ========================================
        // 1.4 创建流程实例（使用新版字段）
        // ========================================
        WfInstance instance = new WfInstance();
        instance.setScenarioId(scenario.getScenarioId());  // 【新版】使用场景ID
        instance.setContractId(contractId);
        instance.setCurrentNodeOrder(1);  // 【新版】从第1个节点开始（发起节点）
        instance.setStatus(WfInstance.STATUS_RUNNING);
        instance.setRequesterId(userId);
        instance.setStartTime(LocalDateTime.now());
        instanceMapper.insert(instance);

        // ========================================
        // 1.5 跳过发起节点，流转到第一个审批节点
        // ========================================
        SysUser requester = userMapper.selectById(userId);
        if (requester == null) {
            throw new RuntimeException("发起人不存在");
        }
        Long initiatorDeptId = requester.getDeptId();
        
        // 获取下一个审批节点（跳过INITIATE节点）
        toNextNodeScenario(instance, initiatorDeptId);

        // 1.6 更新合同状态 -> 审批中(1)
        contract.setStatus(1);
        contractMapper.updateById(contract);

        // ========================================
        // 1.7 ★ 异步触发AI审查 ★
        // ========================================
        try {
            log.info("异步触发AI审查: contractId={}", contractId);
            contractReviewService.triggerReviewAsync(contractId, userId, "AUTO");
        } catch (Exception e) {
            log.error("触发AI审查失败（不影响审批流程）: contractId={}", contractId, e);
        }
        
        log.info("【新版引擎】合同提交审批成功: contractId={}, instanceId={}, scenarioId={}", 
                contractId, instance.getId(), scenario.getScenarioId());
    }

    /**
     * 执行Pre-Flight Check（供前端预检用）
     */
    @Override
    public PreFlightCheckResult preFlightCheck(Long contractId, List<String> attachments) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return PreFlightCheckResult.failure("合同不存在");
        }
        return reviewRuleEngine.preFlightCheck(contract, attachments);
    }

    // ==========================================
    // 2. 执行审批 (通过/驳回) - 【新版引擎】
    // ==========================================
    @Override
    @Transactional
    public void approveTask(ApproveDTO dto, Long userId) {
        // 2.1 校验任务
        WfTask task = taskMapper.selectById(dto.getTaskId());
        if (task == null) throw new RuntimeException("任务不存在");
        if (!task.getAssigneeId().equals(userId)) throw new RuntimeException("你无权审批此任务");
        if (task.getStatus() != WfTask.STATUS_PENDING) throw new RuntimeException("任务已处理");

        // 2.2 更新任务状态
        task.setStatus(dto.getPass() ? WfTask.STATUS_APPROVED : WfTask.STATUS_REJECTED);
        task.setComment(dto.getComment());
        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);

        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        Contract contract = contractMapper.selectById(instance.getContractId());

        // 2.3 判断是新版还是旧版引擎
        if (instance.getScenarioId() != null && !instance.getScenarioId().isEmpty()) {
            // 【新版引擎】场景审批
            handleApprovalScenario(dto, instance, contract);
        } else {
            // 【旧版引擎】兼容模式（保留以防万一）
            handleApprovalLegacy(dto, instance, contract, task);
        }
    }

    /**
     * 【新版引擎】处理审批
     */
    private void handleApprovalScenario(ApproveDTO dto, WfInstance instance, Contract contract) {
        if (dto.getPass()) {
            // --- 通过：流转到下一节点 ---
            SysUser requester = userMapper.selectById(instance.getRequesterId());
            Long initiatorDeptId = requester != null ? requester.getDeptId() : null;
            
            toNextNodeScenario(instance, initiatorDeptId);
        } else {
            // --- 驳回：结束流程 ---
            instance.setStatus(WfInstance.STATUS_REJECTED);
            instance.setEndTime(LocalDateTime.now());
            instanceMapper.updateById(instance);
            
            contract.setStatus(3); // 合同驳回
            contractMapper.updateById(contract);
            
            log.info("【新版引擎】合同审批被驳回: contractId={}", contract.getId());
        }
    }

    /**
     * 【旧版引擎】处理审批（兼容模式）
     */
    private void handleApprovalLegacy(ApproveDTO dto, WfInstance instance, Contract contract, WfTask task) {
        if (dto.getPass()) {
            // 旧版处理逻辑 - 简化处理，直接完成
            log.warn("使用旧版引擎处理审批（兼容模式），建议迁移到新版引擎");
            instance.setStatus(WfInstance.STATUS_COMPLETED);
            instance.setEndTime(LocalDateTime.now());
            instanceMapper.updateById(instance);
            
            contract.setStatus(2); // 已生效
            contractMapper.updateById(contract);
        } else {
            instance.setStatus(WfInstance.STATUS_REJECTED);
            instanceMapper.updateById(instance);
            
            contract.setStatus(3); // 驳回
            contractMapper.updateById(contract);
        }
    }

    // ==========================================
    // 3. 查询任务
    // ==========================================
    @Override
    public List<WfTask> getMyTasks(Long userId) {
        return taskMapper.selectList(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getAssigneeId, userId)
                .eq(WfTask::getStatus, WfTask.STATUS_PENDING)
                .orderByDesc(WfTask::getCreateTime));
    }

    // ==========================================
    // 【新版引擎】核心辅助方法：流转到下一场景节点
    // ==========================================
    private void toNextNodeScenario(WfInstance instance, Long initiatorDeptId) {
        String scenarioId = instance.getScenarioId();
        int currentOrder = instance.getCurrentNodeOrder();
        
        // 获取下一个节点
        WfScenarioNode nextNode = scenarioMatchService.getNextNode(scenarioId, currentOrder);
        
        if (nextNode == null) {
            // 没有下一个节点，流程结束
            instance.setStatus(WfInstance.STATUS_COMPLETED);
            instance.setEndTime(LocalDateTime.now());
            instanceMapper.updateById(instance);
            
            // 更新合同状态 -> 已生效(2)
            Contract contract = contractMapper.selectById(instance.getContractId());
            contract.setStatus(2);
            contractMapper.updateById(contract);
            
            log.info("【新版引擎】流程完成: instanceId={}, scenarioId={}", 
                    instance.getId(), scenarioId);
            return;
        }
        
        // 跳过INITIATE节点（发起人节点不需要派发任务）
        if (WfScenarioNode.ACTION_INITIATE.equals(nextNode.getActionType())) {
            instance.setCurrentNodeOrder(nextNode.getNodeOrder());
            instanceMapper.updateById(instance);
            // 递归获取下一个节点
            toNextNodeScenario(instance, initiatorDeptId);
            return;
        }
        
        // 更新实例的当前节点
        instance.setCurrentNodeOrder(nextNode.getNodeOrder());
        instanceMapper.updateById(instance);
        
        // 匹配审批人
        SysUser approver = scenarioMatchService.matchApprover(nextNode, initiatorDeptId);
        
        if (approver == null) {
            // 如果找不到审批人，记录警告并尝试使用部门经理兜底
            log.warn("找不到审批人，尝试兜底匹配: scenarioId={}, nodeOrder={}, roleCode={}", 
                    scenarioId, nextNode.getNodeOrder(), nextNode.getRoleCode());
            
            // 兜底：尝试找该部门的任意部门经理
            approver = findFallbackApprover(initiatorDeptId);
            
            if (approver == null) {
                throw new RuntimeException(String.format(
                    "流程流转失败：找不到角色 %s 的审批人（节点%d）", 
                    nextNode.getRoleCode(), nextNode.getNodeOrder()));
            }
        }
        
        // 创建审批任务
        WfTask newTask = new WfTask();
        newTask.setInstanceId(instance.getId());
        newTask.setScenarioNodeId(nextNode.getId());  // 【新版】使用场景节点ID
        newTask.setAssigneeId(approver.getId());
        newTask.setStatus(WfTask.STATUS_PENDING);
        newTask.setCreateTime(LocalDateTime.now());
        taskMapper.insert(newTask);
        
        log.info("【新版引擎】派发审批任务: instanceId={}, nodeOrder={}, roleCode={}, assignee={}", 
                instance.getId(), nextNode.getNodeOrder(), nextNode.getRoleCode(), approver.getRealName());
    }

    /**
     * 兜底查找审批人（当精确匹配失败时）
     */
    private SysUser findFallbackApprover(Long deptId) {
        // 尝试找该部门下的任意部门经理
        List<SysUser> managers = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPrimaryRole, SysRole.ROLE_DEPT_MANAGER)
                .eq(SysUser::getIsActive, 1)
                .last("LIMIT 1"));
        
        return managers.isEmpty() ? null : managers.get(0);
    }

    /**
     * 从合同中提取子类型代码
     * 
     * 解析逻辑：
     * 1. 优先从 attributes.subTypeCode 读取
     * 2. 其次从 type 字段解析（如 TYPE_A → A1）
     * 3. 兜底返回 null
     */
    private String extractSubTypeCode(Contract contract) {
        // 1. 从扩展属性中读取
        Map<String, Object> attrs = contract.getAttributes();
        if (attrs != null && attrs.containsKey("subTypeCode")) {
            return String.valueOf(attrs.get("subTypeCode"));
        }
        
        // 2. 从 type 字段解析
        String type = contract.getType();
        if (type == null) {
            return null;
        }
        
        // 映射主类型到默认子类型
        // TYPE_A → A1, TYPE_B → B1, TYPE_C → C1
        switch (type) {
            case "TYPE_A":
                // 检查是否有更具体的子类型信息
                if (attrs != null) {
                    if (attrs.containsKey("isMinorRepair") && Boolean.TRUE.equals(attrs.get("isMinorRepair"))) {
                        return "A3"; // 零星维修
                    }
                    if (attrs.containsKey("isDecoration") && Boolean.TRUE.equals(attrs.get("isDecoration"))) {
                        return "A2"; // 装修工程
                    }
                }
                return "A1"; // 默认：土建工程
                
            case "TYPE_B":
                if (attrs != null) {
                    if (attrs.containsKey("isEmergency") && Boolean.TRUE.equals(attrs.get("isEmergency"))) {
                        return "B4"; // 应急保障
                    }
                    if (attrs.containsKey("isBroadband") && Boolean.TRUE.equals(attrs.get("isBroadband"))) {
                        return "B3"; // 家宽代维
                    }
                    if (attrs.containsKey("isBaseStation") && Boolean.TRUE.equals(attrs.get("isBaseStation"))) {
                        return "B2"; // 基站代维
                    }
                }
                return "B1"; // 默认：光缆代维
                
            case "TYPE_C":
                if (attrs != null) {
                    if (attrs.containsKey("isDICT") && Boolean.TRUE.equals(attrs.get("isDICT"))) {
                        return "C3"; // DICT集成
                    }
                    if (attrs.containsKey("isSoftwarePurchase") && Boolean.TRUE.equals(attrs.get("isSoftwarePurchase"))) {
                        return "C2"; // 商用软件采购
                    }
                }
                return "C1"; // 默认：定制开发
                
            default:
                return null; // 未知类型，将使用兜底场景
        }
    }
}
