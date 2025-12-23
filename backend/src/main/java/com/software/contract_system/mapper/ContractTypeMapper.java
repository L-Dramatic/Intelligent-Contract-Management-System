package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.ContractType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合同类型Mapper
 */
@Mapper
public interface ContractTypeMapper extends BaseMapper<ContractType> {

    /**
     * 获取所有启用的合同类型（按排序）
     */
    @Select("SELECT * FROM t_contract_type WHERE is_active = 1 ORDER BY sort_order")
    List<ContractType> getAllActive();

    /**
     * 根据主类型获取子类型列表
     */
    @Select("SELECT * FROM t_contract_type WHERE type_code = #{typeCode} AND is_active = 1 ORDER BY sort_order")
    List<ContractType> getByTypeCode(String typeCode);
}
