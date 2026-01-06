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

    @Autowired
    private ContractChangeMapper contractChangeMapper;

    @Autowired
    private com.software.contract_system.service.ContractChangeService contractChangeService;

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
        boolean isChange = "CONTRACT_CHANGE".equals(instance.getRemark());
        if (isChange) {
            ContractChange change = contractChangeMapper.selectById(instance.getContractId());
            if (change != null) {
                change.setStatus(3); // 变更已驳回
                contractChangeMapper.updateById(change);
            }
        } else {
            // 4. 更新合同状态
            Contract contract = contractMapper.selectById(instance.getContractId());
            if (contract != null) {
                contract.setStatus(3); // 已驳回
                contractMapper.updateById(contract);
            }
        }

        log.info("审批驳回完成: contractId={}", instance.getContractId());
    }

    @Override
    public List<WfTask> getPendingTasks(Long userId) {
        List<WfTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getAssigneeId, userId)
                .eq(WfTask::getStatus, WfTask.STATUS_PENDING)
                .orderByDesc(WfTask::getCreateTime));

        // 补充合同信息和节点信息
        enrichTasksWithContractInfo(tasks);
        return tasks;
    }

    @Override
    public List<WfTask> getCompletedTasks(Long userId) {
        List<WfTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getAssigneeId, userId)
                .ne(WfTask::getStatus, WfTask.STATUS_PENDING)
                .orderByDesc(WfTask::getFinishTime));

        // 补充合同信息和节点信息
        enrichTasksWithContractInfo(tasks);
        return tasks;
    }

    /**
     * 补充任务的合同信息和节点信息
     */
    private void enrichTasksWithContractInfo(List<WfTask> tasks) {
        for (WfTask task : tasks) {
            enrichTask(task);
        }
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
        try {
            // 先获取流程实例
            WfInstance instance = getWorkflowProgress(contractId);
            if (instance == null) {
                log.info("getApprovalHistory: 合同 {} 没有流程实例", contractId);
                return List.of();
            }

            // 获取该实例下的所有任务（包括待处理的）
            List<WfTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<WfTask>()
                    .eq(WfTask::getInstanceId, instance.getId())
                    .orderByAsc(WfTask::getCreateTime));

            log.info("getApprovalHistory: 找到 {} 个任务", tasks.size());

            // 补充审批人信息和节点信息
            for (WfTask task : tasks) {
                try {
                    // 补充审批人姓名
                    if (task.getAssigneeId() != null) {
                        SysUser assignee = userMapper.selectById(task.getAssigneeId());
                        if (assignee != null) {
                            task.setAssigneeName(assignee.getRealName());
                        }
                    }

                    // 补充节点名称
                    if (task.getScenarioNodeId() != null) {
                        WfScenarioNode node = scenarioNodeMapper.selectById(task.getScenarioNodeId());
                        if (node != null) {
                            String nodeName = node.getNodeName();
                            // 如果数据库中没有 node_name，则根据 role_code 和 node_level 生成
                            if (nodeName == null || nodeName.isEmpty()) {
                                nodeName = generateNodeName(node.getRoleCode(), node.getNodeLevel());
                            }
                            task.setNodeName(nodeName);
                        }
                    }
                } catch (Exception e) {
                    log.warn("补充任务 {} 信息时出错: {}", task.getId(), e.getMessage());
                }
            }

            return tasks;
        } catch (Exception e) {
            log.error("getApprovalHistory 出错: contractId={}", contractId, e);
            throw e;
        }
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

    @Override
    public List<WfInstance> getMyInitiatedInstances(Long userId) {
        List<WfInstance> instances = instanceMapper.selectList(new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getRequesterId, userId)
                .orderByDesc(WfInstance::getStartTime));

        // 补充合同信息和当前节点信息
        for (WfInstance instance : instances) {
            try {
                // 补充合同名称
                if (instance.getContractId() != null) {
                    Contract contract = contractMapper.selectById(instance.getContractId());
                    if (contract != null) {
                        instance.setContractName(contract.getName());
                    }
                }

                // 补充当前节点名称
                if (instance.getScenarioId() != null && instance.getCurrentNodeOrder() != null) {
                    WfScenarioNode currentNode = scenarioMatchService.getNodeByOrder(
                            instance.getScenarioId(), instance.getCurrentNodeOrder());
                    if (currentNode != null) {
                        String nodeName = currentNode.getNodeName();
                        if (nodeName == null || nodeName.isEmpty()) {
                            nodeName = generateNodeName(currentNode.getRoleCode(), currentNode.getNodeLevel());
                        }
                        instance.setCurrentNodeName(nodeName);
                    }
                }

                // 如果流程已结束，设置节点名称为"结束"
                if (instance.getStatus() != null && instance.getStatus() != WfInstance.STATUS_RUNNING) {
                    instance.setCurrentNodeName("结束");
                }
            } catch (Exception e) {
                log.warn("补充流程实例 {} 信息时出错: {}", instance.getId(), e.getMessage());
            }
        }

        return instances;
    }

    @Override
    public WfTask getTaskDetail(Long taskId) {
        WfTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }

        // 补充信息
        enrichTask(task);

        return task;
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
        // 更新合同状态或变更状态
        boolean isChange = "CONTRACT_CHANGE".equals(instance.getRemark());
        if (isChange) {
            // 变更审批完成
            ContractChange change = contractChangeMapper.selectById(instance.getContractId());
            if (change != null) {
                change.setStatus(2); // 已通过
                change.setApprovedAt(LocalDateTime.now());
                change.setEffectiveAt(LocalDateTime.now());
                contractChangeMapper.updateById(change);

                // 应用变更
                try {
                    contractChangeService.applyChange(change.getId());
                    log.info("变更已生效并应用: changeId={}", change.getId());
                } catch (Exception e) {
                    log.error("应用变更失败: changeId={}, error={}", change.getId(), e.getMessage());
                }
            }
        } else {
            // 合同审批完成
            Contract contract = contractMapper.selectById(instance.getContractId());
            if (contract != null) {
                contract.setStatus(2); // 已生效
                contractMapper.updateById(contract);
                log.info("合同已生效: contractId={}", contract.getId());
            }
        }
    }

    /**
     * 根据角色代码和节点级别生成节点名称
     */
    private String generateNodeName(String roleCode, String nodeLevel) {
        StringBuilder sb = new StringBuilder();

        // 级别前缀
        if (nodeLevel != null) {
            switch (nodeLevel) {
                case "COUNTY":
                    sb.append("县级");
                    break;
                case "CITY":
                    sb.append("市级");
                    break;
                case "PROVINCE":
                    sb.append("省级");
                    break;
            }
        }

        // 角色后缀
        if (roleCode != null) {
            if (roleCode.contains("MANAGER")) {
                sb.append("经理审批");
            } else if (roleCode.contains("DIRECTOR")) {
                sb.append("总监审批");
            } else if (roleCode.contains("LEGAL")) {
                sb.append("法务审核");
            } else {
                sb.append("审批");
            }
        } else {
            sb.append("审批");
        }

        return sb.toString();
    }

    /**
     * 单个任务信息补充（核心逻辑提取）
     */
    private void enrichTask(WfTask task) {
        try {
            // 获取流程实例
            WfInstance instance = instanceMapper.selectById(task.getInstanceId());
            if (instance != null) {
                Long realContractId = instance.getContractId();
                boolean isChange = "CONTRACT_CHANGE".equals(instance.getRemark());

                // 设置变更标记
                task.setIsChange(isChange);

                if (isChange && instance.getContractId() != null) {
                    // 如果是变更审批，instance.contractId 实际存的是 changeId
                    Long changeId = instance.getContractId();
                    task.setChangeId(changeId);

                    if (contractChangeMapper != null) {
                        try {
                            ContractChange change = contractChangeMapper.selectById(changeId);
                            if (change != null) {
                                realContractId = change.getContractId();
                            }
                        } catch (Exception e) {
                            log.warn("获取变更信息失败: changeId={}, error={}", changeId, e.getMessage());
                        }
                    }
                }

                // 获取合同信息
                if (realContractId != null) {
                    Contract contract = contractMapper.selectById(realContractId);
                    if (contract != null) {
                        task.setContractName(contract.getName());
                        task.setContractNo(contract.getContractNo());
                        task.setContractAmount(contract.getAmount());
                        task.setContractType(contract.getType());
                        task.setContractId(contract.getId());
                    }
                }

                // 获取发起人信息
                SysUser requester = userMapper.selectById(instance.getRequesterId());
                if (requester != null) {
                    task.setInitiatorName(requester.getRealName());
                }
            }

            // 补充节点名称
            if (task.getScenarioNodeId() != null) {
                WfScenarioNode node = scenarioNodeMapper.selectById(task.getScenarioNodeId());
                if (node != null) {
                    String nodeName = node.getNodeName();
                    if (nodeName == null || nodeName.isEmpty()) {
                        nodeName = generateNodeName(node.getRoleCode(), node.getNodeLevel());
                    }
                    task.setNodeName(nodeName);
                }
            }
        } catch (Exception e) {
            log.warn("补充任务 {} 信息时出错: {}", task.getId(), e.getMessage());
        }
    }
}
