-- Active: 1765803264184@@118.31.77.102@3306@contract_system
/*
 Description: 02_全量数据初始化 (Data)
*/
USE contract_system;

-- ==================== 1. 基础数据：部门 (sys_dept) ====================
-- 注意：补充了 code, type, level 字段
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `manager_id`) VALUES 
(1, 0, '中国电信XX省分公司', 'ROOT', 'PROVINCE', 1, NULL),
(2, 1, '网络建设部', 'NET-DEPT', 'DEPT', 2, 5), -- 经理是 zhang_manager(id=5)
(3, 1, '采购供应部', 'PUR-DEPT', 'DEPT', 2, NULL),
(4, 1, '法律事务部', 'LEGAL-DEPT', 'DEPT', 2, 4), -- 经理是 wang_legal(id=4)
(5, 1, '财务部', 'FIN-DEPT', 'DEPT', 2, NULL);

-- ==================== 2. 基础数据：用户 (sys_user) ====================
-- 密码均为 123456
-- 注意：补充了 direct_leader_id 逻辑
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `dept_id`, `direct_leader_id`) VALUES 
(1, 'admin', '123456', '系统管理员', 'ADMIN', 1, 6),        -- 管理员 (上级设为BOSS)
(2, 'user', '123456', '普通用户', 'USER', 2, 5),            -- 普通用户 (上级是经理)
(3, 'li_project', '123456', '李项目', 'USER', 2, 5),    -- 发起人 (上级是经理)
(4, 'wang_legal', '123456', '王法务', 'LEGAL', 4, 6),   -- 法务 (上级是BOSS)
(5, 'zhang_manager', '123456', '张经理', 'MANAGER', 2, 6), -- 经理 (上级是BOSS)
(6, 'zhao_boss', '123456', '赵副总', 'BOSS', 1, NULL);     -- BOSS (无上级)

-- ==================== 3. RBAC：权限定义 (sys_permission) ====================
INSERT INTO `sys_permission` (`code`, `name`) VALUES 
('user:manage', '用户管理'),
('contract:add', '创建合同'),
('contract:view', '查看合同'),
('contract:audit', '审批合同'),
('risk:review', '风险审查');

-- ==================== 4. RBAC：角色权限分配 (sys_role_permission) ====================
-- ADMIN (user:manage)
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'ADMIN', id FROM `sys_permission` WHERE code = 'user:manage';

-- USER (add, view)
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'USER', id FROM `sys_permission` WHERE code IN ('contract:add', 'contract:view');

-- MANAGER (add, view, audit)
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'MANAGER', id FROM `sys_permission` WHERE code IN ('contract:add', 'contract:view', 'contract:audit');

-- LEGAL (view, audit, risk:review)
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'LEGAL', id FROM `sys_permission` WHERE code IN ('contract:view', 'contract:audit', 'risk:review');

-- BOSS (view, audit)
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'BOSS', id FROM `sys_permission` WHERE code IN ('contract:view', 'contract:audit');

-- ==================== 5. 工作流：流程定义 (wf_definition) ====================
INSERT INTO `wf_definition` (`id`, `name`, `apply_type`, `version`, `is_active`) VALUES 
(1, '基站租赁合同标准审批', 'BASE_STATION', 1, 1);

-- ==================== 6. 工作流：节点 (wf_node) ====================
INSERT INTO `wf_node` (`def_id`, `node_code`, `node_name`, `type`, `approver_type`, `approver_value`) VALUES 
(1, 'node_1', '发起提交', 'START', NULL, NULL),
(1, 'node_2', '部门经理审批', 'APPROVE', 'ROLE', 'MANAGER'),
(1, 'node_3', '法务合规审查', 'APPROVE', 'ROLE', 'LEGAL'),
(1, 'node_4', '流程结束', 'END', NULL, NULL);

-- ==================== 7. 工作流：连线 (wf_transition) ====================
INSERT INTO `wf_transition` (`def_id`, `from_node_id`, `to_node_id`) VALUES 
(1, 1, 2), -- 发起 -> 经理
(1, 2, 3), -- 经理 -> 法务
(1, 3, 4); -- 法务 -> 结束

-- ==================== 8. 知识库：初始数据 (t_knowledge_base) ====================
INSERT INTO `t_knowledge_base` (`title`, `content`, `type`) VALUES 
('电信基础设施共建共享管理办法', '第三条 电信基础设施共建共享应当遵循统筹规划、需求导向、资源节约、保障安全的原则...', 'REGULATION'),
('基站租赁合同标准条款-电磁辐射', '出租方已知悉基站设备产生的电磁辐射符合国家标准（GB 8702-2014），不影响居住环境安全。', 'TERM'),
('不可抗力条款(电信版)', '因自然灾害、政府行为、通信光缆中断等不可抗力因素导致服务中断，双方互不承担违约责任。', 'TERM');