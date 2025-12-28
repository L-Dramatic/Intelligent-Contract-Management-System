-- ============================================================
-- 合同变更管理表 (UC-07)
-- 版本：V3
-- 功能：支持合同变更申请、审批、历史追踪
-- ============================================================

-- 合同变更申请表
CREATE TABLE IF NOT EXISTS `t_contract_change` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `contract_id` BIGINT NOT NULL COMMENT '原合同ID',
    `change_no` VARCHAR(50) NOT NULL COMMENT '变更单号（BG-日期-序号）',
    `title` VARCHAR(200) NOT NULL COMMENT '变更标题',
    `change_version` VARCHAR(20) DEFAULT 'v2.0' COMMENT '变更版本号',
    
    -- 变更类型和原因
    `change_type` VARCHAR(20) NOT NULL COMMENT '变更类型：AMOUNT(金额),TIME(时间),TECH(技术方案),ATTACHMENT(附件),CONTACT(联系人),OTHER(其他)',
    `reason_type` VARCHAR(20) NOT NULL COMMENT '变更原因：ACTIVE(本方主动),PASSIVE(应乙方要求),FORCE_MAJEURE(不可抗力),POLICY(政策调整),OTHER(其他)',
    `description` TEXT COMMENT '变更说明',
    `party_b_communication` TEXT COMMENT '乙方沟通记录',
    
    -- 变更对比数据（JSON格式）
    `diff_data` JSON COMMENT '变更前后对比数据，包含beforeContent和afterContent',
    
    -- 变更判定
    `is_major_change` TINYINT(1) DEFAULT 0 COMMENT '是否重大变更（金额>20%或核心条款）',
    `amount_diff` DECIMAL(18,2) DEFAULT 0 COMMENT '变更金额差额',
    
    -- 状态与流程
    `status` INT DEFAULT 0 COMMENT '状态：0草稿,1审批中,2已通过,3已驳回,4已撤销',
    `instance_id` BIGINT COMMENT '审批流程实例ID',
    
    -- 时间与人员
    `initiator_id` BIGINT NOT NULL COMMENT '发起人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `approved_at` DATETIME COMMENT '审批完成时间',
    `effective_at` DATETIME COMMENT '变更生效时间',
    
    -- 附件
    `attachment_path` VARCHAR(500) COMMENT '附件路径（多个用逗号分隔）',
    
    -- 索引
    INDEX `idx_contract_id` (`contract_id`),
    INDEX `idx_initiator_id` (`initiator_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_change_no` (`change_no`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同变更申请表';

-- ============================================================
-- 变更审批场景配置（如果不存在则插入）
-- ============================================================

-- 普通变更场景
INSERT IGNORE INTO `wf_scenario_config` (
    `scenario_id`, `scenario_name`, `sub_type_code`, `sub_type_name`,
    `min_amount`, `max_amount`, `priority`, `description`, `is_active`
) VALUES (
    'CHANGE-NORMAL', '普通变更审批', 'CHANGE', '合同变更',
    0, 999999999999, 100, '普通合同变更审批流程，部门经理审批即可', 1
);

-- 重大变更场景
INSERT IGNORE INTO `wf_scenario_config` (
    `scenario_id`, `scenario_name`, `sub_type_code`, `sub_type_name`,
    `min_amount`, `max_amount`, `priority`, `description`, `is_active`
) VALUES (
    'CHANGE-MAJOR', '重大变更审批', 'CHANGE-MAJOR', '重大合同变更',
    0, 999999999999, 90, '重大变更审批流程，需要法务会签', 1
);

-- 获取场景配置ID
SET @normal_id = (SELECT id FROM wf_scenario_config WHERE scenario_id = 'CHANGE-NORMAL' LIMIT 1);
SET @major_id = (SELECT id FROM wf_scenario_config WHERE scenario_id = 'CHANGE-MAJOR' LIMIT 1);

-- 普通变更场景节点
INSERT IGNORE INTO `wf_scenario_node` (`scenario_config_id`, `node_order`, `node_name`, `action_type`, `role_code`, `org_level`) VALUES
(@normal_id, 1, '发起申请', 'INITIATE', 'INITIATOR', 'SELF'),
(@normal_id, 2, '部门经理审批', 'APPROVE', 'DEPT_MANAGER', 'DEPT');

-- 重大变更场景节点（含法务会签）
INSERT IGNORE INTO `wf_scenario_node` (`scenario_config_id`, `node_order`, `node_name`, `action_type`, `role_code`, `org_level`) VALUES
(@major_id, 1, '发起申请', 'INITIATE', 'INITIATOR', 'SELF'),
(@major_id, 2, '部门经理审批', 'APPROVE', 'DEPT_MANAGER', 'DEPT'),
(@major_id, 3, '法务会签', 'COUNTERSIGN', 'LEGAL', 'COMPANY'),
(@major_id, 4, '分管领导审批', 'APPROVE', 'VICE_PRESIDENT', 'COMPANY');

-- ============================================================
-- 在流程实例表添加备注字段（用于区分合同审批和变更审批）
-- ============================================================
ALTER TABLE `wf_instance` ADD COLUMN IF NOT EXISTS `remark` VARCHAR(100) COMMENT '备注（如CONTRACT_CHANGE表示变更审批）';

-- ============================================================
-- 添加任务取消状态
-- ============================================================
-- WfTask.STATUS_CANCELLED = 4
-- 确保任务表支持取消状态

SELECT '合同变更管理表创建完成！' AS message;

