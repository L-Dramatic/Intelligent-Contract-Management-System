package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.software.contract_system.common.BusinessException;
import com.software.contract_system.entity.ContractTemplate;
import com.software.contract_system.mapper.ContractTemplateMapper;
import com.software.contract_system.service.ContractTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 合同模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractTemplateServiceImpl extends ServiceImpl<ContractTemplateMapper, ContractTemplate> 
        implements ContractTemplateService {

    private final ContractTemplateMapper templateMapper;

    @Override
    public ContractTemplate getDefaultTemplate(String subTypeCode) {
        ContractTemplate template = templateMapper.getDefaultBySubType(subTypeCode);
        if (template == null) {
            log.warn("未找到子类型 {} 的默认模板", subTypeCode);
            throw BusinessException.notFound("未找到该合同类型的模板: " + subTypeCode);
        }
        return template;
    }

    @Override
    public ContractTemplate getById(Long id) {
        return templateMapper.selectById(id);
    }

    @Override
    public List<ContractTemplate> getByType(String type) {
        return templateMapper.getByType(type);
    }

    @Override
    public List<ContractTemplate> getBySubType(String subTypeCode) {
        return templateMapper.getBySubType(subTypeCode);
    }

    @Override
    public List<ContractTemplate> getAllActive() {
        return list(new LambdaQueryWrapper<ContractTemplate>()
                .eq(ContractTemplate::getIsActive, 1)
                .orderByAsc(ContractTemplate::getType)
                .orderByAsc(ContractTemplate::getSubTypeCode));
    }
}
