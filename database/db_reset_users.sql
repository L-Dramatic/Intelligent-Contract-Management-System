/*
 * Description: 重置用户数据 - 为每个部门每个职位创建账号
 * Purpose: 清理旧用户，创建标准化测试账号
 * Version: v1
 * Date: 2025-01-07
 */

USE contract_system;

-- =======================================================
-- 1. 清理旧数据（保留 admin）
-- =======================================================
DELETE FROM sys_user_role WHERE user_id > 1;
DELETE FROM sys_user WHERE id > 1;

-- 重置自增ID
ALTER TABLE sys_user AUTO_INCREMENT = 10;

-- =======================================================
-- 2. 省级职能部门用户
-- =======================================================

-- 省网络部 (dept_id=10)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('省网络部经理', '123456', '省网络部经理', 'PROVINCE', 'DEPT_MANAGER', 10, 1),
('省网络部项目经理', '123456', '省网络部项目经理', 'PROVINCE', 'PROJECT_MANAGER', 10, 1),
('省网络部网络工程师', '123456', '省网络部网络工程师', 'PROVINCE', 'NETWORK_ENGINEER', 10, 1);

-- 省市场部 (dept_id=11)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('省市场部经理', '123456', '省市场部经理', 'PROVINCE', 'DEPT_MANAGER', 11, 1),
('省市场部客服主管', '123456', '省市场部客服主管', 'PROVINCE', 'CUSTOMER_SERVICE_LEAD', 11, 1);

-- 省政企部 (dept_id=12)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('省政企部经理', '123456', '省政企部经理', 'PROVINCE', 'DEPT_MANAGER', 12, 1),
('省政企部解决方案架构师', '123456', '省政企部解决方案架构师', 'PROVINCE', 'SOLUTION_ARCHITECT', 12, 1),
('省政企部项目经理', '123456', '省政企部项目经理', 'PROVINCE', 'DICT_PM', 12, 1);

-- 省法务部 (dept_id=13)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('省法务部经理', '123456', '省法务部经理', 'PROVINCE', 'DEPT_MANAGER', 13, 1),
('省法务部审查员', '123456', '省法务部审查员', 'PROVINCE', 'LEGAL_REVIEWER', 13, 1);

-- 省财务部 (dept_id=14)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('省财务部经理', '123456', '省财务部经理', 'PROVINCE', 'DEPT_MANAGER', 14, 1),
('省财务部成本审计员', '123456', '省财务部成本审计员', 'PROVINCE', 'COST_AUDITOR', 14, 1),
('省财务部应收专员', '123456', '省财务部应收专员', 'PROVINCE', 'FINANCE_RECEIVABLE', 14, 1);

-- 省综合部 (dept_id=15)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('省综合部经理', '123456', '省综合部经理', 'PROVINCE', 'DEPT_MANAGER', 15, 1),
('省综合部设施协调员', '123456', '省综合部设施协调员', 'PROVINCE', 'FACILITY_COORDINATOR', 15, 1);

-- =======================================================
-- 3. A市职能部门用户
-- =======================================================

-- A市网络部 (dept_id=101)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('A市网络部经理', '123456', 'A市网络部经理', 'CITY', 'DEPT_MANAGER', 101, 1),
('A市网络部项目经理', '123456', 'A市网络部项目经理', 'CITY', 'PROJECT_MANAGER', 101, 1),
('A市网络部网络工程师', '123456', 'A市网络部网络工程师', 'CITY', 'NETWORK_ENGINEER', 101, 1);

-- A市市场部 (dept_id=102)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('A市市场部经理', '123456', 'A市市场部经理', 'CITY', 'DEPT_MANAGER', 102, 1),
('A市市场部客服主管', '123456', 'A市市场部客服主管', 'CITY', 'CUSTOMER_SERVICE_LEAD', 102, 1);

-- A市政企部 (dept_id=103)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('A市政企部经理', '123456', 'A市政企部经理', 'CITY', 'DEPT_MANAGER', 103, 1),
('A市政企部解决方案架构师', '123456', 'A市政企部解决方案架构师', 'CITY', 'SOLUTION_ARCHITECT', 103, 1),
('A市政企部项目经理', '123456', 'A市政企部项目经理', 'CITY', 'DICT_PM', 103, 1);

-- A市法务部 (dept_id=104)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('A市法务部经理', '123456', 'A市法务部经理', 'CITY', 'DEPT_MANAGER', 104, 1),
('A市法务部审查员', '123456', 'A市法务部审查员', 'CITY', 'LEGAL_REVIEWER', 104, 1);

-- A市财务部 (dept_id=105)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('A市财务部经理', '123456', 'A市财务部经理', 'CITY', 'DEPT_MANAGER', 105, 1),
('A市财务部成本审计员', '123456', 'A市财务部成本审计员', 'CITY', 'COST_AUDITOR', 105, 1),
('A市财务部应收专员', '123456', 'A市财务部应收专员', 'CITY', 'FINANCE_RECEIVABLE', 105, 1);

-- A市综合部 (dept_id=106)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('A市综合部经理', '123456', 'A市综合部经理', 'CITY', 'DEPT_MANAGER', 106, 1),
('A市综合部设施协调员', '123456', 'A市综合部设施协调员', 'CITY', 'FACILITY_COORDINATOR', 106, 1);

-- A市采购部 (dept_id=107)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('A市采购部经理', '123456', 'A市采购部经理', 'CITY', 'DEPT_MANAGER', 107, 1),
('A市采购部采购专员', '123456', 'A市采购部采购专员', 'CITY', 'PROCUREMENT_SPECIALIST', 107, 1);

-- =======================================================
-- 4. B市职能部门用户
-- =======================================================

-- B市网络部 (dept_id=201)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('B市网络部经理', '123456', 'B市网络部经理', 'CITY', 'DEPT_MANAGER', 201, 1),
('B市网络部项目经理', '123456', 'B市网络部项目经理', 'CITY', 'PROJECT_MANAGER', 201, 1),
('B市网络部网络工程师', '123456', 'B市网络部网络工程师', 'CITY', 'NETWORK_ENGINEER', 201, 1);

-- B市市场部 (dept_id=202)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('B市市场部经理', '123456', 'B市市场部经理', 'CITY', 'DEPT_MANAGER', 202, 1),
('B市市场部客服主管', '123456', 'B市市场部客服主管', 'CITY', 'CUSTOMER_SERVICE_LEAD', 202, 1);

-- B市政企部 (dept_id=203)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('B市政企部经理', '123456', 'B市政企部经理', 'CITY', 'DEPT_MANAGER', 203, 1),
('B市政企部解决方案架构师', '123456', 'B市政企部解决方案架构师', 'CITY', 'SOLUTION_ARCHITECT', 203, 1),
('B市政企部项目经理', '123456', 'B市政企部项目经理', 'CITY', 'DICT_PM', 203, 1);

-- B市法务部 (dept_id=204)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('B市法务部经理', '123456', 'B市法务部经理', 'CITY', 'DEPT_MANAGER', 204, 1),
('B市法务部审查员', '123456', 'B市法务部审查员', 'CITY', 'LEGAL_REVIEWER', 204, 1);

-- B市财务部 (dept_id=205)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('B市财务部经理', '123456', 'B市财务部经理', 'CITY', 'DEPT_MANAGER', 205, 1),
('B市财务部成本审计员', '123456', 'B市财务部成本审计员', 'CITY', 'COST_AUDITOR', 205, 1);

-- B市综合部 (dept_id=206)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('B市综合部经理', '123456', 'B市综合部经理', 'CITY', 'DEPT_MANAGER', 206, 1),
('B市综合部设施协调员', '123456', 'B市综合部设施协调员', 'CITY', 'FACILITY_COORDINATOR', 206, 1);

-- B市采购部 (dept_id=207)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('B市采购部经理', '123456', 'B市采购部经理', 'CITY', 'DEPT_MANAGER', 207, 1),
('B市采购部采购专员', '123456', 'B市采购部采购专员', 'CITY', 'PROCUREMENT_SPECIALIST', 207, 1);

-- =======================================================
-- 5. C县职能部门用户（A市下属）
-- =======================================================

-- C县综合部 (dept_id=151)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('C县综合部经理', '123456', 'C县综合部经理', 'COUNTY', 'DEPT_MANAGER', 151, 1),
('C县综合部设施协调员', '123456', 'C县综合部设施协调员', 'COUNTY', 'FACILITY_COORDINATOR', 151, 1);

-- C县市场部 (dept_id=152)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('C县市场部经理', '123456', 'C县市场部经理', 'COUNTY', 'DEPT_MANAGER', 152, 1),
('C县市场部客服主管', '123456', 'C县市场部客服主管', 'COUNTY', 'CUSTOMER_SERVICE_LEAD', 152, 1);

-- C县网络部 (dept_id=153)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('C县网络部经理', '123456', 'C县网络部经理', 'COUNTY', 'DEPT_MANAGER', 153, 1),
('C县网络部网络工程师', '123456', 'C县网络部网络工程师', 'COUNTY', 'NETWORK_ENGINEER', 153, 1),
('C县网络部项目经理', '123456', 'C县网络部项目经理', 'COUNTY', 'PROJECT_MANAGER', 153, 1);

-- C县政企部 (dept_id=154)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('C县政企部经理', '123456', 'C县政企部经理', 'COUNTY', 'DEPT_MANAGER', 154, 1),
('C县政企部项目经理', '123456', 'C县政企部项目经理', 'COUNTY', 'DICT_PM', 154, 1);

-- =======================================================
-- 6. D县职能部门用户（A市下属）
-- =======================================================

-- D县综合部 (dept_id=161)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('D县综合部经理', '123456', 'D县综合部经理', 'COUNTY', 'DEPT_MANAGER', 161, 1),
('D县综合部设施协调员', '123456', 'D县综合部设施协调员', 'COUNTY', 'FACILITY_COORDINATOR', 161, 1);

-- D县市场部 (dept_id=162)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('D县市场部经理', '123456', 'D县市场部经理', 'COUNTY', 'DEPT_MANAGER', 162, 1),
('D县市场部客服主管', '123456', 'D县市场部客服主管', 'COUNTY', 'CUSTOMER_SERVICE_LEAD', 162, 1);

-- D县网络部 (dept_id=163)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('D县网络部经理', '123456', 'D县网络部经理', 'COUNTY', 'DEPT_MANAGER', 163, 1),
('D县网络部网络工程师', '123456', 'D县网络部网络工程师', 'COUNTY', 'NETWORK_ENGINEER', 163, 1);

-- D县政企部 (dept_id=164)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('D县政企部经理', '123456', 'D县政企部经理', 'COUNTY', 'DEPT_MANAGER', 164, 1),
('D县政企部项目经理', '123456', 'D县政企部项目经理', 'COUNTY', 'DICT_PM', 164, 1);

-- =======================================================
-- 7. E县职能部门用户（B市下属）
-- =======================================================

-- E县综合部 (dept_id=251)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('E县综合部经理', '123456', 'E县综合部经理', 'COUNTY', 'DEPT_MANAGER', 251, 1),
('E县综合部设施协调员', '123456', 'E县综合部设施协调员', 'COUNTY', 'FACILITY_COORDINATOR', 251, 1);

-- E县市场部 (dept_id=252)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('E县市场部经理', '123456', 'E县市场部经理', 'COUNTY', 'DEPT_MANAGER', 252, 1),
('E县市场部客服主管', '123456', 'E县市场部客服主管', 'COUNTY', 'CUSTOMER_SERVICE_LEAD', 252, 1);

-- E县网络部 (dept_id=253)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('E县网络部经理', '123456', 'E县网络部经理', 'COUNTY', 'DEPT_MANAGER', 253, 1),
('E县网络部网络工程师', '123456', 'E县网络部网络工程师', 'COUNTY', 'NETWORK_ENGINEER', 253, 1);

-- E县政企部 (dept_id=254)
INSERT INTO sys_user (username, password, real_name, role, primary_role, dept_id, is_active) VALUES
('E县政企部经理', '123456', 'E县政企部经理', 'COUNTY', 'DEPT_MANAGER', 254, 1),
('E县政企部项目经理', '123456', 'E县政企部项目经理', 'COUNTY', 'DICT_PM', 254, 1);

-- =======================================================
-- 8. 插入用户角色关联
-- =======================================================
INSERT INTO sys_user_role (user_id, role_code, effective_dept_id, is_primary)
SELECT id, primary_role, dept_id, 1 FROM sys_user WHERE id > 1;

-- =======================================================
-- 9. 验证结果
-- =======================================================
SELECT 
    CASE 
        WHEN u.role = 'PROVINCE' THEN '省级'
        WHEN u.role = 'CITY' THEN '市级'
        WHEN u.role = 'COUNTY' THEN '县级'
        ELSE u.role
    END AS '级别',
    d.name AS '部门',
    u.username AS '用户名',
    u.real_name AS '姓名',
    u.primary_role AS '职位代码'
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.id
WHERE u.id > 1
ORDER BY u.role DESC, d.level, d.id, u.id;

SELECT CONCAT('用户重置完成！共创建 ', COUNT(*)-1, ' 个用户账号（不含admin）') AS message FROM sys_user;

