package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.ContractReviewRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合同审查规则Mapper
 */
@Mapper
public interface ContractReviewRuleMapper extends BaseMapper<ContractReviewRule> {

    /**
     * 根据合同类型查询启用的规则（按优先级排序）
     */
    @Select("SELECT * FROM t_contract_review_rule " +
            "WHERE contract_type = #{contractType} AND is_enabled = 1 " +
            "ORDER BY priority ASC")
    List<ContractReviewRule> selectByContractType(@Param("contractType") String contractType);

    /**
     * 根据合同类型和规则类别查询启用的规则
     */
    @Select("SELECT * FROM t_contract_review_rule " +
            "WHERE contract_type = #{contractType} AND rule_category = #{ruleCategory} AND is_enabled = 1 " +
            "ORDER BY priority ASC")
    List<ContractReviewRule> selectByTypeAndCategory(
            @Param("contractType") String contractType, 
            @Param("ruleCategory") String ruleCategory);

    /**
     * 根据规则编码查询规则
     */
    @Select("SELECT * FROM t_contract_review_rule WHERE rule_code = #{ruleCode}")
    ContractReviewRule selectByRuleCode(@Param("ruleCode") String ruleCode);

    /**
     * 查询所有CRITICAL级别的规则
     */
    @Select("SELECT * FROM t_contract_review_rule " +
            "WHERE contract_type = #{contractType} AND mandate_level = 'CRITICAL' AND is_enabled = 1 " +
            "ORDER BY priority ASC")
    List<ContractReviewRule> selectCriticalRules(@Param("contractType") String contractType);
}

