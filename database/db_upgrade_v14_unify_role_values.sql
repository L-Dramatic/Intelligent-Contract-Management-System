/*
 * Description: 统一用户角色值
 * Purpose: 将数据库中的 USER、MANAGER、LEGAL 等统一改为 STAFF，与前端保持一致
 * Version: v14
 * Date: 2025-01-06
 */

USE contract_system;

-- =======================================================
-- 1. 查看当前角色分布
-- =======================================================
SELECT role, COUNT(*) AS count FROM sys_user GROUP BY role;

-- =======================================================
-- 2. 将 USER、MANAGER、LEGAL 统一改为 STAFF
-- =======================================================
UPDATE sys_user SET role = 'STAFF' WHERE role IN ('USER', 'MANAGER', 'LEGAL');

-- =======================================================
-- 3. 验证修改结果
-- =======================================================
SELECT role, COUNT(*) AS count FROM sys_user GROUP BY role;

SELECT '升级脚本 v14 执行完成！角色值已统一。' AS message;

