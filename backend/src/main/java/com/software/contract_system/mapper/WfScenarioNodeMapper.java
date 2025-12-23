package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.WfScenarioNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 场景审批节点表数据库访问接口
 */
@Mapper
public interface WfScenarioNodeMapper extends BaseMapper<WfScenarioNode> {
    
    /**
     * 查询场景的所有审批节点（按顺序）
     */
    @Select("SELECT sn.*, r.role_name, r.role_category " +
            "FROM wf_scenario_node sn " +
            "LEFT JOIN sys_role r ON sn.role_code = r.role_code " +
            "WHERE sn.scenario_id = #{scenarioId} " +
            "ORDER BY sn.node_order")
    List<WfScenarioNode> selectByScenarioId(@Param("scenarioId") String scenarioId);
    
    /**
     * 查询场景的指定顺序节点
     */
    @Select("SELECT sn.*, r.role_name, r.role_category " +
            "FROM wf_scenario_node sn " +
            "LEFT JOIN sys_role r ON sn.role_code = r.role_code " +
            "WHERE sn.scenario_id = #{scenarioId} AND sn.node_order = #{nodeOrder}")
    WfScenarioNode selectByScenarioAndOrder(@Param("scenarioId") String scenarioId, @Param("nodeOrder") Integer nodeOrder);
    
    /**
     * 查询场景的下一个节点
     */
    @Select("SELECT sn.*, r.role_name, r.role_category " +
            "FROM wf_scenario_node sn " +
            "LEFT JOIN sys_role r ON sn.role_code = r.role_code " +
            "WHERE sn.scenario_id = #{scenarioId} AND sn.node_order > #{currentOrder} " +
            "ORDER BY sn.node_order " +
            "LIMIT 1")
    WfScenarioNode selectNextNode(@Param("scenarioId") String scenarioId, @Param("currentOrder") Integer currentOrder);
    
    /**
     * 查询场景的节点数量
     */
    @Select("SELECT COUNT(*) FROM wf_scenario_node WHERE scenario_id = #{scenarioId}")
    int countByScenarioId(@Param("scenarioId") String scenarioId);
    
    /**
     * 查询场景的最后一个节点
     */
    @Select("SELECT sn.*, r.role_name, r.role_category " +
            "FROM wf_scenario_node sn " +
            "LEFT JOIN sys_role r ON sn.role_code = r.role_code " +
            "WHERE sn.scenario_id = #{scenarioId} " +
            "ORDER BY sn.node_order DESC " +
            "LIMIT 1")
    WfScenarioNode selectLastNode(@Param("scenarioId") String scenarioId);
    
    /**
     * 根据角色编码查询关联的节点
     */
    @Select("SELECT sn.*, r.role_name, r.role_category " +
            "FROM wf_scenario_node sn " +
            "LEFT JOIN sys_role r ON sn.role_code = r.role_code " +
            "WHERE sn.role_code = #{roleCode}")
    List<WfScenarioNode> selectByRoleCode(@Param("roleCode") String roleCode);
}
