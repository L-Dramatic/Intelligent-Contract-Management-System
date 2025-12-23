package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.WfScenarioConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 审批场景配置表数据库访问接口
 */
@Mapper
public interface WfScenarioConfigMapper extends BaseMapper<WfScenarioConfig> {
    
    /**
     * 根据场景ID查询
     */
    @Select("SELECT * FROM wf_scenario_config WHERE scenario_id = #{scenarioId} AND is_active = 1")
    WfScenarioConfig selectByScenarioId(@Param("scenarioId") String scenarioId);
    
    /**
     * 根据合同子类型和金额匹配审批场景
     * 这是核心匹配逻辑！
     */
    @Select("SELECT * FROM wf_scenario_config " +
            "WHERE sub_type_code = #{subTypeCode} " +
            "AND is_active = 1 " +
            "AND amount_min <= #{amount} " +
            "AND (amount_max IS NULL OR amount_max > #{amount}) " +
            "LIMIT 1")
    WfScenarioConfig matchScenario(@Param("subTypeCode") String subTypeCode, @Param("amount") BigDecimal amount);
    
    /**
     * 根据合同子类型查询所有场景
     */
    @Select("SELECT * FROM wf_scenario_config " +
            "WHERE sub_type_code = #{subTypeCode} " +
            "AND is_active = 1 " +
            "ORDER BY amount_min")
    List<WfScenarioConfig> selectBySubTypeCode(@Param("subTypeCode") String subTypeCode);
    
    /**
     * 查询所有快速通道场景
     */
    @Select("SELECT * FROM wf_scenario_config WHERE is_fast_track = 1 AND is_active = 1")
    List<WfScenarioConfig> selectFastTrackScenarios();
    
    /**
     * 查询所有启用的场景（按子类型和金额排序）
     */
    @Select("SELECT * FROM wf_scenario_config WHERE is_active = 1 ORDER BY sub_type_code, amount_min")
    List<WfScenarioConfig> selectAllActive();
}
