package com.software.contract_system.service;

import com.software.contract_system.dto.ContractTypeDTO;
import com.software.contract_system.entity.ContractType;

import java.util.List;

/**
 * 合同类型服务接口
 */
public interface ContractTypeService {

    /**
     * 获取所有合同类型（按主类型分组）
     * @return 分组后的合同类型列表
     */
    List<ContractTypeDTO> getAllGrouped();

    /**
     * 获取所有合同类型（扁平列表）
     */
    List<ContractType> getAll();

    /**
     * 根据主类型获取子类型
     */
    List<ContractType> getByTypeCode(String typeCode);

    /**
     * 根据子类型代码获取详情
     */
    ContractType getBySubTypeCode(String subTypeCode);
}
