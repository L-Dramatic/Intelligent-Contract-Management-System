package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.Contract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ContractMapper extends BaseMapper<Contract> {
    // MyBatis-Plus 搞定一切 CRUD
    
    /**
     * 查询用户参与审批的已生效合同
     * 通过 wf_task 和 wf_instance 关联查询
     */
    @Select("SELECT DISTINCT c.* FROM t_contract c " +
            "INNER JOIN wf_instance wi ON c.id = wi.contract_id " +
            "INNER JOIN wf_task wt ON wi.id = wt.instance_id " +
            "WHERE wt.assignee_id = #{userId} " +
            "AND c.status = 2") // 已生效
    List<Contract> selectChangeableContractsByApprover(@Param("userId") Long userId);
}