/*
 Description: 移除高管角色，统一为部门经理
 Purpose: 简化系统，取消VICE_PRESIDENT和GENERAL_MANAGER，所有非县级人员统一使用同一平台
 Version: v16
 Date: 2025-12-22
 Changes: 
   - 将所有VICE_PRESIDENT和GENERAL_MANAGER节点替换为DEPT_MANAGER
   - 将用户表中的高管角色改为DEPT_MANAGER
*/

USE contract_system;

-- =======================================================
-- 1. 更新工作流节点：将所有高管角色替换为部门经理
-- =======================================================

-- 将所有VICE_PRESIDENT替换为DEPT_MANAGER
UPDATE wf_scenario_node 
SET role_code = 'DEPT_MANAGER' 
WHERE role_code = 'VICE_PRESIDENT';

-- 将所有GENERAL_MANAGER替换为DEPT_MANAGER
UPDATE wf_scenario_node 
SET role_code = 'DEPT_MANAGER' 
WHERE role_code = 'GENERAL_MANAGER';

-- =======================================================
-- 2. 更新用户角色：将高管角色用户改为部门经理
-- =======================================================

-- 将VICE_PRESIDENT用户改为DEPT_MANAGER
UPDATE sys_user 
SET primary_role = 'DEPT_MANAGER' 
WHERE primary_role = 'VICE_PRESIDENT';

-- 将GENERAL_MANAGER用户改为DEPT_MANAGER
UPDATE sys_user 
SET primary_role = 'DEPT_MANAGER' 
WHERE primary_role = 'GENERAL_MANAGER';

-- =======================================================
-- 3. 更新用户角色关联表
-- =======================================================

-- 更新sys_user_role表中的关联
UPDATE sys_user_role 
SET role_code = 'DEPT_MANAGER' 
WHERE role_code IN ('VICE_PRESIDENT', 'GENERAL_MANAGER', 'T1M');

-- =======================================================
-- 4. 验证修改结果
-- =======================================================

-- 检查节点更新情况
SELECT 
    '节点统计' AS category,
    role_code,
    COUNT(*) AS count
FROM wf_scenario_node
WHERE role_code IN ('VICE_PRESIDENT', 'GENERAL_MANAGER', 'DEPT_MANAGER')
GROUP BY role_code;

-- 检查用户更新情况
SELECT 
    '用户统计' AS category,
    primary_role,
    COUNT(*) AS count
FROM sys_user
WHERE primary_role IN ('VICE_PRESIDENT', 'GENERAL_MANAGER', 'DEPT_MANAGER')
GROUP BY primary_role;

SELECT 'db_upgrade_v16_remove_executive_roles 执行完成！所有高管角色已替换为部门经理' AS message;

