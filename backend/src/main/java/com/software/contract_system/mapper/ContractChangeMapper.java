package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.ContractChange;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContractChangeMapper extends BaseMapper<ContractChange> {
    
}