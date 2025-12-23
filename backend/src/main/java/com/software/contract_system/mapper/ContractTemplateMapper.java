package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.ContractTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合同模板Mapper
 */
@Mapper
public interface ContractTemplateMapper extends BaseMapper<ContractTemplate> {

    /**
     * 根据子类型获取默认模板
     */
    @Select("SELECT * FROM t_contract_template " +
            "WHERE sub_type_code = #{subTypeCode} " +
            "AND is_default = 1 AND is_active = 1 " +
            "LIMIT 1")
    ContractTemplate getDefaultBySubType(@Param("subTypeCode") String subTypeCode);

    /**
     * 根据主类型获取所有模板
     */
    @Select("SELECT * FROM t_contract_template " +
            "WHERE type = #{type} AND is_active = 1 " +
            "ORDER BY sub_type_code")
    List<ContractTemplate> getByType(@Param("type") String type);

    /**
     * 根据子类型获取所有模板
     */
    @Select("SELECT * FROM t_contract_template " +
            "WHERE sub_type_code = #{subTypeCode} AND is_active = 1 " +
            "ORDER BY is_default DESC, version DESC")
    List<ContractTemplate> getBySubType(@Param("subTypeCode") String subTypeCode);
}
