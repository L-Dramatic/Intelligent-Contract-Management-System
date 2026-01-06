/*
 Description: 12_修复县级用户职位（移除INITIATOR，改为实际职位）
 Purpose: 县级员工在个人信息中显示实际职位，但在审批流程中自动识别为发起人
 Version: v12
 Date: 2025-01-06
 
 修改说明：
 - 县级员工的primary_role改为实际职位（如NETWORK_ENGINEER、DICT_PM等）
 - 系统在审批流程中自动识别县级员工为发起人（通过dept.level判断）
*/

USE contract_system;

-- =======================================================
-- 1. 更新县级用户的职位为实际职位
-- =======================================================

-- C县网络部员工：改为网络工程师
UPDATE sys_user 
SET primary_role = 'NETWORK_ENGINEER' 
WHERE username = 'c_net_user' AND primary_role = 'INITIATOR';

-- C县政企部员工：改为DICT项目经理
UPDATE sys_user 
SET primary_role = 'DICT_PM' 
WHERE username = 'c_gov_user' AND primary_role = 'INITIATOR';

-- 更新sys_user_role表
UPDATE sys_user_role 
SET role_code = 'NETWORK_ENGINEER', is_primary = 1
WHERE user_id = (SELECT id FROM sys_user WHERE username = 'c_net_user')
  AND role_code = 'INITIATOR';

UPDATE sys_user_role 
SET role_code = 'DICT_PM', is_primary = 1
WHERE user_id = (SELECT id FROM sys_user WHERE username = 'c_gov_user')
  AND role_code = 'INITIATOR';

-- =======================================================
-- 2. 验证更新结果
-- =======================================================

-- 查看县级用户的职位
SELECT 
    u.id,
    u.username,
    u.real_name,
    d.name AS dept_name,
    d.level AS dept_level,
    u.primary_role,
    r.role_name
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.id
LEFT JOIN sys_role r ON u.primary_role = r.role_code
WHERE d.level >= 3 AND d.type = 'COUNTY'
ORDER BY u.id;

-- 验证是否还有INITIATOR作为primary_role的县级用户
SELECT 
    COUNT(*) AS count_with_initiator
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.id
WHERE d.level >= 3 AND d.type = 'COUNTY' 
  AND u.primary_role = 'INITIATOR';

-- =======================================================
-- 完成提示
-- =======================================================
SELECT 'db_upgrade_v12_fix_county_user_roles 执行完成！县级用户职位已更新为实际职位' AS message;

