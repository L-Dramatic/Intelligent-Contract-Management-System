package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.software.contract_system.entity.ContractChange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合同变更Mapper
 */
@Mapper
public interface ContractChangeMapper extends BaseMapper<ContractChange> {
    
    /**
     * 根据合同ID查询变更历史
     */
    @Select("SELECT * FROM t_contract_change WHERE contract_id = #{contractId} ORDER BY created_at DESC")
    List<ContractChange> selectByContractId(@Param("contractId") Long contractId);

    /**
     * 根据发起人查询变更申请
     */
    @Select("SELECT * FROM t_contract_change WHERE initiator_id = #{initiatorId} ORDER BY created_at DESC")
    List<ContractChange> selectByInitiatorId(@Param("initiatorId") Long initiatorId);

    /**
     * 查询合同的最新版本号
     */
    @Select("SELECT MAX(change_version) FROM t_contract_change WHERE contract_id = #{contractId} AND status = 2")
    String selectLatestVersion(@Param("contractId") Long contractId);

    /**
     * 检查合同是否有正在审批中的变更
     */
    @Select("SELECT COUNT(*) FROM t_contract_change WHERE contract_id = #{contractId} AND status = 1")
    int countPendingChanges(@Param("contractId") Long contractId);
}
