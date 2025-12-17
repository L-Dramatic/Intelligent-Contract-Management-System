package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.dto.ApproveDTO;
import com.software.contract_system.entity.*;
import com.software.contract_system.mapper.*;
import com.software.contract_system.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Autowired private ContractMapper contractMapper;
    @Autowired private WfDefinitionMapper defMapper;
    @Autowired private WfInstanceMapper instanceMapper;
    @Autowired private WfNodeMapper nodeMapper;
    @Autowired private WfTransitionMapper transitionMapper;
    @Autowired private WfTaskMapper taskMapper;
    @Autowired private SysUserMapper userMapper;

    // ==========================================
    // 1. 提交合同 (启动流程)
    // ==========================================
    @Override
    @Transactional // 开启事务，保证原子性
    public void submitContract(Long contractId, Long userId) {
        // 1.1 检查合同状态
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) throw new RuntimeException("合同不存在");
        if (contract.getStatus() != 0 && contract.getStatus() != 3) {
            throw new RuntimeException("只有草稿或已驳回的合同可以提交");
        }

        // 1.2 查找适用的流程定义 (这里为了简化，默认找 ID=1 的流程)
        // 实际项目应根据 contract.type 去匹配
        WfDefinition def = defMapper.selectById(1L);
        if (def == null) throw new RuntimeException("未找到匹配的审批流程");

        // 1.3 创建流程实例
        WfInstance instance = new WfInstance();
        instance.setDefId(def.getId());
        instance.setContractId(contractId);
        instance.setStatus(1); // 运行中
        instance.setRequesterId(userId);
        instanceMapper.insert(instance);

        // 1.4 找到开始节点，并流转到下一个节点
        WfNode startNode = nodeMapper.selectOne(new LambdaQueryWrapper<WfNode>()
                .eq(WfNode::getDefId, def.getId())
                .eq(WfNode::getType, "START"));
        
        // 自动流转到下一关
        toNextNode(instance, startNode);

        // 1.5 更新合同状态 -> 审批中(1)
        contract.setStatus(1);
        contractMapper.updateById(contract);
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
        // 1. 找连线 (From Current -> To Next)
        WfTransition transition = transitionMapper.selectOne(new LambdaQueryWrapper<WfTransition>()
                .eq(WfTransition::getFromNodeId, currentNode.getId()));
        
        if (transition == null) {
            // 没有后续连线，说明流程配置有问题，或者到了隐式结尾
            throw new RuntimeException("流程中断：找不到下一节点");
        }

        // 2. 获取下一节点
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
            // 去用户表找一个该角色的用户
            // 简单起见，我们取第一个匹配的人。实际项目可能需要更复杂的分配策略。
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getRole, roleCode));
            
            if (users.isEmpty()) {
                throw new RuntimeException("流转失败：系统中找不到角色为 " + roleCode + " 的用户");
            }
            return users.get(0).getId(); // 派给第一个人
        }
        
        // 如果配置的是具体用户 (USER)
        if ("USER".equals(node.getApproverType())) {
            return Long.parseLong(node.getApproverValue());
        }
        
        throw new RuntimeException("节点配置错误：未知的审批人类型");
    }
}