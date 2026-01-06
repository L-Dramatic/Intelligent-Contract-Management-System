package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.entity.*;
import com.software.contract_system.mapper.*;
import com.software.contract_system.service.ScenarioMatchService;
import com.software.contract_system.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * 审批场景匹配服务实现类
 * 这是审批引擎的核心实现！
 */
@Service
public class ScenarioMatchServiceImpl implements ScenarioMatchService {
    
    @Autowired
    private WfScenarioConfigMapper scenarioConfigMapper;
    
    @Autowired
    private WfScenarioNodeMapper scenarioNodeMapper;
    
    @Autowired
    private SysDeptService deptService;
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private SysDeptMapper deptMapper;
    
    @Autowired
    private WfTaskMapper taskMapper;
    
    @Override
    public WfScenarioConfig matchScenario(String subTypeCode, BigDecimal amount) {
        return scenarioConfigMapper.matchScenario(subTypeCode, amount);
    }
    
    @Override
    public List<WfScenarioNode> getScenarioNodes(String scenarioId) {
        return scenarioNodeMapper.selectByScenarioId(scenarioId);
    }
    
    @Override
    public WfScenarioNode getNextNode(String scenarioId, int currentNodeOrder) {
        if (currentNodeOrder == 0) {
            // 获取第一个节点
            return scenarioNodeMapper.selectByScenarioAndOrder(scenarioId, 1);
        }
        return scenarioNodeMapper.selectNextNode(scenarioId, currentNodeOrder);
    }
    
    @Override
    public SysUser matchApprover(WfScenarioNode node, Long initiatorDeptId) {
        List<SysUser> candidates = matchApproverCandidates(node, initiatorDeptId);
        return candidates.isEmpty() ? null : candidates.get(0);
    }
    
    @Override
    public List<SysUser> matchApproverCandidates(WfScenarioNode node, Long initiatorDeptId) {
        String roleCode = node.getRoleCode();
        String nodeLevel = node.getNodeLevel();
        
        // 确定审批人所在的部门范围
        Long targetDeptId = determineTargetDept(initiatorDeptId, nodeLevel);
        if (targetDeptId == null) {
            return List.of();
        }
        
        // 根据角色和部门查找用户
        return findUsersByRoleAndDept(roleCode, targetDeptId, nodeLevel);
    }
    
    @Override
    public boolean isLastNode(String scenarioId, int nodeOrder) {
        WfScenarioNode lastNode = scenarioNodeMapper.selectLastNode(scenarioId);
        return lastNode != null && lastNode.getNodeOrder() == nodeOrder;
    }
    
    @Override
    public List<WfScenarioConfig> getAllScenarios() {
        // 返回所有场景（包括禁用的），用于管理页面
        return scenarioConfigMapper.selectAll();
    }
    
    @Override
    public List<WfScenarioConfig> getScenariosBySubType(String subTypeCode) {
        return scenarioConfigMapper.selectBySubTypeCode(subTypeCode);
    }
    
    @Override
    public WfScenarioConfig getScenarioWithNodes(String scenarioId) {
        WfScenarioConfig config = scenarioConfigMapper.selectByScenarioId(scenarioId);
        if (config != null) {
            config.setNodes(scenarioNodeMapper.selectByScenarioId(scenarioId));
        }
        return config;
    }
    
    @Override
    public WfScenarioNode getNodeByOrder(String scenarioId, int nodeOrder) {
        List<WfScenarioNode> nodes = scenarioNodeMapper.selectByScenarioId(scenarioId);
        for (WfScenarioNode node : nodes) {
            if (node.getNodeOrder() != null && node.getNodeOrder() == nodeOrder) {
                return node;
            }
        }
        return null;
    }
    
    @Override
    public boolean updateScenario(WfScenarioConfig config) {
        return scenarioConfigMapper.updateById(config) > 0;
    }
    
    @Override
    public boolean toggleScenarioActive(Long id) {
        WfScenarioConfig config = scenarioConfigMapper.selectById(id);
        if (config == null) {
            return false;
        }
        // 切换状态
        config.setIsActive(config.getIsActive() == 1 ? 0 : 1);
        config.setUpdatedAt(java.time.LocalDateTime.now());
        return scenarioConfigMapper.updateById(config) > 0;
    }
    
    @Override
    public WfScenarioConfig createScenario(WfScenarioConfig config) {
        // 生成场景ID
        String scenarioId = generateScenarioId(config.getSubTypeCode());
        config.setScenarioId(scenarioId);
        config.setIsActive(1);
        config.setCreatedAt(java.time.LocalDateTime.now());
        config.setUpdatedAt(java.time.LocalDateTime.now());
        scenarioConfigMapper.insert(config);
        return config;
    }
    
    @Override
    public boolean deleteScenario(Long id) {
        WfScenarioConfig config = scenarioConfigMapper.selectById(id);
        if (config == null) {
            return false;
        }
        // 删除场景的所有节点
        scenarioNodeMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfScenarioNode>()
                .eq(WfScenarioNode::getScenarioId, config.getScenarioId()));
        // 删除场景
        return scenarioConfigMapper.deleteById(id) > 0;
    }
    
    @Override
    public WfScenarioNode addNode(WfScenarioNode node) {
        node.setCreatedAt(java.time.LocalDateTime.now());
        scenarioNodeMapper.insert(node);
        return node;
    }
    
    @Override
    public boolean updateNode(WfScenarioNode node) {
        return scenarioNodeMapper.updateById(node) > 0;
    }
    
    @Override
    public boolean deleteNode(Long nodeId) {
        return scenarioNodeMapper.deleteById(nodeId) > 0;
    }
    
    @Override
    public boolean saveScenarioNodes(String scenarioId, List<WfScenarioNode> nodes) {
        // 先删除原有节点
        scenarioNodeMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfScenarioNode>()
                .eq(WfScenarioNode::getScenarioId, scenarioId));
        
        // 插入新节点
        for (int i = 0; i < nodes.size(); i++) {
            WfScenarioNode node = nodes.get(i);
            node.setScenarioId(scenarioId);
            node.setNodeOrder(i + 1);
            node.setCreatedAt(java.time.LocalDateTime.now());
            scenarioNodeMapper.insert(node);
        }
        return true;
    }
    
    /**
     * 生成场景ID
     */
    private String generateScenarioId(String subTypeCode) {
        // 查询该类型已有的场景数量
        long count = scenarioConfigMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfScenarioConfig>()
                        .likeRight(WfScenarioConfig::getScenarioId, subTypeCode + "-"));
        return subTypeCode + "-" + String.format("%03d", count + 1);
    }
    
    /**
     * 确定审批人所在的目标部门
     * 
     * 核心逻辑：
     * - COUNTY 级别：返回发起人所在部门（仅用于发起节点）
     * - CITY 级别：返回对应的市级部门
     * - PROVINCE 级别：返回对应的省级部门
     */
    private Long determineTargetDept(Long initiatorDeptId, String nodeLevel) {
        SysDept initiatorDept = deptMapper.selectById(initiatorDeptId);
        if (initiatorDept == null) {
            return null;
        }
        
        switch (nodeLevel) {
            case WfScenarioNode.LEVEL_COUNTY:
                // 县级审批：直接使用发起人部门
                return initiatorDeptId;
                
            case WfScenarioNode.LEVEL_CITY:
                // 市级审批：需要找到对应的市级部门
                return findCityLevelDept(initiatorDept);
                
            case WfScenarioNode.LEVEL_PROVINCE:
                // 省级审批：需要找到对应的省级部门
                return findProvinceLevelDept(initiatorDept);
                
            default:
                return null;
        }
    }
    
    /**
     * 查找市级部门
     */
    private Long findCityLevelDept(SysDept initiatorDept) {
        // 如果发起人本身就在市级部门
        if (SysDept.TYPE_DEPT.equals(initiatorDept.getType())) {
            // 检查父节点是否是市公司
            SysDept parent = deptMapper.selectById(initiatorDept.getParentId());
            if (parent != null && SysDept.TYPE_CITY.equals(parent.getType())) {
                // 已经是市级部门
                return initiatorDept.getId();
            }
        }
        
        // 如果发起人在县级部门，需要找到对应的市级部门
        SysDept cityDept = deptService.findCityLevelDept(initiatorDept.getId());
        if (cityDept != null) {
            return cityDept.getId();
        }
        
        // 兜底：找到市公司
        SysDept cityCompany = deptService.getCityCompany(initiatorDept.getId());
        return cityCompany != null ? cityCompany.getId() : null;
    }
    
    /**
     * 查找省级部门
     */
    private Long findProvinceLevelDept(SysDept initiatorDept) {
        // 获取祖先链
        List<SysDept> ancestors = deptMapper.selectAncestors(initiatorDept.getId());
        
        // 找到省公司
        SysDept province = ancestors.stream()
                .filter(d -> SysDept.TYPE_PROVINCE.equals(d.getType()))
                .findFirst()
                .orElse(null);
        
        if (province == null) {
            return null;
        }
        
        // 提取部门关键字
        String deptKeyword = extractDeptKeyword(initiatorDept.getCode());
        if (deptKeyword != null) {
            // 查找省级对应部门
            SysDept provDept = deptMapper.selectCityDeptByKeyword(province.getId(), deptKeyword);
            if (provDept != null) {
                return provDept.getId();
            }
        }
        
        // 兜底：返回省公司本身
        return province.getId();
    }
    
    /**
     * 根据角色和部门查找用户
     * 
     * 匹配策略（简化版，移除高管角色）：
     * 1. 部门经理：在市级职能部门查找
     * 2. 技术审批角色：先精确匹配部门，失败则扩大到市级同类部门
     */
    private List<SysUser> findUsersByRoleAndDept(String roleCode, Long deptId, String nodeLevel) {
        List<SysUser> result;
        
        // 部门经理角色
        if (SysRole.ROLE_DEPT_MANAGER.equals(roleCode)) {
            result = findDeptManagerUsers(deptId, nodeLevel);
            if (!result.isEmpty()) return result;
        }
        
        // 普通技术审批角色：先尝试精确匹配
        result = findUsersByRoleInDept(roleCode, deptId);
        if (!result.isEmpty()) return result;
        
        // 精确匹配失败，尝试在市公司范围内查找
        if (WfScenarioNode.LEVEL_CITY.equals(nodeLevel)) {
            result = findUsersByRoleInCityScope(roleCode, deptId);
            if (!result.isEmpty()) return result;
        }
        
        return List.of();
    }
    
    /**
     * 获取用户的待办任务数量
     */
    private int getPendingTaskCount(Long userId) {
        if (userId == null) return 0;
        long count = taskMapper.selectCount(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getAssigneeId, userId)
                .eq(WfTask::getStatus, WfTask.STATUS_PENDING));
        return (int) count;
    }
    
    /**
     * 按待办任务数量对用户列表排序（任务少的优先）
     */
    private List<SysUser> sortUsersByPendingTasks(List<SysUser> users) {
        return users.stream()
                .sorted(Comparator.comparingInt(u -> getPendingTaskCount(u.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 查找部门经理
     */
    private List<SysUser> findDeptManagerUsers(Long deptId, String nodeLevel) {
        // 先尝试在指定部门查找
        List<SysUser> managers = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPrimaryRole, SysRole.ROLE_DEPT_MANAGER)
                .eq(SysUser::getDeptId, deptId)
                .eq(SysUser::getIsActive, 1));
        
        if (!managers.isEmpty()) return sortUsersByPendingTasks(managers);
        
        // 如果是市级审批，扩大到市公司范围
        if (WfScenarioNode.LEVEL_CITY.equals(nodeLevel)) {
            SysDept cityCompany = deptService.getCityCompany(deptId);
            if (cityCompany != null) {
                // 查找市公司下所有部门的部门经理
                List<SysDept> cityDepts = deptMapper.selectByParentId(cityCompany.getId());
                for (SysDept dept : cityDepts) {
                    managers = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getPrimaryRole, SysRole.ROLE_DEPT_MANAGER)
                            .eq(SysUser::getDeptId, dept.getId())
                            .eq(SysUser::getIsActive, 1));
                    if (!managers.isEmpty()) {
                        List<SysUser> sorted = sortUsersByPendingTasks(managers);
                        return List.of(sorted.get(0)); // 返回任务最少的
                    }
                }
            }
        }
        
        return List.of();
    }
    
    /**
     * 在指定部门查找角色用户
     */
    private List<SysUser> findUsersByRoleInDept(String roleCode, Long deptId) {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPrimaryRole, roleCode)
                .eq(SysUser::getDeptId, deptId)
                .eq(SysUser::getIsActive, 1));
        
        return sortUsersByPendingTasks(users);
    }
    
    /**
     * 在市公司范围内查找角色用户
     */
    private List<SysUser> findUsersByRoleInCityScope(String roleCode, Long deptId) {
        SysDept cityCompany = deptService.getCityCompany(deptId);
        if (cityCompany == null) {
            return List.of();
        }
        
        // 获取市公司下所有部门
        List<SysDept> cityDepts = deptMapper.selectByParentId(cityCompany.getId());
        
        // 在所有市级部门中查找
        for (SysDept dept : cityDepts) {
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPrimaryRole, roleCode)
                    .eq(SysUser::getDeptId, dept.getId())
                    .eq(SysUser::getIsActive, 1));
            if (!users.isEmpty()) {
                return sortUsersByPendingTasks(users);
            }
        }
        
        return List.of();
    }
    
    /**
     * 从部门代码提取关键字
     */
    private String extractDeptKeyword(String code) {
        if (code == null || !code.contains("-")) {
            return null;
        }
        String[] parts = code.split("-");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }
}
