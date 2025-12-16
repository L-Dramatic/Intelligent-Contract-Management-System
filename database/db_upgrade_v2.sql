-- 切换到 contract_system 数据库
USE contract_system;

-- =============================================
-- 1. 升级部门表 (sys_dept) - 对应 SRS 4.1.1
-- 增加：部门代码、类型、层级
-- =============================================
ALTER TABLE `sys_dept`
ADD COLUMN `code` varchar(50) DEFAULT NULL COMMENT '部门代码' AFTER `name`,
ADD COLUMN `type` varchar(20) DEFAULT 'DEPT' COMMENT '类型: PROVINCE(省), CITY(市), DEPT(部门)' AFTER `code`,
ADD COLUMN `level` int DEFAULT 1 COMMENT '树层级' AFTER `type`;

-- 更新一下现有数据的默认值，防止空值
UPDATE `sys_dept` SET `type` = 'PROVINCE', `code`='ORG-ROOT', `level`=1 WHERE id = 1;
UPDATE `sys_dept` SET `type` = 'DEPT', `level`=2 WHERE id > 1;

-- =============================================
-- 2. 升级用户表 (sys_user) - 对应 SRS 4.1.2
-- 增加：直接上级ID (用于树状审批策略)
-- =============================================
ALTER TABLE `sys_user`
ADD COLUMN `direct_leader_id` bigint DEFAULT NULL COMMENT '直接上级ID' AFTER `dept_id`;

-- =============================================
-- 3. 升级合同表 (t_contract) - 对应 SRS 4.2.7
-- 增加：版本号
-- =============================================
ALTER TABLE `t_contract`
ADD COLUMN `version` varchar(20) DEFAULT 'v1.0' COMMENT '当前版本号' AFTER `contract_no`;

-- =============================================
-- 4. 新增：合同变更申请表 - 对应 SRS UC-07
-- 用于记录变更申请、审批状态和变更前后的差异
-- =============================================
CREATE TABLE IF NOT EXISTS `t_contract_change` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contract_id` bigint NOT NULL COMMENT '原合同ID',
  `change_no` varchar(50) NOT NULL COMMENT '变更单号',
  `title` varchar(200) NOT NULL COMMENT '变更标题',
  
  `change_type` varchar(50) NOT NULL COMMENT '变更类型: AMOUNT, TIME, TECH, OTHER',
  `reason_type` varchar(50) NOT NULL COMMENT '原因类型: ACTIVE(主动), PASSIVE(被动-乙方要求)',
  `description` text COMMENT '变更详细说明',
  `diff_data` json COMMENT '变更对比数据(JSON结构)',
  `status` tinyint DEFAULT 0 COMMENT '0:草稿, 1:审批中, 2:已通过, 3:已驳回',
  `initiator_id` bigint NOT NULL COMMENT '发起人',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `approved_at` datetime DEFAULT NULL COMMENT '生效时间',
  
  PRIMARY KEY (`id`)
) COMMENT='合同变更申请表';

-- =============================================
-- 5. 补充数据：设置一下用户的上下级关系 (方便后续测试)
-- 假设: zhang_manager(5) 是 admin(1) 的上级 (仅为测试逻辑)
-- =============================================
UPDATE `sys_user` SET `direct_leader_id` = 5 WHERE `username` = 'admin';