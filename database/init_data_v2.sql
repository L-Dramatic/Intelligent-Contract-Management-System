/*
 Description: 02_全量数据初始化 v2（中国移动省级公司标准架构）
 Purpose: 初始化组织树、角色、用户、审批场景等基础数据
 Version: v2
 Date: 2025-12-22
*/

USE contract_system;

-- =======================================================
-- 0. 清理旧数据（可选，生产环境慎用）
-- =======================================================
-- DELETE FROM sys_user_role;
-- DELETE FROM sys_user WHERE id > 0;
-- DELETE FROM sys_dept WHERE id > 0;
-- DELETE FROM sys_role WHERE id > 0;

-- =======================================================
-- 1. 组织架构初始化（标准模板）
-- =======================================================
-- 说明：这是中国移动省级公司的标准组织架构模板
--      部署后，管理员可根据本省实际情况调整

-- 第1层：省公司（根节点）
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`, `manager_id`) VALUES 
(1, 0, '中国移动XX省公司', 'PROVINCE', 'PROVINCE', 1, 0, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`), `type` = VALUES(`type`);

-- 第2层：省级职能部门
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`, `manager_id`) VALUES 
(10, 1, '省公司网络部', 'PROV-NET', 'DEPT', 2, 1, NULL),
(11, 1, '省公司市场部', 'PROV-MKT', 'DEPT', 2, 2, NULL),
(12, 1, '省公司政企部', 'PROV-GOV', 'DEPT', 2, 3, NULL),
(13, 1, '省公司法务部', 'PROV-LEGAL', 'DEPT', 2, 4, NULL),
(14, 1, '省公司财务部', 'PROV-FIN', 'DEPT', 2, 5, NULL),
(15, 1, '省公司综合部', 'PROV-ADMIN', 'DEPT', 2, 6, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`);

-- 第2层：市级分公司（模板提供2个示例）
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`, `manager_id`) VALUES 
(100, 1, 'A市分公司', 'CITY-A', 'CITY', 2, 10, NULL),
(200, 1, 'B市分公司', 'CITY-B', 'CITY', 2, 20, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`);

-- 第3层：A市职能部门
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`, `manager_id`) VALUES 
(101, 100, 'A市网络部', 'CITY-A-NET', 'DEPT', 3, 1, NULL),
(102, 100, 'A市市场经营部', 'CITY-A-MKT', 'DEPT', 3, 2, NULL),
(103, 100, 'A市政企客户部', 'CITY-A-GOV', 'DEPT', 3, 3, NULL),
(104, 100, 'A市法务部', 'CITY-A-LEGAL', 'DEPT', 3, 4, NULL),
(105, 100, 'A市财务部', 'CITY-A-FIN', 'DEPT', 3, 5, NULL),
(106, 100, 'A市综合部', 'CITY-A-ADMIN', 'DEPT', 3, 6, NULL),
(107, 100, 'A市采购部', 'CITY-A-PROC', 'DEPT', 3, 7, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`);

-- 第3层：B市职能部门（结构与A市相同）
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`, `manager_id`) VALUES 
(201, 200, 'B市网络部', 'CITY-B-NET', 'DEPT', 3, 1, NULL),
(202, 200, 'B市市场经营部', 'CITY-B-MKT', 'DEPT', 3, 2, NULL),
(203, 200, 'B市政企客户部', 'CITY-B-GOV', 'DEPT', 3, 3, NULL),
(204, 200, 'B市法务部', 'CITY-B-LEGAL', 'DEPT', 3, 4, NULL),
(205, 200, 'B市财务部', 'CITY-B-FIN', 'DEPT', 3, 5, NULL),
(206, 200, 'B市综合部', 'CITY-B-ADMIN', 'DEPT', 3, 6, NULL),
(207, 200, 'B市采购部', 'CITY-B-PROC', 'DEPT', 3, 7, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`);

-- 第3层：县级分公司（示例：A市下属2个县，B市下属1个县）
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`, `manager_id`) VALUES 
(150, 100, 'C县分公司', 'COUNTY-C', 'COUNTY', 3, 10, NULL),
(160, 100, 'D县分公司', 'COUNTY-D', 'COUNTY', 3, 11, NULL),
(250, 200, 'E县分公司', 'COUNTY-E', 'COUNTY', 3, 10, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`);

-- 第4层：C县职能部门
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`, `manager_id`) VALUES 
(151, 150, 'C县综合部', 'COUNTY-C-ADMIN', 'DEPT', 4, 1, NULL),
(152, 150, 'C县市场部', 'COUNTY-C-MKT', 'DEPT', 4, 2, NULL),
(153, 150, 'C县网络部', 'COUNTY-C-NET', 'DEPT', 4, 3, NULL),
(154, 150, 'C县政企部', 'COUNTY-C-GOV', 'DEPT', 4, 4, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`);

-- 第4层：D县职能部门（结构与C县相同）
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`, `manager_id`) VALUES 
(161, 160, 'D县综合部', 'COUNTY-D-ADMIN', 'DEPT', 4, 1, NULL),
(162, 160, 'D县市场部', 'COUNTY-D-MKT', 'DEPT', 4, 2, NULL),
(163, 160, 'D县网络部', 'COUNTY-D-NET', 'DEPT', 4, 3, NULL),
(164, 160, 'D县政企部', 'COUNTY-D-GOV', 'DEPT', 4, 4, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`);

-- 第4层：E县职能部门
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`, `manager_id`) VALUES 
(251, 250, 'E县综合部', 'COUNTY-E-ADMIN', 'DEPT', 4, 1, NULL),
(252, 250, 'E县市场部', 'COUNTY-E-MKT', 'DEPT', 4, 2, NULL),
(253, 250, 'E县网络部', 'COUNTY-E-NET', 'DEPT', 4, 3, NULL),
(254, 250, 'E县政企部', 'COUNTY-E-GOV', 'DEPT', 4, 4, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`);

-- =======================================================
-- 2. 角色定义初始化（对应Master Workflow配置矩阵）
-- =======================================================

INSERT INTO `sys_role` (`role_code`, `role_name`, `role_category`, `dept_type_required`, `z_level_min`, `description`) VALUES 
-- 发起人
('INITIATOR', '合同发起人', 'BUSINESS', NULL, 'Z8', '县级/市级业务人员，发起合同审批'),

-- 管理层
('DEPT_MANAGER', '部门经理', 'MANAGEMENT', NULL, 'Z12', '市级部门经理，部门级审批'),
('VICE_PRESIDENT', '副总经理', 'EXECUTIVE', NULL, 'Z13', '市级分公司副总经理，高级审批'),
('GENERAL_MANAGER', '总经理', 'EXECUTIVE', NULL, 'Z14', '市级分公司总经理，最终审批'),
('T1M', '三重一大会议', 'EXECUTIVE', NULL, 'Z13', '市级分公司执行委员会集体决策'),

-- 技术审查（网络类）
('PROJECT_MANAGER', '项目经理', 'TECHNICAL', 'NET', 'Z10', '工程项目技术审查'),
('RF_ENGINEER', '射频工程师', 'TECHNICAL', 'NET', 'Z9', '基站代维技术审查'),
('NETWORK_ENGINEER', '网络工程师', 'TECHNICAL', 'NET', 'Z9', '光缆代维技术审查'),
('NETWORK_PLANNING', '网络规划', 'TECHNICAL', 'NET', 'Z10', '网络战略对齐审查'),
('FACILITY_COORDINATOR', '设施协调员', 'TECHNICAL', 'ADMIN', 'Z9', '零星维修技术审查'),
('BROADBAND_SPECIALIST', '家宽专家', 'TECHNICAL', 'NET', 'Z9', '家宽代维技术审查'),
('OPS_CENTER', '运营中心', 'TECHNICAL', 'NET', 'Z10', '应急保障快速通道'),

-- 设计与质量
('DESIGN_REVIEWER', '设计审查', 'TECHNICAL', 'NET', 'Z10', '装修工程设计标准审查'),
('SITE_ACQUISITION', '站点获取', 'TECHNICAL', 'NET', 'Z10', '基站物业租赁审查'),

-- 法务审查
('LEGAL_REVIEWER', '法务审查员', 'LEGAL', 'LEGAL', 'Z11', '合同法律合规性审查'),

-- 财务审查
('COST_AUDITOR', '成本审计员', 'FINANCE', 'FIN', 'Z10', '工程造价审计'),
('FINANCE_RECEIVABLE', '财务应收检查', 'FINANCE', 'FIN', 'Z10', 'DICT背靠背项目应收验证'),

-- IT审查
('TECHNICAL_LEAD', 'IT技术负责人', 'IT', 'GOV', 'Z11', 'IT项目代码架构审查'),
('SECURITY_REVIEWER', 'IT安全审查', 'IT', 'GOV', 'Z10', 'IT项目网络安全合规审查'),
('IT_ARCHITECT', 'IT架构师', 'IT', 'GOV', 'Z11', 'IT项目企业架构审查'),

-- 采购审查
('PROCUREMENT_SPECIALIST', '采购专员', 'PROCUREMENT', 'PROC', 'Z9', '商用软件采购审查'),
('VENDOR_MANAGER', '供应商管理', 'PROCUREMENT', 'PROC', 'Z10', '供应商资质审查'),

-- DICT审查
('SOLUTION_ARCHITECT', '解决方案架构师', 'DICT', 'GOV', 'Z11', 'DICT项目技术方案审查'),
('DICT_PM', 'DICT项目经理', 'DICT', 'GOV', 'Z10', 'DICT项目治理审查'),

-- 客服审查
('CUSTOMER_SERVICE_LEAD', '客服主管', 'BUSINESS', 'MKT', 'Z10', '家宽代维服务影响审查'),

-- 系统管理员
('SYSTEM_ADMIN', '系统管理员', 'ADMIN', NULL, 'Z10', '系统配置与管理')

ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `role_category` = VALUES(`role_category`);

-- =======================================================
-- 3. 用户数据初始化（测试用户）
-- =======================================================
-- 密码均为 123456

-- 省级管理员
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `z_level`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(1, 'admin', '123456', '系统管理员', 'ADMIN', 'SYSTEM_ADMIN', 'Z12', 1, NULL, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- A市分公司领导层
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `z_level`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(100, 'a_gm', '123456', 'A市总经理', 'BOSS', 'GENERAL_MANAGER', 'Z14', 100, NULL, 1),
(101, 'a_vp', '123456', 'A市副总经理', 'BOSS', 'VICE_PRESIDENT', 'Z13', 100, 100, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- A市网络部人员
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `z_level`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(110, 'a_net_mgr', '123456', 'A市网络部经理', 'MANAGER', 'DEPT_MANAGER', 'Z12', 101, 101, 1),
(111, 'a_rf_eng', '123456', 'A市射频工程师', 'USER', 'RF_ENGINEER', 'Z9', 101, 110, 1),
(112, 'a_net_eng', '123456', 'A市网络工程师', 'USER', 'NETWORK_ENGINEER', 'Z9', 101, 110, 1),
(113, 'a_pm', '123456', 'A市项目经理', 'USER', 'PROJECT_MANAGER', 'Z10', 101, 110, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- A市法务部人员
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `z_level`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(120, 'a_legal_mgr', '123456', 'A市法务部经理', 'MANAGER', 'DEPT_MANAGER', 'Z12', 104, 101, 1),
(121, 'a_legal', '123456', 'A市法务审查员', 'LEGAL', 'LEGAL_REVIEWER', 'Z11', 104, 120, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- A市财务部人员
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `z_level`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(130, 'a_fin_mgr', '123456', 'A市财务部经理', 'MANAGER', 'DEPT_MANAGER', 'Z12', 105, 101, 1),
(131, 'a_cost_audit', '123456', 'A市成本审计员', 'USER', 'COST_AUDITOR', 'Z10', 105, 130, 1),
(132, 'a_fin_recv', '123456', 'A市财务应收', 'USER', 'FINANCE_RECEIVABLE', 'Z10', 105, 130, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- A市政企部人员
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `z_level`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(140, 'a_gov_mgr', '123456', 'A市政企部经理', 'MANAGER', 'DEPT_MANAGER', 'Z12', 103, 101, 1),
(141, 'a_solution', '123456', 'A市解决方案架构师', 'USER', 'SOLUTION_ARCHITECT', 'Z11', 103, 140, 1),
(142, 'a_dict_pm', '123456', 'A市DICT项目经理', 'USER', 'DICT_PM', 'Z10', 103, 140, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- A市采购部人员
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(145, 'a_proc_mgr', '123456', 'A市采购部经理', 'MANAGER', 'DEPT_MANAGER', 107, 101, 1),
(146, 'a_proc', '123456', 'A市采购专员', 'USER', 'PROCUREMENT_SPECIALIST', 107, 145, 1),
(147, 'a_vendor', '123456', 'A市供应商管理', 'USER', 'VENDOR_MANAGER', 107, 145, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- A市市场部人员（补充）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(155, 'a_mkt_mgr', '123456', 'A市市场部经理', 'MANAGER', 'DEPT_MANAGER', 102, 101, 1),
(156, 'a_cs_lead', '123456', 'A市客服主管', 'USER', 'CUSTOMER_SERVICE_LEAD', 102, 155, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- A市综合部人员（补充）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(158, 'a_admin_mgr', '123456', 'A市综合部经理', 'MANAGER', 'DEPT_MANAGER', 106, 101, 1),
(159, 'a_facility', '123456', 'A市设施协调员', 'USER', 'FACILITY_COORDINATOR', 106, 158, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- 省级职能部门人员（补充，用于省级审批测试）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(20, 'p_net_mgr', '123456', '省网络部经理', 'MANAGER', 'DEPT_MANAGER', 10, 1, 1),
(21, 'p_legal_mgr', '123456', '省法务部经理', 'MANAGER', 'DEPT_MANAGER', 13, 1, 1),
(22, 'p_legal', '123456', '省法务审查员', 'LEGAL', 'LEGAL_REVIEWER', 13, 21, 1),
(23, 'p_fin_mgr', '123456', '省财务部经理', 'MANAGER', 'DEPT_MANAGER', 14, 1, 1),
(24, 'p_gov_mgr', '123456', '省政企部经理', 'MANAGER', 'DEPT_MANAGER', 12, 1, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- C县分公司人员（县级员工保留实际职位，在审批流程起点统一作为发起人）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `primary_role`, `dept_id`, `direct_leader_id`, `is_active`) VALUES 
(150, 'c_mgr', '123456', 'C县分公司经理', 'MANAGER', 'DEPT_MANAGER', 150, 100, 1),
(151, 'c_net_user', '123456', 'C县网络部员工', 'USER', 'NETWORK_ENGINEER', 153, 150, 1),
(152, 'c_gov_user', '123456', 'C县政企部员工', 'USER', 'DICT_PM', 154, 150, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `primary_role` = VALUES(`primary_role`);

-- =======================================================
-- 4. 用户角色关联初始化
-- =======================================================

INSERT INTO `sys_user_role` (`user_id`, `role_code`, `effective_dept_id`, `is_primary`) VALUES 
-- 省级管理员
(1, 'SYSTEM_ADMIN', 1, 1),

-- A市领导
(100, 'GENERAL_MANAGER', 100, 1),
(101, 'VICE_PRESIDENT', 100, 1),

-- A市网络部
(110, 'DEPT_MANAGER', 101, 1),
(111, 'RF_ENGINEER', 101, 1),
(112, 'NETWORK_ENGINEER', 101, 1),
(113, 'PROJECT_MANAGER', 101, 1),
(113, 'NETWORK_PLANNING', 101, 0),  -- 项目经理兼任网络规划

-- A市法务部
(120, 'DEPT_MANAGER', 104, 1),
(121, 'LEGAL_REVIEWER', 104, 1),

-- A市财务部
(130, 'DEPT_MANAGER', 105, 1),
(131, 'COST_AUDITOR', 105, 1),
(132, 'FINANCE_RECEIVABLE', 105, 1),

-- A市政企部
(140, 'DEPT_MANAGER', 103, 1),
(141, 'SOLUTION_ARCHITECT', 103, 1),
(142, 'DICT_PM', 103, 1),

-- A市采购部
(145, 'DEPT_MANAGER', 107, 1),
(146, 'PROCUREMENT_SPECIALIST', 107, 1),
(147, 'VENDOR_MANAGER', 107, 1),

-- A市市场部（补充）
(155, 'DEPT_MANAGER', 102, 1),
(156, 'CUSTOMER_SERVICE_LEAD', 102, 1),

-- A市综合部（补充）
(158, 'DEPT_MANAGER', 106, 1),
(159, 'FACILITY_COORDINATOR', 106, 1),

-- 省级职能部门（补充）
(20, 'DEPT_MANAGER', 10, 1),
(21, 'DEPT_MANAGER', 13, 1),
(22, 'LEGAL_REVIEWER', 13, 1),
(23, 'DEPT_MANAGER', 14, 1),
(24, 'DEPT_MANAGER', 12, 1),

-- C县人员（使用实际职位角色）
(150, 'DEPT_MANAGER', 150, 1),
(151, 'NETWORK_ENGINEER', 153, 1),
(152, 'DICT_PM', 154, 1)

ON DUPLICATE KEY UPDATE `is_primary` = VALUES(`is_primary`);

-- =======================================================
-- 5. 权限定义初始化
-- =======================================================

INSERT INTO `sys_permission` (`code`, `name`) VALUES 
('user:manage', '用户管理'),
('dept:manage', '组织架构管理'),
('contract:create', '创建合同'),
('contract:view', '查看合同'),
('contract:edit', '编辑合同'),
('contract:submit', '提交合同'),
('contract:approve', '审批合同'),
('workflow:manage', '工作流管理'),
('system:config', '系统配置')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- =======================================================
-- 6. 审批场景配置初始化（Master Workflow矩阵）
-- =======================================================

-- Type A - 工程施工合同
INSERT INTO `wf_scenario_config` (`scenario_id`, `sub_type_code`, `sub_type_name`, `amount_min`, `amount_max`, `is_fast_track`, `description`) VALUES 
('A1-Tier1', 'A1', '土建工程', 0, 10000, 0, '土建工程<1万，标准路径'),
('A1-Tier2', 'A1', '土建工程', 10000, 50000, 0, '土建工程1-5万，需成本审计'),
('A1-Tier3', 'A1', '土建工程', 50000, 500000, 0, '土建工程5-50万，需法务审查'),
('A1-Tier4', 'A1', '土建工程', 500000, NULL, 0, '土建工程>50万，需三重一大'),
('A2-Tier1', 'A2', '装修工程', 0, 10000, 0, '装修工程<1万，标准路径'),
('A2-Tier2', 'A2', '装修工程', 10000, 50000, 0, '装修工程1-5万，需设计审查'),
('A2-Tier3', 'A2', '装修工程', 50000, 500000, 0, '装修工程5-50万，需法务审查'),
('A2-Tier4', 'A2', '装修工程', 500000, NULL, 0, '装修工程>50万，需三重一大'),
('A3-Tier1', 'A3', '零星维修', 0, 10000, 0, '零星维修<1万，标准路径'),
('A3-Tier2', 'A3', '零星维修', 10000, 50000, 0, '零星维修1-5万，升级审批')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- Type B - 代维服务合同
INSERT INTO `wf_scenario_config` (`scenario_id`, `sub_type_code`, `sub_type_name`, `amount_min`, `amount_max`, `is_fast_track`, `description`) VALUES 
('B1-Tier1', 'B1', '光缆代维', 0, 10000, 0, '光缆代维<1万，标准路径'),
('B1-Tier2', 'B1', '光缆代维', 10000, 50000, 0, '光缆代维1-5万，需网络规划'),
('B1-Tier3', 'B1', '光缆代维', 50000, 500000, 0, '光缆代维5-50万，需法务审查'),
('B2-Tier1', 'B2', '基站代维', 0, 10000, 0, '基站代维<1万，标准路径'),
('B2-Tier2', 'B2', '基站代维', 10000, 50000, 0, '基站代维1-5万，需站点获取'),
('B2-Tier3', 'B2', '基站代维', 50000, 500000, 0, '基站代维5-50万，需法务审查'),
('B2-Tier4', 'B2', '基站代维', 500000, NULL, 0, '基站代维>50万，需三重一大'),
('B3-Tier1', 'B3', '家宽代维', 0, 10000, 0, '家宽代维<1万，标准路径'),
('B3-Tier2', 'B3', '家宽代维', 10000, 50000, 0, '家宽代维1-5万，需客服主管'),
('B4-Tier1', 'B4', '应急保障', 0, 10000, 1, '应急保障<1万，快速通道'),
('B4-Tier2', 'B4', '应急保障', 10000, 50000, 1, '应急保障1-5万，快速通道'),
('B4-Tier3', 'B4', '应急保障', 50000, 500000, 1, '应急保障5-50万，快速通道+法务'),
('B4-Tier4', 'B4', '应急保障', 500000, NULL, 1, '应急保障>50万，快速通道+三重一大')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- Type C - IT/DICT合同
INSERT INTO `wf_scenario_config` (`scenario_id`, `sub_type_code`, `sub_type_name`, `amount_min`, `amount_max`, `is_fast_track`, `description`) VALUES 
('C1-Tier1', 'C1', '定制开发', 0, 10000, 0, '定制开发<1万，需安全审查'),
('C1-Tier2', 'C1', '定制开发', 10000, 50000, 0, '定制开发1-5万，需IT架构'),
('C1-Tier3', 'C1', '定制开发', 50000, 500000, 0, '定制开发5-50万，需法务审查'),
('C1-Tier4', 'C1', '定制开发', 500000, NULL, 0, '定制开发>50万，需三重一大'),
('C2-Tier1', 'C2', '商用软件采购', 0, 10000, 0, '软件采购<1万，需供应商管理'),
('C2-Tier2', 'C2', '商用软件采购', 10000, 50000, 0, '软件采购1-5万，需IT安全'),
('C2-Tier3', 'C2', '商用软件采购', 50000, 500000, 0, '软件采购5-50万，需法务审查'),
('C3-Tier1', 'C3', 'DICT集成', 0, 10000, 0, 'DICT集成<1万，需财务应收检查'),
('C3-Tier2', 'C3', 'DICT集成', 10000, 50000, 0, 'DICT集成1-5万，需DICT项目经理'),
('C3-Tier3', 'C3', 'DICT集成', 50000, 500000, 0, 'DICT集成5-50万，需法务审查'),
('C3-Tier4', 'C3', 'DICT集成', 500000, NULL, 0, 'DICT集成>50万，需三重一大')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- =======================================================
-- 7. 审批节点配置初始化（示例：A1场景）
-- =======================================================

-- A1-Tier1: 土建工程 < 1万
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A1-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A1-Tier1', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('A1-Tier1', 3, 'DEPT_MANAGER', 'CITY', 'APPROVE', 1)
ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- A1-Tier2: 土建工程 1-5万
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A1-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A1-Tier2', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('A1-Tier2', 3, 'COST_AUDITOR', 'CITY', 'VERIFY', 1),
('A1-Tier2', 4, 'DEPT_MANAGER', 'CITY', 'APPROVE', 1),
('A1-Tier2', 5, 'VICE_PRESIDENT', 'CITY', 'APPROVE', 1)
ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- A1-Tier3: 土建工程 5-50万
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A1-Tier3', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A1-Tier3', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('A1-Tier3', 3, 'COST_AUDITOR', 'CITY', 'VERIFY', 1),
('A1-Tier3', 4, 'LEGAL_REVIEWER', 'CITY', 'REVIEW', 1),
('A1-Tier3', 5, 'DEPT_MANAGER', 'CITY', 'APPROVE', 1),
('A1-Tier3', 6, 'VICE_PRESIDENT', 'CITY', 'APPROVE', 1),
('A1-Tier3', 7, 'GENERAL_MANAGER', 'CITY', 'FINAL_APPROVE', 1)
ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- B2-Tier2: 基站代维 1-5万（示例）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B2-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B2-Tier2', 2, 'RF_ENGINEER', 'CITY', 'REVIEW', 1),
('B2-Tier2', 3, 'SITE_ACQUISITION', 'CITY', 'REVIEW', 1),
('B2-Tier2', 4, 'DEPT_MANAGER', 'CITY', 'APPROVE', 1),
('B2-Tier2', 5, 'VICE_PRESIDENT', 'CITY', 'APPROVE', 1)
ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- C3-Tier2: DICT集成 1-5万（示例）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C3-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C3-Tier2', 2, 'SOLUTION_ARCHITECT', 'CITY', 'REVIEW', 1),
('C3-Tier2', 3, 'FINANCE_RECEIVABLE', 'CITY', 'VERIFY', 1),
('C3-Tier2', 4, 'DICT_PM', 'CITY', 'REVIEW', 1),
('C3-Tier2', 5, 'DEPT_MANAGER', 'CITY', 'APPROVE', 1),
('C3-Tier2', 6, 'VICE_PRESIDENT', 'CITY', 'APPROVE', 1)
ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- =======================================================
-- 8. 知识库初始数据
-- =======================================================

INSERT INTO `t_knowledge_base` (`title`, `content`, `type`) VALUES 
('电信基础设施共建共享管理办法', '第三条 电信基础设施共建共享应当遵循统筹规划、需求导向、资源节约、保障安全的原则...', 'REGULATION'),
('基站租赁合同标准条款-电磁辐射', '出租方已知悉基站设备产生的电磁辐射符合国家标准（GB 8702-2014），不影响居住环境安全。', 'TERM'),
('不可抗力条款(电信版)', '因自然灾害、政府行为、通信光缆中断等不可抗力因素导致服务中断，双方互不承担违约责任。', 'TERM'),
('中国移动工程施工合同标准模板', '本合同适用于通信工程施工类项目，包含但不限于土建工程、装修工程、零星维修等。', 'TEMPLATE'),
('中国移动代维服务合同标准模板', '本合同适用于网络代维服务类项目，包含但不限于光缆代维、基站代维、家宽代维等。', 'TEMPLATE'),
('中国移动IT服务合同标准模板', '本合同适用于IT及DICT类项目，包含但不限于定制开发、软件采购、DICT集成等。', 'TEMPLATE')
ON DUPLICATE KEY UPDATE `content` = VALUES(`content`);

-- =======================================================
-- 完成提示
-- =======================================================
SELECT 'init_data_v2 初始化完成' AS message;
