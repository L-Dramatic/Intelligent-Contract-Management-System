package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.dto.ApproveDTO;
import com.software.contract_system.dto.PreFlightCheckResult;
import com.software.contract_system.entity.*;
import com.software.contract_system.mapper.*;
import com.software.contract_system.service.ContractReviewRuleEngine;
import com.software.contract_system.service.ContractReviewService;
import com.software.contract_system.service.WorkflowRuleEngine;
import com.software.contract_system.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Autowired private ContractMapper contractMapper;
    @Autowired private WfDefinitionMapper defMapper;
    @Autowired private WfInstanceMapper instanceMapper;
    @Autowired private WfNodeMapper nodeMapper;
    @Autowired private WfTransitionMapper transitionMapper;
    @Autowired private WfTaskMapper taskMapper;
    @Autowired private SysUserMapper userMapper;
    
    // 新增：审查规则引擎（Pre-Flight Check）
    @Autowired private ContractReviewRuleEngine reviewRuleEngine;
    
    // 新增：AI审查服务
    @Autowired private ContractReviewService contractReviewService;
    
    // 新增：审批流程引擎
    @Autowired private WorkflowRuleEngine workflowRuleEngine;

    // ==========================================
    // 1. 提交合同 (启动流程)
    // ==========================================
    @Override
    @Transactional // 开启事务，保证原子性
    public void submitContract(Long contractId, Long userId) {
        submitContract(contractId, userId, Collections.emptyList(), false);
    }

    /**
     * 提交合同（带附件列表和用户确认标志）
     * 
     * @param contractId 合同ID
     * @param userId 用户ID
     * @param attachments 附件文件名列表
     * @param userConfirmed 用户是否已确认警告
     */
    @Override
    @Transactional
    public void submitContract(Long contractId, Long userId, List<String> attachments, boolean userConfirmed) {
        log.info("提交合同审批: contractId={}, userId={}, attachments={}, userConfirmed={}", 
                contractId, userId, attachments != null ? attachments.size() : 0, userConfirmed);
        
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
        
        // 如果有阻断性错误，抛出异常阻止提交
        if (!checkResult.isPassed()) {
            log.warn("Pre-Flight Check失败: contractId={}, blockingErrors={}", 
                    contractId, checkResult.getBlockingErrors().size());
            StringBuilder errorMsg = new StringBuilder("合同检查未通过，存在以下问题：\n");
            for (PreFlightCheckResult.CheckItem error : checkResult.getBlockingErrors()) {
                errorMsg.append("- ").append(error.getMessage()).append("\n");
            }
            throw new RuntimeException(errorMsg.toString());
        }
        
        // 如果需要确认但用户未确认，抛出提示
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
        // 1.3 查找适用的流程定义
        // ========================================
        // TODO: 后续实现审批流程引擎，根据合同类型和金额匹配流程
        // 目前暂时使用硬编码 ID=1
        WfDefinition def = matchWorkflowDefinition(contract);
        if (def == null) throw new RuntimeException("未找到匹配的审批流程");

        // 1.4 创建流程实例
        WfInstance instance = new WfInstance();
        instance.setDefId(def.getId());
        instance.setContractId(contractId);
        instance.setStatus(1); // 运行中
        instance.setRequesterId(userId);
        instanceMapper.insert(instance);

        // 1.5 找到开始节点，并流转到下一个节点
        WfNode startNode = nodeMapper.selectOne(new LambdaQueryWrapper<WfNode>()
                .eq(WfNode::getDefId, def.getId())
                .eq(WfNode::getType, "START"));
        
        // 自动流转到下一关
        toNextNode(instance, startNode);

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
            // AI审查失败不阻断审批流程，仅记录日志
            log.error("触发AI审查失败（不影响审批流程）: contractId={}", contractId, e);
        }
        
        log.info("合同提交审批成功: contractId={}, instanceId={}", contractId, instance.getId());
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

    /**
     * 匹配审批流程定义
     * 使用WorkflowRuleEngine进行匹配
     */
    private WfDefinition matchWorkflowDefinition(Contract contract) {
        // 使用审批流程引擎进行匹配
        WfDefinition definition = workflowRuleEngine.matchDefinition(contract);
        
        if (definition != null) {
            log.info("审批流程匹配成功: {}", workflowRuleEngine.getMatchReason(contract));
        }
        
        return definition;
    }

    // ==========================================
    // 2. 执行审批 (通过/驳回)
    // ==========================================
    @Override
    @Transactional
    public void approveTask(ApproveDTO dto, Long userId) {
        // 2.1 校验任务
        WfTask task = taskMapper.selectById(dto.getTaskId());
        if (task == null) throw new RuntimeException("任务不存在");
        if (!task.getApproverId().equals(userId)) throw new RuntimeException("你无权审批此任务");
        if (task.getStatus() != 0) throw new RuntimeException("任务已处理");

        // 2.2 更新任务状态
        task.setStatus(dto.getPass() ? 1 : 2); // 1通过, 2驳回
        task.setComment(dto.getComment());
        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);

        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        Contract contract = contractMapper.selectById(instance.getContractId());

        // 2.3 逻辑分支
        if (dto.getPass()) {
            // --- 通过 ---
            WfNode currentNode = nodeMapper.selectById(task.getNodeId());
            toNextNode(instance, currentNode);
        } else {
            // --- 驳回 ---
            instance.setStatus(3); // 实例驳回
            instanceMapper.updateById(instance);
            
            contract.setStatus(3); // 合同驳回
            contractMapper.updateById(contract);
        }
    }

    // ==========================================
    // 3. 查询任务
    // ==========================================
    @Override
    public List<WfTask> getMyTasks(Long userId) {
        return taskMapper.selectList(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getApproverId, userId)
                .eq(WfTask::getStatus, 0) // 只查待办
                .orderByDesc(WfTask::getCreateTime));
    }

    // ==========================================
    // 核心辅助方法：流转到下一节点
    // ==========================================
    private void toNextNode(WfInstance instance, WfNode currentNode) {
        // 1. 找所有可能的连线 (From Current -> To Next)
        List<WfTransition> transitions = transitionMapper.selectList(
            new LambdaQueryWrapper<WfTransition>()
                .eq(WfTransition::getFromNodeId, currentNode.getId())
        );
        
        if (transitions == null || transitions.isEmpty()) {
            // 没有后续连线，说明流程配置有问题，或者到了隐式结尾
            throw new RuntimeException("流程中断：找不到下一节点");
        }

        // 2. 根据条件表达式选择合适的连线
        // 如果只有一条连线，直接使用
        // 如果有多条连线，需要评估条件表达式（简化实现：优先选择无条件的，或第一条）
        WfTransition transition = selectTransition(transitions, instance);
        
        if (transition == null) {
            throw new RuntimeException("流程流转失败：没有满足条件的连线");
        }

        // 3. 获取下一节点
        WfNode nextNode = nodeMapper.selectById(transition.getToNodeId());

        // 3. 判断下一节点类型
        if ("END".equals(nextNode.getType())) {
            // --- 流程结束 ---
            instance.setStatus(2); // 完成
            instance.setCurrentNodeId(nextNode.getId());
            instanceMapper.updateById(instance);

            // 更新合同状态 -> 已生效(2)
            Contract contract = contractMapper.selectById(instance.getContractId());
            contract.setStatus(2);
            contractMapper.updateById(contract);
            
        } else {
            // --- 普通节点/审批节点 ---
            instance.setCurrentNodeId(nextNode.getId());
            instanceMapper.updateById(instance);

            // 4. ★★★ 派发任务 (最关键的一步) ★★★
            // 查找谁该负责这个节点
            Long approverId = findApprover(nextNode);
            
            // 创建新任务
            WfTask newTask = new WfTask();
            newTask.setInstanceId(instance.getId());
            newTask.setNodeId(nextNode.getId());
            newTask.setApproverId(approverId);
            newTask.setStatus(0); // 待处理
            taskMapper.insert(newTask);
        }
    }

    /**
     * 根据节点配置查找审批人
     */
    private Long findApprover(WfNode node) {
        // 如果配置的是角色 (ROLE)，比如 'MANAGER'
        if ("ROLE".equals(node.getApproverType())) {
            String roleCode = node.getApproverValue();
            if (roleCode == null || roleCode.trim().isEmpty()) {
                throw new RuntimeException("节点配置错误：审批人角色编码为空");
            }
            
            // 去用户表找一个该角色的用户
            // 简单起见，我们取第一个匹配的人。实际项目可能需要更复杂的分配策略。
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getRole, roleCode));
            
            if (users.isEmpty()) {
                throw new RuntimeException("流转失败：系统中找不到角色为 " + roleCode + " 的用户");
            }
            
            // 获取第一个用户并进行null检查
            SysUser user = users.get(0);
            if (user == null || user.getId() == null) {
                throw new RuntimeException("流转失败：用户数据异常，用户ID为空");
            }
            
            return user.getId();
        }
        
        // 如果配置的是具体用户 (USER)
        if ("USER".equals(node.getApproverType())) {
            String userIdStr = node.getApproverValue();
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                throw new RuntimeException("节点配置错误：审批人用户ID为空");
            }
            
            try {
                Long userId = Long.parseLong(userIdStr);
                // 验证用户是否存在
                SysUser user = userMapper.selectById(userId);
                if (user == null) {
                    throw new RuntimeException("流转失败：指定的审批人不存在(ID: " + userId + ")");
                }
                return userId;
            } catch (NumberFormatException e) {
                throw new RuntimeException("节点配置错误：审批人用户ID格式不正确");
            }
        }
        
        throw new RuntimeException("节点配置错误：未知的审批人类型 " + node.getApproverType());
    }
    
    /**
     * 从多条连线中选择一条满足条件的
     * 简化实现：优先选择无条件的连线，如果没有则选择第一条
     * 
     * TODO: 后续可以实现真正的条件表达式解析（例如：amount > 100000）
     */
    private WfTransition selectTransition(List<WfTransition> transitions, WfInstance instance) {
        if (transitions.size() == 1) {
            return transitions.get(0);
        }
        
        // 查找无条件的连线
        for (WfTransition transition : transitions) {
            String conditionExpr = transition.getConditionExpr();
            if (conditionExpr == null || conditionExpr.trim().isEmpty()) {
                return transition;
            }
        }
        
        // TODO: 在这里可以实现条件表达式评估逻辑
        // 例如：解析 "amount > 100000" 这样的表达式
        // Contract contract = contractMapper.selectById(instance.getContractId());
        // if (evaluateExpression(transition.getConditionExpr(), contract)) {
        //     return transition;
        // }
        
        // 简化实现：如果都有条件，返回第一个（实际项目应该抛异常）
        return transitions.get(0);
    }
}