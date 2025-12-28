package com.software.contract_system.service;

import com.software.contract_system.entity.SysUser;
import com.software.contract_system.entity.WfScenarioConfig;
import com.software.contract_system.entity.WfScenarioNode;
import java.math.BigDecimal;
import java.util.List;

/**
 * 审批场景匹配服务接口
 * 这是审批引擎的核心服务！
 * 
 * 主要功能：
 * 1. 根据合同类型和金额匹配审批场景
 * 2. 获取场景的审批节点列表
 * 3. 为每个节点匹配具体的审批人
 */
public interface ScenarioMatchService {
    
    /**
     * 根据合同子类型和金额匹配审批场景
     * 
     * @param subTypeCode 合同子类型代码（如 A1, B2, C3）
     * @param amount 合同金额
     * @return 匹配的审批场景，未找到返回null
     */
    WfScenarioConfig matchScenario(String subTypeCode, BigDecimal amount);
    
    /**
     * 获取场景的所有审批节点
     */
    List<WfScenarioNode> getScenarioNodes(String scenarioId);
    
    /**
     * 获取场景的下一个审批节点
     * 
     * @param scenarioId 场景ID
     * @param currentNodeOrder 当前节点顺序（0表示获取第一个节点）
     */
    WfScenarioNode getNextNode(String scenarioId, int currentNodeOrder);
    
    /**
     * 为指定节点匹配审批人
     * 核心逻辑：根据节点的角色要求 + 发起人所属组织 → 确定具体审批人
     * 
     * @param node 审批节点
     * @param initiatorDeptId 发起人所属部门ID
     * @return 匹配到的审批人，未找到返回null
     */
    SysUser matchApprover(WfScenarioNode node, Long initiatorDeptId);
    
    /**
     * 为指定节点匹配所有候选审批人（用于会签或选人场景）
     */
    List<SysUser> matchApproverCandidates(WfScenarioNode node, Long initiatorDeptId);
    
    /**
     * 判断场景是否已到达最后一个节点
     */
    boolean isLastNode(String scenarioId, int nodeOrder);
    
    /**
     * 获取所有场景配置
     */
    List<WfScenarioConfig> getAllScenarios();
    
    /**
     * 根据子类型获取场景配置
     */
    List<WfScenarioConfig> getScenariosBySubType(String subTypeCode);
    
    /**
     * 获取场景详情（包含节点列表）
     */
    WfScenarioConfig getScenarioWithNodes(String scenarioId);
    
    /**
     * 根据场景ID和节点顺序获取节点
     */
    WfScenarioNode getNodeByOrder(String scenarioId, int nodeOrder);
}
