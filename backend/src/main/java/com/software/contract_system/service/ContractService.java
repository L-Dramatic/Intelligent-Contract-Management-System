package com.software.contract_system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.software.contract_system.dto.ContractDTO;
import com.software.contract_system.entity.Contract;

/**
 * 合同服务接口
 */
public interface ContractService extends IService<Contract> {

    /**
     * 创建合同
     * @param contractDTO 前端传来的参数
     * @return 新创建的合同ID
     */
    Long createContract(ContractDTO contractDTO);

    /**
     * 修改合同
     * @param contractDTO 前端传来的参数
     * @return 是否成功
     */
    Boolean updateContract(ContractDTO contractDTO);

    /**
     * 分页查询合同
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @param name 合同名称(模糊查询)
     * @param type 合同类型
     * @return 分页结果
     */
    IPage<Contract> getContractPage(int pageNum, int pageSize, String name, String type);
}