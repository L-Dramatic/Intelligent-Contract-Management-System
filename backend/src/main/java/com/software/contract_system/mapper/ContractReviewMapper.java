package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.ContractReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合同审查报告Mapper
 */
@Mapper
public interface ContractReviewMapper extends BaseMapper<ContractReview> {

    /**
     * 根据合同ID查询最新的审查报告
     */
    @Select("SELECT * FROM t_contract_review WHERE contract_id = #{contractId} " +
            "ORDER BY created_at DESC LIMIT 1")
    ContractReview selectLatestByContractId(@Param("contractId") Long contractId);

    /**
     * 根据合同ID查询所有审查报告（按时间倒序）
     */
    @Select("SELECT * FROM t_contract_review WHERE contract_id = #{contractId} " +
            "ORDER BY created_at DESC")
    List<ContractReview> selectByContractId(@Param("contractId") Long contractId);

    /**
     * 查询指定状态的审查报告数量
     */
    @Select("SELECT COUNT(*) FROM t_contract_review WHERE status = #{status}")
    int countByStatus(@Param("status") String status);
}

