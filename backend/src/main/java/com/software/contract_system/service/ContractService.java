package com.software.contract_system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.software.contract_system.dto.ContractDTO;
import com.software.contract_system.entity.Contract;

public interface ContractService extends IService<Contract> {
    
    /**
     * 创建合同
     */
    Long createContract(ContractDTO dto);
    
    /**
     * 更新合同
     */
    Boolean updateContract(ContractDTO dto);
    
    /**
     * 分页查询合同列表
     */
    IPage<Contract> getContractPage(int pageNum, int pageSize, String name, String type);
    
    /**
     * 获取当前用户创建的合同
     */
    IPage<Contract> getMyContracts(int pageNum, int pageSize);
    
    /**
     * 删除合同（带级联检查）
     */
    Boolean deleteContract(Long id);
}
