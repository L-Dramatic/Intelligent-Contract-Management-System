/*
 Description: 角色权限关联初始化
 Purpose: 为各角色分配对应的系统权限
 Version: v7
 Date: 2025-12-22
*/

USE contract_system;

-- =======================================================
-- 1. 确保权限表有完整数据
-- =======================================================
INSERT INTO `sys_permission` (`id`, `code`, `name`) VALUES 
(1, 'user:manage', '用户管理'),
(2, 'dept:manage', '组织架构管理'),
(3, 'contract:add', '创建合同'),
(4, 'contract:view', '查看合同'),
(5, 'contract:edit', '编辑合同'),
(6, 'contract:submit', '提交合同'),
(7, 'contract:audit', '审批合同'),
(8, 'workflow:manage', '工作流管理'),
(9, 'system:config', '系统配置')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- =======================================================
-- 2. 清理旧的角色权限关联（可选）
-- =======================================================
DELETE FROM `sys_role_permission`;

-- =======================================================
-- 3. 角色权限关联初始化
-- =======================================================

-- ADMIN (系统管理员) - 管理权限，不负责业务
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`) VALUES 
('ADMIN', 1),  -- user:manage
('ADMIN', 2),  -- dept:manage
('ADMIN', 4),  -- contract:view (可查看)
('ADMIN', 8),  -- workflow:manage
('ADMIN', 9);  -- system:config

-- USER (普通用户/发起人) - 可创建、编辑、提交合同
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`) VALUES 
('USER', 3),   -- contract:add
('USER', 4),   -- contract:view
('USER', 5),   -- contract:edit
('USER', 6);   -- contract:submit

-- MANAGER (经理) - 可创建、查看、审批
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`) VALUES 
('MANAGER', 3),  -- contract:add
('MANAGER', 4),  -- contract:view
('MANAGER', 5),  -- contract:edit
('MANAGER', 6),  -- contract:submit
('MANAGER', 7);  -- contract:audit

-- LEGAL (法务) - 查看和审批
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`) VALUES 
('LEGAL', 4),  -- contract:view
('LEGAL', 7);  -- contract:audit

-- BOSS (领导层) - 查看和审批
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`) VALUES 
('BOSS', 4),   -- contract:view
('BOSS', 7);   -- contract:audit

-- =======================================================
-- 4. 验证
-- =======================================================
SELECT rp.role_code, p.code AS permission_code, p.name AS permission_name
FROM sys_role_permission rp
JOIN sys_permission p ON rp.permission_id = p.id
ORDER BY rp.role_code, p.id;

SELECT '角色权限初始化完成' AS message;

