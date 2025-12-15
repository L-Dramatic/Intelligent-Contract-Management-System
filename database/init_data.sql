USE contract_system;

-- 1. 清理旧数据 (可选，如果你想重置的话)
-- TRUNCATE TABLE sys_dept;
-- TRUNCATE TABLE sys_user;

-- 2. 初始化部门 (模拟真实电信架构)
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `manager_id`) VALUES 
(1, 0, '中国电信XX省分公司', NULL),
(2, 1, '网络建设部', NULL),
(3, 1, '采购供应部', NULL),
(4, 1, '法律事务部', NULL),
(5, 1, '财务部', NULL);

-- 3. 初始化用户 (覆盖不同角色)
-- 密码均为 123456 (假设加密逻辑暂未开启，或你需要手动 update 为加密串)
-- 提示：如果你的注册逻辑用了 BCrypt，这里的密码在登录时可能无法通过，建议后续用注册接口创建，或者暂时只用之前注册好的账号。
-- 这里仅作为参考数据结构。

-- 增加前端默认演示账号：admin / 123456、user / 123456
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `dept_id`) VALUES 
('admin', '123456', '系统管理员', 'ADMIN', 1),        -- 管理员
('user', '123456', '普通用户', 'USER', 2),            -- 普通用户
('li_project', '123456', '李项目', 'USER', 2),    -- 网络部项目经理 (发起人)
('wang_legal', '123456', '王法务', 'LEGAL', 4),   -- 法务 (审查人)
('zhang_manager', '123456', '张经理', 'MANAGER', 2), -- 部门经理 (审批人)
('zhao_boss', '123456', '赵副总', 'BOSS', 1);     -- 公司领导 (终审)

-- 4. 初始化流程定义 (基站租赁合同审批流程)
INSERT INTO `wf_definition` (`name`, `apply_type`, `version`, `is_active`) VALUES 
('基站租赁合同标准审批', 'BASE_STATION', 1, 1);

-- 获取刚才插入的流程ID (假设是 1)
-- 插入节点：开始 -> 部门经理 -> 法务 -> 结束
INSERT INTO `wf_node` (`def_id`, `node_code`, `node_name`, `type`, `approver_type`, `approver_value`) VALUES 
(1, 'node_1', '发起提交', 'START', NULL, NULL),
(1, 'node_2', '部门经理审批', 'APPROVE', 'ROLE', 'MANAGER'),
(1, 'node_3', '法务合规审查', 'APPROVE', 'ROLE', 'LEGAL'),
(1, 'node_4', '流程结束', 'END', NULL, NULL);

-- 插入连线
INSERT INTO `wf_transition` (`def_id`, `from_node_id`, `to_node_id`) VALUES 
(1, 1, 2), -- 发起 -> 经理
(1, 2, 3), -- 经理 -> 法务
(1, 3, 4); -- 法务 -> 结束

-- 5. 初始化知识库 (RAG 数据源预览)
INSERT INTO `t_knowledge_base` (`title`, `content`, `type`) VALUES 
('电信基础设施共建共享管理办法', '第三条 电信基础设施共建共享应当遵循统筹规划、需求导向、资源节约、保障安全的原则...', 'REGULATION'),
('基站租赁合同标准条款-电磁辐射', '出租方已知悉基站设备产生的电磁辐射符合国家标准（GB 8702-2014），不影响居住环境安全。', 'TERM'),
('不可抗力条款(电信版)', '因自然灾害、政府行为、通信光缆中断等不可抗力因素导致服务中断，双方互不承担违约责任。', 'TERM');