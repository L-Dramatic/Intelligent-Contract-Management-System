/*
 * Description: 细化用户角色
 * Purpose: 将 STAFF 角色细分为 COUNTY（县级）、CITY（市级）、PROVINCE（省级）
 * Version: v15
 * Date: 2025-01-07
 */

USE contract_system;

-- =======================================================
-- 1. 查看当前角色分布
-- =======================================================
SELECT role, COUNT(*) AS count FROM sys_user GROUP BY role;

-- =======================================================
-- 2. 根据部门ID更新角色
-- =======================================================

-- 县级部门员工（dept_id: 150-154, 160-164, 250-254）改为 COUNTY
UPDATE sys_user SET role = 'COUNTY' 
WHERE role = 'STAFF' AND (
    (dept_id >= 150 AND dept_id <= 154) OR 
    (dept_id >= 160 AND dept_id <= 164) OR 
    (dept_id >= 250 AND dept_id <= 254)
);

-- 省级部门员工（dept_id: 1, 10-15）改为 PROVINCE
UPDATE sys_user SET role = 'PROVINCE' 
WHERE role = 'STAFF' AND (
    dept_id = 1 OR 
    (dept_id >= 10 AND dept_id <= 15)
);

-- 市级部门员工（其他 STAFF）改为 CITY
UPDATE sys_user SET role = 'CITY' 
WHERE role = 'STAFF';

-- =======================================================
-- 3. 验证修改结果
-- =======================================================
SELECT role, COUNT(*) AS count FROM sys_user GROUP BY role;

SELECT 
    u.username,
    u.real_name,
    u.role,
    d.name AS dept_name
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.id
WHERE u.is_active = 1
ORDER BY 
    CASE u.role 
        WHEN 'ADMIN' THEN 1 
        WHEN 'BOSS' THEN 2 
        WHEN 'PROVINCE' THEN 3
        WHEN 'CITY' THEN 4 
        WHEN 'COUNTY' THEN 5 
    END,
    u.dept_id;

SELECT '升级脚本 v15 执行完成！角色已细化。' AS message;

