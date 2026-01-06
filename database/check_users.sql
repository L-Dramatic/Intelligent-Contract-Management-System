USE contract_system;

-- 检查是否有重复的用户名
SELECT username, COUNT(*) as cnt FROM sys_user GROUP BY username HAVING cnt > 1;

-- 显示所有用户（带ID）
SELECT id, username, real_name, role, dept_id FROM sys_user ORDER BY id;

