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

-- 3.1 部门表 (融合了 db_upgrade_v2 的升级字段)
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `manager_id` bigint DEFAULT NULL COMMENT '部门负责人ID',
  -- 新增字段
  `code` varchar(50) DEFAULT NULL COMMENT '部门代码',
  `type` varchar(20) DEFAULT 'DEPT' COMMENT '类型: PROVINCE, CITY, DEPT',
  `level` int DEFAULT 1 COMMENT '树层级',
  PRIMARY KEY (`id`)
) COMMENT='部门表';

-- 3.2 用户表 (融合了 db_upgrade_v2 的升级字段)
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色标识(简单逻辑用)',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  -- 新增字段
  `direct_leader_id` bigint DEFAULT NULL COMMENT '直属上级ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) COMMENT='用户表';

-- 3.3 角色表 (来自 rbac_schema)
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) COMMENT='角色表';

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
  `role_code` varchar(20) NOT NULL COMMENT '角色代码',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`)
) COMMENT='角色权限关联表';

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
  `def_id` bigint NOT NULL,
  `contract_id` bigint NOT NULL,
  `current_node_id` bigint DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `requester_id` bigint NOT NULL,
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='流程实例表';

-- 3.12 审批任务 (来自 init.sql)
CREATE TABLE `wf_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `instance_id` bigint NOT NULL,
  `node_id` bigint NOT NULL,
  `approver_id` bigint NOT NULL,
  `status` tinyint DEFAULT '0',
  `comment` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `finish_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
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