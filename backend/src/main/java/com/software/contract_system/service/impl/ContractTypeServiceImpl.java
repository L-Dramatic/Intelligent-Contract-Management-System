package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.software.contract_system.dto.ContractTypeDTO;
import com.software.contract_system.entity.ContractType;
import com.software.contract_system.mapper.ContractTypeMapper;
import com.software.contract_system.service.ContractTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 合同类型服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractTypeServiceImpl extends ServiceImpl<ContractTypeMapper, ContractType> 
        implements ContractTypeService {

    private final ContractTypeMapper typeMapper;

    @Override
    public List<ContractTypeDTO> getAllGrouped() {
        List<ContractType> allTypes = typeMapper.getAllActive();
        
        // 按主类型分组
        Map<String, ContractTypeDTO> groupMap = new LinkedHashMap<>();
        
        for (ContractType type : allTypes) {
            String typeCode = type.getTypeCode();
            
            // 获取或创建分组
            ContractTypeDTO group = groupMap.computeIfAbsent(typeCode, k -> {
                ContractTypeDTO dto = new ContractTypeDTO();
                dto.setTypeCode(typeCode);
                dto.setTypeName(type.getTypeName());
                dto.setSubTypes(new ArrayList<>());
                return dto;
            });
            
            // 添加子类型
            ContractTypeDTO.SubType subType = new ContractTypeDTO.SubType();
            subType.setSubTypeCode(type.getSubTypeCode());
            subType.setSubTypeName(type.getSubTypeName());
            subType.setDescription(type.getDescription());
            group.getSubTypes().add(subType);
        }
        
        return new ArrayList<>(groupMap.values());
    }

    @Override
    public List<ContractType> getAll() {
        return typeMapper.getAllActive();
    }

    @Override
    public List<ContractType> getByTypeCode(String typeCode) {
        return typeMapper.getByTypeCode(typeCode);
    }

    @Override
    public ContractType getBySubTypeCode(String subTypeCode) {
        return getOne(new LambdaQueryWrapper<ContractType>()
                .eq(ContractType::getSubTypeCode, subTypeCode)
                .eq(ContractType::getIsActive, 1));
    }
}
