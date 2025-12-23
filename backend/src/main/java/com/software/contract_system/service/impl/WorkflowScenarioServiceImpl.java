package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.entity.*;
import com.software.contract_system.mapper.*;
import com.software.contract_system.service.ScenarioMatchService;
import com.software.contract_system.service.WorkflowScenarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于场景的审批流程服务实现类
 * 核心实现：根据合同类型+金额自动匹配审批场景，动态派发任务
 */
@Slf4j
@Service
public class WorkflowScenarioServiceImpl implements WorkflowScenarioService {
    
    @Autowired
    private ScenarioMatchService scenarioMatchService;
    
    @Autowired
    private WfInstanceMapper instanceMapper;
    
    @Autowired
    private WfTaskMapper taskMapper;
    
    @Autowired
    private WfScenarioNodeMapper scenarioNodeMapper;
    
    @Autowired
    private ContractMapper contractMapper;
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Override
    @Transactional
    public WfInstance startWorkflow(Contract contract, Long initiatorId) {
        log.info("启动审批流程: contractId={}, initiatorId={}, type={}, amount={}", 
                contract.getId(), initiatorId, contract.getType(), contract.getAmount());
        
        // 1. 匹配审批场景
        WfScenarioConfig scenario = matchScenario(contract);
        if (scenario == null) {
            throw new RuntimeException("未找到匹配的审批场景，请检查合同类型和金额配置");
        }
        
        log.info("匹配到审批场景: scenarioId={}, name={}", 
                scenario.getScenarioId(), scenario.getSubTypeName());
        
        // 2. 创建流程实例
        WfInstance instance = new WfInstance();
        instance.setScenarioId(scenario.getScenarioId());
        instance.setContractId(contract.getId());
        instance.setCurrentNodeOrder(0); // 初始为0，表示还未开始
        instance.setStatus(WfInstance.STATUS_RUNNING);
        instance.setRequesterId(initiatorId);
        instance.setStartTime(LocalDateTime.now());
        instanceMapper.insert(instance);
        
        log.info("创建流程实例: instanceId={}", instance.getId());
        
        // 3. 获取发起人所属部门
        SysUser initiator = userMapper.selectById(initiatorId);
        if (initiator == null || initiator.getDeptId() == null) {
            throw new RuntimeException("发起人信息不完整，请检查用户部门配置");
        }
        
        // 4. 流转到第一个审批节点（跳过发起节点）
        advanceToNextNode(instance, initiator.getDeptId());
        
        return instance;
    }
    
    @Override
    @Transactional
    public void approve(Long taskId, Long approverId, String comment) {
        log.info("审批通过: taskId={}, approverId={}", taskId, approverId);
        
        // 1. 校验任务
        WfTask task = validateAndGetTask(taskId, approverId);
        
        // 2. 更新任务状态
        task.setStatus(WfTask.STATUS_APPROVED);
        task.setComment(comment);
        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);
        
        // 3. 获取流程实例
        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        
        // 4. 获取发起人部门（用于匹配下一节点的审批人）
        SysUser requester = userMapper.selectById(instance.getRequesterId());
        
        // 5. 流转到下一节点
        advanceToNextNode(instance, requester.getDeptId());
    }
    
    @Override
    @Transactional
    public void reject(Long taskId, Long approverId, String comment) {
        log.info("审批驳回: taskId={}, approverId={}", taskId, approverId);
        
        // 1. 校验任务
        WfTask task = validateAndGetTask(taskId, approverId);
        
        // 2. 更新任务状态
        task.setStatus(WfTask.STATUS_REJECTED);
        task.setComment(comment);
        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);
        
        // 3. 更新流程实例状态
        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        instance.setStatus(WfInstance.STATUS_REJECTED);
        instance.setEndTime(LocalDateTime.now());
        instanceMapper.updateById(instance);
        
        // 4. 更新合同状态
        Contract contract = contractMapper.selectById(instance.getContractId());
        contract.setStatus(3); // 已驳回
        contractMapper.updateById(contract);
        
        log.info("审批驳回完成: contractId={}", contract.getId());
    }
    
    @Override
    public List<WfTask> getPendingTasks(Long userId) {
        return taskMapper.selectList(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getAssigneeId, userId)
                .eq(WfTask::getStatus, WfTask.STATUS_PENDING)
                .orderByDesc(WfTask::getCreateTime));
    }
    
    @Override
    public List<WfTask> getCompletedTasks(Long userId) {
        return taskMapper.selectList(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getAssigneeId, userId)
                .ne(WfTask::getStatus, WfTask.STATUS_PENDING)
                .orderByDesc(WfTask::getFinishTime));
    }
    
    @Override
    public WfInstance getWorkflowProgress(Long contractId) {
        return instanceMapper.selectOne(new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getContractId, contractId)
                .orderByDesc(WfInstance::getStartTime)
                .last("LIMIT 1"));
    }
    
    @Override
    public List<WfTask> getApprovalHistory(Long contractId) {
        // 先获取流程实例
        WfInstance instance = getWorkflowProgress(contractId);
        if (instance == null) {
            return List.of();
        }
        
        return taskMapper.selectList(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, instance.getId())
                .orderByAsc(WfTask::getCreateTime));
    }
    
    @Override
    @Transactional
    public void cancelWorkflow(Long instanceId, Long userId) {
        WfInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        
        // 只有发起人可以撤销
        if (!instance.getRequesterId().equals(userId)) {
            throw new RuntimeException("只有发起人可以撤销流程");
        }
        
        // 只有进行中的流程可以撤销
        if (instance.getStatus() != WfInstance.STATUS_RUNNING) {
            throw new RuntimeException("只有进行中的流程可以撤销");
        }
        
        // 更新流程状态
        instance.setStatus(WfInstance.STATUS_CANCELLED);
        instance.setEndTime(LocalDateTime.now());
        instanceMapper.updateById(instance);
        
        // 取消所有待办任务
        WfTask cancelTask = new WfTask();
        cancelTask.setStatus(WfTask.STATUS_VETOED);
        cancelTask.setFinishTime(LocalDateTime.now());
        cancelTask.setComment("流程已撤销");
        taskMapper.update(cancelTask, new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, instanceId)
                .eq(WfTask::getStatus, WfTask.STATUS_PENDING));
        
        // 更新合同状态回草稿
        Contract contract = contractMapper.selectById(instance.getContractId());
        contract.setStatus(0); // 草稿
        contractMapper.updateById(contract);
        
        log.info("流程已撤销: instanceId={}, contractId={}", instanceId, contract.getId());
    }
    
    @Override
    public WfScenarioConfig checkSubmittable(Contract contract) {
        return matchScenario(contract);
    }
    
    @Override
    public WfScenarioConfig getMatchedScenario(Contract contract) {
        WfScenarioConfig scenario = matchScenario(contract);
        if (scenario != null) {
            // 加载节点列表
            scenario.setNodes(scenarioMatchService.getScenarioNodes(scenario.getScenarioId()));
        }
        return scenario;
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 匹配审批场景
     */
    private WfScenarioConfig matchScenario(Contract contract) {
        // 从合同类型中提取子类型代码
        String subTypeCode = extractSubTypeCode(contract.getType());
        if (subTypeCode == null) {
            log.warn("无法从合同类型提取子类型代码: type={}", contract.getType());
            return null;
        }
        
        BigDecimal amount = contract.getAmount();
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        
        return scenarioMatchService.matchScenario(subTypeCode, amount);
    }
    
    /**
     * 从合同类型提取子类型代码
     * 支持格式：
     * - 直接的子类型代码：A1, B2, C3
     * - 带前缀的类型：TYPE_A1, CONSTRUCTION_A1
     */
    private String extractSubTypeCode(String contractType) {
        if (contractType == null) {
            return null;
        }
        
        // 如果是标准子类型代码（A1, A2, B1, B2, C1, C2, C3等）
        if (contractType.matches("^[ABC][1-4]$")) {
            return contractType;
        }
        
        // 尝试从类型名称中提取（如 "土建工程" -> A1）
        // 这里可以根据实际需求扩展映射逻辑
        switch (contractType) {
            case "土建工程":
            case "CONSTRUCTION":
                return "A1";
            case "装修工程":
            case "RENOVATION":
                return "A2";
            case "零星维修":
            case "MAINTENANCE":
                return "A3";
            case "光缆代维":
            case "FIBER_MAINTENANCE":
                return "B1";
            case "基站代维":
            case "BASE_STATION":
                return "B2";
            case "家宽代维":
            case "BROADBAND":
                return "B3";
            case "应急保障":
            case "EMERGENCY":
                return "B4";
            case "定制开发":
            case "CUSTOM_DEV":
                return "C1";
            case "软件采购":
            case "SOFTWARE":
                return "C2";
            case "DICT集成":
            case "DICT":
                return "C3";
            default:
                // 尝试匹配末尾的子类型代码
                if (contractType.length() >= 2) {
                    String suffix = contractType.substring(contractType.length() - 2);
                    if (suffix.matches("^[ABC][1-4]$")) {
                        return suffix;
                    }
                }
                return null;
        }
    }
    
    /**
     * 校验任务并获取任务实体
     */
    private WfTask validateAndGetTask(Long taskId, Long approverId) {
        WfTask task = taskMapper.selectById(taskId);
        
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!task.getAssigneeId().equals(approverId)) {
            throw new RuntimeException("你无权审批此任务");
        }
        
        if (task.getStatus() != WfTask.STATUS_PENDING) {
            throw new RuntimeException("任务已处理");
        }
        
        return task;
    }
    
    /**
     * 流转到下一节点
     */
    private void advanceToNextNode(WfInstance instance, Long initiatorDeptId) {
        String scenarioId = instance.getScenarioId();
        int currentOrder = instance.getCurrentNodeOrder();
        
        // 获取下一个节点
        WfScenarioNode nextNode = scenarioMatchService.getNextNode(scenarioId, currentOrder);
        
        if (nextNode == null) {
            // 没有下一节点，流程结束
            completeWorkflow(instance);
            return;
        }
        
        log.info("流转到节点: scenarioId={}, nodeOrder={}, roleCode={}", 
                scenarioId, nextNode.getNodeOrder(), nextNode.getRoleCode());
        
        // 更新实例的当前节点
        instance.setCurrentNodeOrder(nextNode.getNodeOrder());
        instanceMapper.updateById(instance);
        
        // 匹配审批人
        SysUser approver = scenarioMatchService.matchApprover(nextNode, initiatorDeptId);
        
        if (approver == null) {
            log.error("无法匹配审批人: nodeOrder={}, roleCode={}, initiatorDeptId={}", 
                    nextNode.getNodeOrder(), nextNode.getRoleCode(), initiatorDeptId);
            throw new RuntimeException("无法找到审批人，请检查组织架构和角色配置。节点角色: " + nextNode.getRoleCode());
        }
        
        log.info("匹配到审批人: userId={}, realName={}", approver.getId(), approver.getRealName());
        
        // 创建审批任务
        WfTask task = new WfTask();
        task.setInstanceId(instance.getId());
        task.setScenarioNodeId(nextNode.getId());
        task.setAssigneeId(approver.getId());
        task.setStatus(WfTask.STATUS_PENDING);
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);
        
        log.info("创建审批任务: taskId={}, assigneeId={}", task.getId(), approver.getId());
    }
    
    /**
     * 完成流程
     */
    private void completeWorkflow(WfInstance instance) {
        log.info("流程完成: instanceId={}", instance.getId());
        
        // 更新流程实例状态
        instance.setStatus(WfInstance.STATUS_COMPLETED);
        instance.setEndTime(LocalDateTime.now());
        instanceMapper.updateById(instance);
        
        // 更新合同状态为已生效
        Contract contract = contractMapper.selectById(instance.getContractId());
        contract.setStatus(2); // 已生效
        contractMapper.updateById(contract);
        
        log.info("合同已生效: contractId={}", contract.getId());
    }
}
