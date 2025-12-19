-- =====================================================
-- 数据库升级脚本 v3 - SRS文档对齐
-- 
-- 功能：修复后端实体类与SRS文档的不一致问题
-- 执行前请先备份数据库！
-- =====================================================

-- 1. 升级 t_contract 表
-- 添加更新时间字段
ALTER TABLE `t_contract` 
ADD COLUMN `updated_at` datetime DEFAULT NULL COMMENT '更新时间' AFTER `created_at`;

-- 更新状态字段注释（添加待签署和已作废状态）
ALTER TABLE `t_contract` 
MODIFY COLUMN `status` tinyint DEFAULT '0' COMMENT '0:草稿,1:审批中,2:已生效,3:已驳回,4:已终止,5:待签署,6:已作废';

-- 初始化已有数据的更新时间
UPDATE `t_contract` SET `updated_at` = `created_at` WHERE `updated_at` IS NULL;


-- 2. 升级 t_contract_change 表
-- 添加新字段
ALTER TABLE `t_contract_change` 
ADD COLUMN `change_version` varchar(20) DEFAULT NULL COMMENT '变更版本号' AFTER `title`,
ADD COLUMN `party_b_communication` text DEFAULT NULL COMMENT '乙方沟通记录' AFTER `description`,
ADD COLUMN `is_major_change` tinyint(1) DEFAULT 0 COMMENT '是否重大变更' AFTER `diff_data`,
ADD COLUMN `amount_diff` decimal(18,2) DEFAULT NULL COMMENT '变更金额差额' AFTER `is_major_change`,
ADD COLUMN `instance_id` bigint DEFAULT NULL COMMENT '审批流程实例ID' AFTER `status`,
ADD COLUMN `effective_at` datetime DEFAULT NULL COMMENT '变更生效时间' AFTER `approved_at`,
ADD COLUMN `attachment_path` varchar(500) DEFAULT NULL COMMENT '附件路径' AFTER `effective_at`;

-- 更新状态字段注释（添加已撤销状态）
ALTER TABLE `t_contract_change` 
MODIFY COLUMN `status` tinyint DEFAULT '0' COMMENT '0:草稿,1:审批中,2:已通过,3:已驳回,4:已撤销';

-- 更新变更类型字段注释
ALTER TABLE `t_contract_change` 
MODIFY COLUMN `change_type` varchar(50) NOT NULL COMMENT 'AMOUNT:金额,TIME:时间,TECH:技术方案,ATTACHMENT:附件,CONTACT:联系人,OTHER:其他';

-- 更新变更原因字段注释
ALTER TABLE `t_contract_change` 
MODIFY COLUMN `reason_type` varchar(50) NOT NULL COMMENT 'ACTIVE:本方主动,PASSIVE:应乙方要求,FORCE_MAJEURE:不可抗力,POLICY:政策调整,OTHER:其他';


-- 3. 升级 wf_task 表
-- 添加并行组ID（用于会签）
ALTER TABLE `wf_task` 
ADD COLUMN `parallel_group_id` varchar(50) DEFAULT NULL COMMENT '并行组ID（会签分组）' AFTER `approver_id`;

-- 更新状态字段注释（添加已否决状态）
ALTER TABLE `wf_task` 
MODIFY COLUMN `status` tinyint DEFAULT '0' COMMENT '0:待处理,1:已通过,2:已驳回,3:已否决';


-- 4. 升级 wf_definition 表
-- 添加新字段
ALTER TABLE `wf_definition` 
ADD COLUMN `description` varchar(500) DEFAULT NULL COMMENT '流程描述' AFTER `name`,
ADD COLUMN `condition_expr` varchar(500) DEFAULT NULL COMMENT '适用条件表达式' AFTER `apply_type`,
ADD COLUMN `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `is_active`,
ADD COLUMN `updated_at` datetime DEFAULT NULL COMMENT '更新时间' AFTER `created_at`;


-- 5. 创建合同类型枚举说明视图（可选，用于文档化）
CREATE OR REPLACE VIEW `v_contract_type_reference` AS
SELECT 'TYPE_A' AS type_code, '工程施工' AS type_name, '基站建设、传输管线、室分系统集成' AS subtypes
UNION ALL
SELECT 'TYPE_B', '代维服务', '基站代维、铁塔维护、机房维护'
UNION ALL
SELECT 'TYPE_C', 'IT服务', '软件开发、系统集成、DICT项目';


-- 6. 创建合同状态枚举说明视图（可选，用于文档化）
CREATE OR REPLACE VIEW `v_contract_status_reference` AS
SELECT 0 AS status_code, '草稿' AS status_name, '合同创建后的初始状态' AS description
UNION ALL SELECT 1, '审批中', '已提交审批，等待审批人处理'
UNION ALL SELECT 2, '已生效', '审批通过并签署后生效'
UNION ALL SELECT 3, '已驳回', '审批被驳回，可重新编辑提交'
UNION ALL SELECT 4, '已终止', '已生效的合同被终止'
UNION ALL SELECT 5, '待签署', '审批通过后等待签署'
UNION ALL SELECT 6, '已作废', '草稿或驳回状态的合同被作废';


-- =====================================================
-- 执行完成后验证
-- =====================================================
-- SELECT 'Upgrade completed!' AS message;
-- SHOW COLUMNS FROM t_contract;
-- SHOW COLUMNS FROM t_contract_change;
-- SHOW COLUMNS FROM wf_task;
-- SHOW COLUMNS FROM wf_definition;

