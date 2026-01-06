/*
 * Description: 补充测试用户数据
 * Purpose: 为各部门补充必要的测试人员，确保审批流程能完整运行
 * Version: v13
 * Date: 2025-01-06
 */

USE contract_system;

-- =======================================================
-- 1. 补充 A市市场部人员
-- =======================================================
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(155, 'a_mkt_mgr', '123456', 'A市市场部经理', 'MANAGER', 'DEPT_MANAGER', 102, 101, 1),
(156, 'a_cs_lead', '123456', 'A市客服主管', 'STAFF', 'CUSTOMER_SERVICE_LEAD', 102, 155, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

INSERT INTO `sys_user_role` (`user_id`, `role_code`, `effective_dept_id`, `is_primary`) VALUES 
(155, 'DEPT_MANAGER', 102, 1),
(156, 'CUSTOMER_SERVICE_LEAD', 102, 1)
ON DUPLICATE KEY UPDATE `is_primary` = VALUES(`is_primary`);

-- =======================================================
-- 2. 补充 A市综合部人员
-- =======================================================
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(158, 'a_admin_mgr', '123456', 'A市综合部经理', 'MANAGER', 'DEPT_MANAGER', 106, 101, 1),
(159, 'a_facility', '123456', 'A市设施协调员', 'STAFF', 'FACILITY_COORDINATOR', 106, 158, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

INSERT INTO `sys_user_role` (`user_id`, `role_code`, `effective_dept_id`, `is_primary`) VALUES 
(158, 'DEPT_MANAGER', 106, 1),
(159, 'FACILITY_COORDINATOR', 106, 1)
ON DUPLICATE KEY UPDATE `is_primary` = VALUES(`is_primary`);

-- =======================================================
-- 3. 补充省级职能部门人员（用于省级审批测试）
-- =======================================================
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(20, 'p_net_mgr', '123456', '省网络部经理', 'MANAGER', 'DEPT_MANAGER', 10, 1, 1),
(21, 'p_legal_mgr', '123456', '省法务部经理', 'MANAGER', 'DEPT_MANAGER', 13, 1, 1),
(22, 'p_legal', '123456', '省法务审查员', 'STAFF', 'LEGAL_REVIEWER', 13, 21, 1),
(23, 'p_fin_mgr', '123456', '省财务部经理', 'MANAGER', 'DEPT_MANAGER', 14, 1, 1),
(24, 'p_gov_mgr', '123456', '省政企部经理', 'MANAGER', 'DEPT_MANAGER', 12, 1, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

INSERT INTO `sys_user_role` (`user_id`, `role_code`, `effective_dept_id`, `is_primary`) VALUES 
(20, 'DEPT_MANAGER', 10, 1),
(21, 'DEPT_MANAGER', 13, 1),
(22, 'LEGAL_REVIEWER', 13, 1),
(23, 'DEPT_MANAGER', 14, 1),
(24, 'DEPT_MANAGER', 12, 1)
ON DUPLICATE KEY UPDATE `is_primary` = VALUES(`is_primary`);

-- =======================================================
-- 4. 验证结果
-- =======================================================
SELECT 
    d.name AS '部门',
    COUNT(u.id) AS '人员数量',
    GROUP_CONCAT(u.real_name SEPARATOR ', ') AS '人员列表'
FROM sys_dept d
LEFT JOIN sys_user u ON u.dept_id = d.id AND u.is_active = 1
WHERE d.is_deleted = 0
GROUP BY d.id, d.name
ORDER BY d.level, d.id;

SELECT '升级脚本 v13 执行完成！新增测试用户已添加。' AS message;

