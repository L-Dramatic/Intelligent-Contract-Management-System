/*
 Description: 简化用户角色分类
 Purpose: 将role字段统一为 ADMIN/STAFF/BOSS 三类
 Version: v8
 Date: 2025-12-23
 
 设计说明：
 - ADMIN = 系统管理员（管理界面，不参与业务）
 - STAFF = 工作人员（起草合同 + 审批合同）
 - BOSS  = 领导层（起草 + 审批 + 数据统计）
 
 注意：primary_role 字段保持不变，用于审批流程匹配
*/

USE contract_system;

-- =======================================================
-- 1. 更新用户表的role字段
-- =======================================================

-- 保持 ADMIN 不变
-- UPDATE sys_user SET role = 'ADMIN' WHERE role = 'ADMIN';

-- USER, MANAGER, LEGAL 统一改为 STAFF
UPDATE sys_user SET role = 'STAFF' WHERE role IN ('USER', 'MANAGER', 'LEGAL');

-- BOSS 保持不变
-- UPDATE sys_user SET role = 'BOSS' WHERE role = 'BOSS';

-- =======================================================
-- 2. 更新权限关联表
-- =======================================================

-- 删除旧的 USER, MANAGER, LEGAL 权限关联
DELETE FROM sys_role_permission WHERE role_code IN ('USER', 'MANAGER', 'LEGAL');

-- 为 STAFF 角色添加权限
INSERT INTO sys_role_permission (role_code, permission_id) VALUES 
('STAFF', 3),  -- contract:add
('STAFF', 4),  -- contract:view
('STAFF', 5),  -- contract:edit
('STAFF', 6),  -- contract:submit
('STAFF', 7);  -- contract:audit (STAFF也需要审批权限)

-- 确保 BOSS 有完整权限
DELETE FROM sys_role_permission WHERE role_code = 'BOSS';
INSERT INTO sys_role_permission (role_code, permission_id) VALUES 
('BOSS', 3),   -- contract:add
('BOSS', 4),   -- contract:view
('BOSS', 5),   -- contract:edit
('BOSS', 6),   -- contract:submit
('BOSS', 7);   -- contract:audit

-- =======================================================
-- 3. 验证结果
-- =======================================================

SELECT '=== 用户角色分布 ===' AS info;
SELECT role, COUNT(*) AS user_count FROM sys_user GROUP BY role;

SELECT '=== 角色权限关联 ===' AS info;
SELECT rp.role_code, GROUP_CONCAT(p.code) AS permissions
FROM sys_role_permission rp
JOIN sys_permission p ON rp.permission_id = p.id
GROUP BY rp.role_code;

SELECT '角色简化完成' AS message;

