package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.ContractEditHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 合同编辑历史Mapper
 */
@Mapper
public interface ContractEditHistoryMapper extends BaseMapper<ContractEditHistory> {

    /**
     * 根据撤销令牌获取记录
     */
    @Select("SELECT * FROM t_contract_edit_history WHERE undo_token = #{undoToken} AND is_undone = 0")
    ContractEditHistory getByUndoToken(@Param("undoToken") String undoToken);

    /**
     * 获取合同的可撤销操作列表
     */
    @Select("SELECT * FROM t_contract_edit_history " +
            "WHERE contract_id = #{contractId} AND is_undone = 0 " +
            "ORDER BY created_at DESC")
    List<ContractEditHistory> getUndoableByContractId(@Param("contractId") Long contractId);

    /**
     * 标记为已撤销
     */
    @Update("UPDATE t_contract_edit_history SET is_undone = 1 WHERE id = #{id}")
    int markAsUndone(@Param("id") Long id);
}
