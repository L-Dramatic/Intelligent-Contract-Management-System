package com.software.contract_system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.software.contract_system.dto.ContractChangeDTO;
import com.software.contract_system.dto.ContractChangeVO;
import com.software.contract_system.entity.ContractChange;

import java.util.List;

/**
 * 合同变更服务接口
 */
public interface ContractChangeService {

    /**
     * 创建变更申请（草稿）
     */
    ContractChange createChange(ContractChangeDTO dto, Long initiatorId);

    /**
     * 提交变更审批
     */
    void submitChange(Long changeId, Long userId);

    /**
     * 撤销变更申请
     */
    void cancelChange(Long changeId, Long userId);

    /**
     * 变更审批通过后，应用变更到合同
     */
    void applyChange(Long changeId);

    /**
     * 获取变更详情
     */
    ContractChangeVO getChangeDetail(Long changeId);

    /**
     * 获取合同的变更历史
     */
    List<ContractChangeVO> getChangeHistory(Long contractId);

    /**
     * 分页查询变更申请
     */
    IPage<ContractChangeVO> pageChanges(Page<ContractChange> page, Long contractId, Integer status, Long initiatorId);

    /**
     * 检查合同是否可以发起变更
     */
    boolean canCreateChange(Long contractId);

    /**
     * 判断是否为重大变更
     */
    boolean isMajorChange(ContractChangeDTO dto, Long contractId);
    
    /**
     * 获取用户可变更的合同列表
     * 返回用户有权限发起变更的已生效合同（合同创建者或审批流程参与者）
     */
    java.util.List<com.software.contract_system.entity.Contract> getChangeableContracts(Long userId);
}

