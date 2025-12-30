/*
 Description: 01_全量表结构初始化 (Schema)
 Notes: 包含基础表、工作流、RBAC、AI模块及所有后续升级字段
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0; -- 禁用外键检查

-- 1. 创建并使用数据库
CREATE DATABASE IF NOT EXISTS contract_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE contract_system;

-- =======================================================
-- 2. 清理旧表 (Drop Tables)
-- =======================================================
DROP TABLE IF EXISTS `t_contract_change`;
DROP TABLE IF EXISTS `t_contract`;
DROP TABLE IF EXISTS `t_knowledge_base`;
DROP TABLE IF EXISTS `t_ai_log`;
DROP TABLE IF EXISTS `wf_log`;
DROP TABLE IF EXISTS `wf_task`;
DROP TABLE IF EXISTS `wf_instance`;
DROP TABLE IF EXISTS `wf_transition`;
DROP TABLE IF EXISTS `wf_node`;
DROP TABLE IF EXISTS `wf_definition`;
DROP TABLE IF EXISTS `sys_role_permission`;
DROP TABLE IF EXISTS `sys_permission`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_file`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_dept`;

-- =======================================================
-- 3. 创建表结构 (Create Tables)
-- =======================================================

-- 3.1 部门表 (支持省-市-县三级组织架构)
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID（0表示根节点）',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `code` varchar(50) DEFAULT NULL COMMENT '部门代码',
  `type` varchar(20) DEFAULT 'DEPT' COMMENT '类型: PROVINCE（省公司）, CITY（市公司）, COUNTY（县公司）, DEPT（职能部门）',
  `level` int DEFAULT 1 COMMENT '树层级（1=根节点）',
  `manager_id` bigint DEFAULT NULL COMMENT '部门负责人用户ID',
  `sort_order` int DEFAULT 0 COMMENT '排序序号',
  `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除（软删除）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_type` (`type`),
  KEY `idx_code` (`code`)
) COMMENT='组织架构表';

-- 3.2 用户表 (支持角色和Z岗级体系)
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色标识(简单逻辑用，兼容旧版)',
  `primary_role` varchar(50) DEFAULT NULL COMMENT '主要角色编码（关联sys_role.role_code）',
  `z_level` varchar(10) DEFAULT NULL COMMENT 'Z岗级（Z8, Z9, Z10, Z11, Z12, Z13, Z14, Z15）',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `direct_leader_id` bigint DEFAULT NULL COMMENT '直属上级用户ID',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `is_active` tinyint DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_primary_role` (`primary_role`),
  KEY `idx_z_level` (`z_level`)
) COMMENT='用户表';

-- 3.3 角色表 (审批角色定义)
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色编码（对应Master Workflow中的Role Code）',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_category` varchar(50) DEFAULT NULL COMMENT '角色类别（BUSINESS/TECHNICAL/LEGAL/FINANCE/IT/MANAGEMENT/EXECUTIVE/PROCUREMENT/DICT/ADMIN）',
  `dept_type_required` varchar(50) DEFAULT NULL COMMENT '要求的部门类型关键字（如NET-网络部, LEGAL-法务部）',
  `z_level_min` varchar(10) DEFAULT NULL COMMENT '最低Z岗级要求',
  `description` varchar(200) DEFAULT NULL COMMENT '角色说明',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) COMMENT='角色定义表';

-- 3.4 权限表 (来自 rbac_schema)
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '权限标识',
  `name` varchar(50) NOT NULL COMMENT '权限名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) COMMENT='权限定义表';

-- 3.5 角色权限关联表 (来自 rbac_schema)
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色代码',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`)
) COMMENT='角色权限关联表';

-- 3.5.1 用户角色关联表（支持一人多角色）
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `effective_dept_id` bigint DEFAULT NULL COMMENT '生效部门ID（可选，用于跨部门兼职）',
  `is_primary` tinyint DEFAULT 0 COMMENT '是否主要角色',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role_dept` (`user_id`, `role_code`, `effective_dept_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_code` (`role_code`)
) COMMENT='用户角色关联表';

-- 3.6 合同主表 (融合了 db_upgrade_v2 的升级字段)
CREATE TABLE `t_contract` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contract_no` varchar(50) NOT NULL COMMENT '合同编号',
  -- 新增字段
  `version` varchar(20) DEFAULT 'v1.0' COMMENT '版本号',
  `name` varchar(200) NOT NULL COMMENT '合同名称',
  `type` varchar(20) NOT NULL COMMENT '类型',
  `party_a` varchar(100) NOT NULL COMMENT '甲方',
  `party_b` varchar(100) NOT NULL COMMENT '乙方',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT '金额',
  `content` longtext COMMENT '正文',
  `status` tinyint DEFAULT '0' COMMENT '0:草稿,1:审批中,2:生效,3:驳回',
  `is_ai_generated` tinyint DEFAULT '0',
  `creator_id` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `attributes` json DEFAULT NULL COMMENT '扩展属性',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_no` (`contract_no`)
) COMMENT='合同主表';

-- 3.7 合同变更申请表 (来自 db_upgrade_v2)
CREATE TABLE `t_contract_change` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contract_id` bigint NOT NULL COMMENT '原合同ID',
  `change_no` varchar(50) NOT NULL COMMENT '变更单号',
  `title` varchar(200) NOT NULL,
  `change_type` varchar(50) NOT NULL,
  `reason_type` varchar(50) NOT NULL,
  `description` text,
  `diff_data` json COMMENT '变更对比数据',
  `status` tinyint DEFAULT 0,
  `initiator_id` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `approved_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='合同变更申请表';

-- 3.8 工作流定义 (来自 init.sql)
CREATE TABLE `wf_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `apply_type` varchar(50),
  `version` int DEFAULT '1',
  `is_active` tinyint DEFAULT '1',
  PRIMARY KEY (`id`)
) COMMENT='流程定义表';

-- 3.8.1 审批场景配置表（对应Master Workflow矩阵）
CREATE TABLE `wf_scenario_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `scenario_id` varchar(50) NOT NULL COMMENT '场景ID（如A1-Tier1, B2-Tier2）',
  `sub_type_code` varchar(10) NOT NULL COMMENT '合同子类型代码（A1, A2, B1, B2, C1, C2, C3）',
  `sub_type_name` varchar(100) NOT NULL COMMENT '合同子类型名称',
  `amount_min` decimal(18,2) DEFAULT 0 COMMENT '金额下限（含）',
  `amount_max` decimal(18,2) DEFAULT NULL COMMENT '金额上限（不含，NULL表示无上限）',
  `is_fast_track` tinyint DEFAULT 0 COMMENT '是否快速通道',
  `description` varchar(500) DEFAULT NULL COMMENT '场景描述',
  `is_active` tinyint DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scenario_id` (`scenario_id`),
  KEY `idx_sub_type_code` (`sub_type_code`),
  KEY `idx_amount` (`amount_min`, `amount_max`)
) COMMENT='审批场景配置表';

-- 3.8.2 场景审批节点表
CREATE TABLE `wf_scenario_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `scenario_id` varchar(50) NOT NULL COMMENT '场景ID（关联wf_scenario_config）',
  `node_order` int NOT NULL COMMENT '节点顺序',
  `role_code` varchar(50) NOT NULL COMMENT '审批角色编码（关联sys_role）',
  `node_level` varchar(20) NOT NULL COMMENT '审批级别（COUNTY/CITY/PROVINCE）',
  `action_type` varchar(20) NOT NULL COMMENT '动作类型（INITIATE/REVIEW/VERIFY/APPROVE/FINAL_APPROVE）',
  `is_mandatory` tinyint DEFAULT 1 COMMENT '是否必须节点',
  `can_skip` tinyint DEFAULT 0 COMMENT '是否可跳过',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_scenario_id` (`scenario_id`),
  KEY `idx_role_code` (`role_code`),
  UNIQUE KEY `uk_scenario_order` (`scenario_id`, `node_order`)
) COMMENT='场景审批节点配置表';

-- 3.9 工作流节点 (来自 init.sql)
CREATE TABLE `wf_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `def_id` bigint NOT NULL,
  `node_code` varchar(50) NOT NULL,
  `node_name` varchar(50) NOT NULL,
  `type` varchar(20) NOT NULL,
  `approver_type` varchar(20),
  `approver_value` varchar(50),
  PRIMARY KEY (`id`)
) COMMENT='流程节点配置表';

-- 3.10 工作流连线 (来自 init.sql)
CREATE TABLE `wf_transition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `def_id` bigint NOT NULL,
  `from_node_id` bigint NOT NULL,
  `to_node_id` bigint NOT NULL,
  `condition_expr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='流程连线表';

-- 3.11 流程实例 (来自 init.sql)
CREATE TABLE `wf_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `def_id` bigint DEFAULT NULL COMMENT '流程定义ID（兼容旧版）',
  `scenario_id` varchar(50) DEFAULT NULL COMMENT '审批场景ID（新版）',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `current_node_id` bigint DEFAULT NULL COMMENT '当前节点ID（旧版）',
  `current_node_order` int DEFAULT 0 COMMENT '当前节点顺序（新版）',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-进行中，2-已完成，3-已驳回，4-已撤销',
  `requester_id` bigint NOT NULL COMMENT '发起人ID',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_scenario_id` (`scenario_id`),
  KEY `idx_requester_id` (`requester_id`)
) COMMENT='流程实例表';

-- 3.12 审批任务 (来自 init.sql)
CREATE TABLE `wf_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `instance_id` bigint NOT NULL COMMENT '流程实例ID',
  `node_id` bigint DEFAULT NULL COMMENT '节点ID（旧版）',
  `scenario_node_id` bigint DEFAULT NULL COMMENT '场景节点ID（新版）',
  `parallel_group_id` varchar(50) DEFAULT NULL COMMENT '并行审批组ID（用于会签）',
  `assignee_id` bigint NOT NULL COMMENT '审批人ID',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审批，1-已通过，2-已驳回，3-已否决',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `finish_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_instance_id` (`instance_id`),
  KEY `idx_assignee_id` (`assignee_id`),
  KEY `idx_status` (`status`)
) COMMENT='审批任务表';

-- 3.13 审批日志表 (来自 upgrade_schema - 之前漏掉的)
CREATE TABLE `wf_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `instance_id` bigint NOT NULL,
  `node_name` varchar(50) NOT NULL,
  `approver_id` bigint NOT NULL,
  `approver_name` varchar(50),
  `action` varchar(20) NOT NULL,
  `comment` varchar(500),
  `operate_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='审批操作日志表';

-- 3.14 知识库表 (来自 upgrade_schema)
CREATE TABLE `t_knowledge_base` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `content` longtext NOT NULL,
  `type` varchar(50) NOT NULL,
  `vector_id` varchar(100) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='电信领域知识库表';

-- 3.15 AI日志表 (来自 upgrade_schema)
CREATE TABLE `t_ai_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contract_id` bigint,
  `user_id` bigint NOT NULL,
  `function_type` varchar(50),
  `prompt_summary` text,
  `result_summary` text,
  `token_usage` int DEFAULT 0,
  `status` tinyint DEFAULT 1,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='AI服务调用日志表';

-- 3.16 文件表 (来自 upgrade_schema)
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_name` varchar(200) NOT NULL,
  `file_url` varchar(500) NOT NULL,
  `file_size` bigint DEFAULT 0,
  `uploader_id` bigint NOT NULL,
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='文件存储表';

SET FOREIGN_KEY_CHECKS = 1;