-- 1. 创建并使用数据库
CREATE DATABASE IF NOT EXISTS contract_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE contract_system;

-- 2. 用户表 (User)
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色: ADMIN, MANAGER, USER, LEGAL',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) COMMENT='用户表';

-- 3. 部门表 (Department)
CREATE TABLE IF NOT EXISTS `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `manager_id` bigint DEFAULT NULL COMMENT '部门负责人ID',
  PRIMARY KEY (`id`)
) COMMENT='部门表';

-- 3.1 角色表 (Role)
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) COMMENT='角色表';

-- 3.2 权限表 (Permission)
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `permission_code` varchar(100) NOT NULL COMMENT '权限编码，如: contract:add',
  `permission_name` varchar(100) NOT NULL COMMENT '权限名称',
  `resource_type` varchar(20) DEFAULT 'MENU' COMMENT '资源类型: MENU, BUTTON, API',
  `parent_id` bigint DEFAULT '0' COMMENT '父权限ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) COMMENT='权限表';

-- 3.3 角色-权限关联表 (Role-Permission)
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) COMMENT='角色-权限关联表';

-- 4. 合同主表 (Contract)
CREATE TABLE IF NOT EXISTS `t_contract` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contract_no` varchar(50) NOT NULL COMMENT '合同编号',
  `name` varchar(200) NOT NULL COMMENT '合同名称',
  `type` varchar(20) NOT NULL COMMENT '类型: BASE_STATION, NETWORK...',
  `party_a` varchar(100) NOT NULL COMMENT '甲方',
  `party_b` varchar(100) NOT NULL COMMENT '乙方',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT '合同金额',
  `content` longtext COMMENT '合同正文',
  `status` tinyint DEFAULT '0' COMMENT '0:草稿, 1:审批中, 2:已生效, 3:已驳回',
  `is_ai_generated` tinyint DEFAULT '0' COMMENT '是否AI生成',
  `creator_id` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `attributes` json DEFAULT NULL COMMENT '扩展属性',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_no` (`contract_no`)
) COMMENT='电信合同表';

-- 5. 流程定义表 (Workflow Definition)
CREATE TABLE IF NOT EXISTS `wf_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '流程名称',
  `apply_type` varchar(50) COMMENT '适用合同类型',
  `version` int DEFAULT '1' COMMENT '版本号',
  `is_active` tinyint DEFAULT '1' COMMENT '是否启用',
  PRIMARY KEY (`id`)
) COMMENT='流程定义表';

-- 6. 流程节点表 (Workflow Node)
CREATE TABLE IF NOT EXISTS `wf_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `def_id` bigint NOT NULL COMMENT '关联流程定义ID',
  `node_code` varchar(50) NOT NULL COMMENT '节点编码',
  `node_name` varchar(50) NOT NULL COMMENT '节点名称',
  `type` varchar(20) NOT NULL COMMENT '类型: START, APPROVE, COUNTERSIGN, END',
  `approver_type` varchar(20) COMMENT '审批人类型',
  `approver_value` varchar(50) COMMENT '审批人值',
  PRIMARY KEY (`id`)
) COMMENT='流程节点配置表';

-- 7. 流程连线表 (Workflow Transition)
CREATE TABLE IF NOT EXISTS `wf_transition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `def_id` bigint NOT NULL,
  `from_node_id` bigint NOT NULL,
  `to_node_id` bigint NOT NULL,
  `condition_expr` varchar(255) DEFAULT NULL COMMENT '条件表达式',
  PRIMARY KEY (`id`)
) COMMENT='流程连线表';

-- 8. 流程实例表 (Workflow Instance)
CREATE TABLE IF NOT EXISTS `wf_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `def_id` bigint NOT NULL,
  `contract_id` bigint NOT NULL,
  `current_node_id` bigint DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1:运行中, 2:完成, 3:驳回, 4:终止',
  `requester_id` bigint NOT NULL,
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='流程实例表';

-- 9. 审批任务表 (Workflow Task)
CREATE TABLE IF NOT EXISTS `wf_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `instance_id` bigint NOT NULL,
  `node_id` bigint NOT NULL,
  `approver_id` bigint NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0:待处理, 1:已通过, 2:已驳回',
  `comment` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `finish_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='审批任务表';

-- ==================== 初始化数据 ====================

-- 插入角色数据
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`) VALUES
('ADMIN', '系统管理员', '拥有所有权限'),
('MANAGER', '项目经理', '可创建和查看合同，提交审批'),
('APPROVER', '审批领导', '可审批合同'),
('LEGAL', '法务人员', '可进行合规审查'),
('USER', '普通员工', '可查看合同');

-- 插入权限数据
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `resource_type`, `parent_id`) VALUES
-- 合同管理权限
('contract:view', '查看合同', 'API', 0),
('contract:add', '创建/修改合同', 'API', 0),
('contract:delete', '删除合同', 'API', 0),
('contract:audit', '审批合同', 'API', 0),
-- 工作流权限
('workflow:view', '查看流程', 'API', 0),
('workflow:config', '配置流程', 'API', 0),
-- 系统管理权限
('system:user', '用户管理', 'API', 0),
('system:dept', '部门管理', 'API', 0),
('system:role', '角色管理', 'API', 0);

-- 插入角色-权限关联（ADMIN拥有所有权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.role_code = 'ADMIN';

-- MANAGER角色权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM sys_role r, sys_permission p 
WHERE r.role_code = 'MANAGER' 
AND p.permission_code IN ('contract:view', 'contract:add', 'workflow:view');

-- APPROVER角色权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM sys_role r, sys_permission p 
WHERE r.role_code = 'APPROVER' 
AND p.permission_code IN ('contract:view', 'contract:audit', 'workflow:view');

-- LEGAL角色权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM sys_role r, sys_permission p 
WHERE r.role_code = 'LEGAL' 
AND p.permission_code IN ('contract:view', 'contract:audit');

-- USER角色权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM sys_role r, sys_permission p 
WHERE r.role_code = 'USER' 
AND p.permission_code = 'contract:view';

-- 插入测试用户数据 (密码需要使用BCrypt加密，这里先用明文，实际使用需通过程序加密)
-- BCrypt加密的"123456": $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN'),
('manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '项目经理', 'MANAGER'),
('approver', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '审批领导', 'APPROVER');