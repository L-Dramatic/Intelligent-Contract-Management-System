package com.software.contract_system.service;

import com.software.contract_system.entity.ContractTemplate;

import java.util.List;

/**
 * 合同模板服务接口
 */
public interface ContractTemplateService {

    /**
     * 根据子类型获取默认模板
     * @param subTypeCode 子类型代码 (A1, B2, C3...)
     * @return 默认模板
     */
    ContractTemplate getDefaultTemplate(String subTypeCode);

    /**
     * 根据ID获取模板
     */
    ContractTemplate getById(Long id);

    /**
     * 根据主类型获取所有模板
     */
    List<ContractTemplate> getByType(String type);

    /**
     * 根据子类型获取所有模板
     */
    List<ContractTemplate> getBySubType(String subTypeCode);

    /**
     * 获取所有启用的模板
     */
    List<ContractTemplate> getAllActive();
}
