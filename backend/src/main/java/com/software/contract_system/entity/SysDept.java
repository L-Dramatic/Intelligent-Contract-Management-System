package com.software.contract_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_dept")
public class SysDept {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parentId;
    private String name;
    private Long managerId;

    // ★★★ 新增字段 (SRS 4.1.1) ★★★
    private String code;   // 部门代码 (如 DEPT-001)
    private String type;   // 类型 (PROVINCE, CITY, DEPT)
    private Integer level; // 层级 (1, 2, 3)
}