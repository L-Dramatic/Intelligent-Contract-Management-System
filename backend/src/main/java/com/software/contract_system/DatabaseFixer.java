package com.software.contract_system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== 开始检查数据库表结构 ======");
        try {
            // 检查 updated_at 是否存在
            jdbcTemplate.queryForObject("SELECT updated_at FROM t_contract LIMIT 1", Object.class);
            System.out.println(">>> 字段 updated_at 已存在，无需修复。");
        } catch (Exception e) {
            System.out.println(">>> 发现缺失字段 updated_at，正在修复...");
            try {
                jdbcTemplate.execute("ALTER TABLE t_contract ADD COLUMN updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER created_at");
                System.out.println(">>> [成功] 字段 updated_at 已添加！");
            } catch (Exception ex) {
                System.err.println(">>> [失败] 修复失败: " + ex.getMessage());
            }
        }

        // 检查 wf_instance 表 remark 字段
        try {
            jdbcTemplate.execute("SELECT remark FROM wf_instance LIMIT 1");
            System.out.println(">>> 字段 wf_instance.remark 已存在，无需修复。");
        } catch (Exception e) {
            System.out.println(">>> 发现缺失字段 wf_instance.remark，正在修复...");
            try {
                jdbcTemplate.execute("ALTER TABLE wf_instance ADD COLUMN remark VARCHAR(100) COMMENT '备注'");
                System.out.println(">>> [成功] 字段 wf_instance.remark 已添加！");
            } catch (Exception ex) {
                System.err.println(">>> [失败] 修复 wf_instance.remark 失败: " + ex.getMessage());
            }
        }

        // 检查 wf_scenario_node 表 node_name 字段
        try {
            jdbcTemplate.execute("SELECT node_name FROM wf_scenario_node LIMIT 1");
            System.out.println(">>> 字段 wf_scenario_node.node_name 已存在，无需修复。");
        } catch (Exception e) {
            System.out.println(">>> 发现缺失字段 wf_scenario_node.node_name，正在修复...");
            try {
                jdbcTemplate.execute("ALTER TABLE wf_scenario_node ADD COLUMN node_name VARCHAR(50) COMMENT '节点名称'");
                System.out.println(">>> [成功] 字段 wf_scenario_node.node_name 已添加！");
                // 根据 role_code 和 node_level 自动填充节点名称
                jdbcTemplate.execute("UPDATE wf_scenario_node SET node_name = CONCAT(CASE node_level WHEN 'COUNTY' THEN '县级' WHEN 'CITY' THEN '市级' WHEN 'PROVINCE' THEN '省级' ELSE '' END, CASE WHEN role_code LIKE '%MANAGER%' THEN '经理审批' WHEN role_code LIKE '%DIRECTOR%' THEN '总监审批' WHEN role_code LIKE '%LEGAL%' THEN '法务审核' ELSE '审批' END) WHERE node_name IS NULL");
                System.out.println(">>> [成功] node_name 已自动填充！");
            } catch (Exception ex) {
                System.err.println(">>> [失败] 修复 wf_scenario_node.node_name 失败: " + ex.getMessage());
            }
        }

        // ============ 合同变更表修复 ============
        // 检查 t_contract_change 表是否存在
        try {
            jdbcTemplate.execute("SELECT 1 FROM t_contract_change LIMIT 1");
            System.out.println(">>> 表 t_contract_change 已存在。");
            
            // 检查 change_version 字段
            try {
                jdbcTemplate.execute("SELECT change_version FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.change_version 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.change_version，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN change_version VARCHAR(20) DEFAULT 'v2.0' COMMENT '变更版本号' AFTER title");
                    System.out.println(">>> [成功] 字段 change_version 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 change_version 失败: " + ex.getMessage());
                }
            }
            
            // 检查 diff_data 字段
            try {
                jdbcTemplate.execute("SELECT diff_data FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.diff_data 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.diff_data，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN diff_data JSON COMMENT '变更前后对比数据'");
                    System.out.println(">>> [成功] 字段 diff_data 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 diff_data 失败: " + ex.getMessage());
                }
            }
            
            // 检查 is_major_change 字段
            try {
                jdbcTemplate.execute("SELECT is_major_change FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.is_major_change 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.is_major_change，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN is_major_change TINYINT(1) DEFAULT 0 COMMENT '是否重大变更'");
                    System.out.println(">>> [成功] 字段 is_major_change 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 is_major_change 失败: " + ex.getMessage());
                }
            }
            
            // 检查 amount_diff 字段
            try {
                jdbcTemplate.execute("SELECT amount_diff FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.amount_diff 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.amount_diff，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN amount_diff DECIMAL(18,2) DEFAULT 0 COMMENT '变更金额差额'");
                    System.out.println(">>> [成功] 字段 amount_diff 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 amount_diff 失败: " + ex.getMessage());
                }
            }
            
            // 检查 party_b_communication 字段
            try {
                jdbcTemplate.execute("SELECT party_b_communication FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.party_b_communication 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.party_b_communication，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN party_b_communication TEXT COMMENT '乙方沟通记录'");
                    System.out.println(">>> [成功] 字段 party_b_communication 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 party_b_communication 失败: " + ex.getMessage());
                }
            }
            
            // 检查 approved_at 字段
            try {
                jdbcTemplate.execute("SELECT approved_at FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.approved_at 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.approved_at，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN approved_at DATETIME COMMENT '审批完成时间'");
                    System.out.println(">>> [成功] 字段 approved_at 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 approved_at 失败: " + ex.getMessage());
                }
            }
            
            // 检查 effective_at 字段
            try {
                jdbcTemplate.execute("SELECT effective_at FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.effective_at 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.effective_at，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN effective_at DATETIME COMMENT '变更生效时间'");
                    System.out.println(">>> [成功] 字段 effective_at 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 effective_at 失败: " + ex.getMessage());
                }
            }
            
            // 检查 attachment_path 字段
            try {
                jdbcTemplate.execute("SELECT attachment_path FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.attachment_path 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.attachment_path，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN attachment_path VARCHAR(500) COMMENT '附件路径'");
                    System.out.println(">>> [成功] 字段 attachment_path 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 attachment_path 失败: " + ex.getMessage());
                }
            }
            
            // 检查 instance_id 字段
            try {
                jdbcTemplate.execute("SELECT instance_id FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.instance_id 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.instance_id，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN instance_id BIGINT COMMENT '审批流程实例ID'");
                    System.out.println(">>> [成功] 字段 instance_id 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 instance_id 失败: " + ex.getMessage());
                }
            }
            
            // 检查 initiator_id 字段
            try {
                jdbcTemplate.execute("SELECT initiator_id FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.initiator_id 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.initiator_id，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN initiator_id BIGINT NOT NULL DEFAULT 0 COMMENT '发起人ID'");
                    System.out.println(">>> [成功] 字段 initiator_id 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 initiator_id 失败: " + ex.getMessage());
                }
            }
            
            // 检查 created_at 字段
            try {
                jdbcTemplate.execute("SELECT created_at FROM t_contract_change LIMIT 1");
                System.out.println(">>> 字段 t_contract_change.created_at 已存在。");
            } catch (Exception e) {
                System.out.println(">>> 发现缺失字段 t_contract_change.created_at，正在修复...");
                try {
                    jdbcTemplate.execute("ALTER TABLE t_contract_change ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
                    System.out.println(">>> [成功] 字段 created_at 已添加！");
                } catch (Exception ex) {
                    System.err.println(">>> [失败] 修复 created_at 失败: " + ex.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println(">>> 表 t_contract_change 不存在，正在创建...");
            try {
                jdbcTemplate.execute("""
                    CREATE TABLE t_contract_change (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                        contract_id BIGINT NOT NULL COMMENT '原合同ID',
                        change_no VARCHAR(50) NOT NULL COMMENT '变更单号',
                        title VARCHAR(200) NOT NULL COMMENT '变更标题',
                        change_version VARCHAR(20) DEFAULT 'v2.0' COMMENT '变更版本号',
                        change_type VARCHAR(20) NOT NULL COMMENT '变更类型',
                        reason_type VARCHAR(20) NOT NULL COMMENT '变更原因',
                        description TEXT COMMENT '变更说明',
                        party_b_communication TEXT COMMENT '乙方沟通记录',
                        diff_data JSON COMMENT '变更前后对比数据',
                        is_major_change TINYINT(1) DEFAULT 0 COMMENT '是否重大变更',
                        amount_diff DECIMAL(18,2) DEFAULT 0 COMMENT '变更金额差额',
                        status INT DEFAULT 0 COMMENT '状态：0草稿,1审批中,2已通过,3已驳回,4已撤销',
                        instance_id BIGINT COMMENT '审批流程实例ID',
                        initiator_id BIGINT NOT NULL COMMENT '发起人ID',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        approved_at DATETIME COMMENT '审批完成时间',
                        effective_at DATETIME COMMENT '变更生效时间',
                        attachment_path VARCHAR(500) COMMENT '附件路径',
                        INDEX idx_contract_id (contract_id),
                        INDEX idx_initiator_id (initiator_id),
                        INDEX idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同变更申请表'
                    """);
                System.out.println(">>> [成功] 表 t_contract_change 已创建！");
            } catch (Exception ex) {
                System.err.println(">>> [失败] 创建表 t_contract_change 失败: " + ex.getMessage());
            }
        }

        // 注意：变更审批使用与原合同相同的审批流程
        // 不再需要单独的 CHANGE-NORMAL 和 CHANGE-MAJOR 场景
        // 变更审批会根据原合同的类型和金额自动匹配相应的审批场景

        System.out.println("====== 数据库检查完成 ======");
    }
}

