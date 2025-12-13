USE contract_system;

-- 1. 权限定义表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '权限标识 (如 user:list)',
  `name` varchar(50) NOT NULL COMMENT '权限名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) COMMENT='权限定义表';

-- 2. 角色-权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(20) NOT NULL COMMENT '角色代码 (对应 sys_user.role)',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`)
) COMMENT='角色权限关联表';

-- ==========================================
-- 3. 初始化权限数据 (这就是你系统的"能力清单")
-- ==========================================
INSERT INTO `sys_permission` (`code`, `name`) VALUES 
('user:manage', '用户管理'),      -- 只有管理员有
('contract:add', '创建合同'),     -- 员工、经理有
('contract:view', '查看合同'),    -- 所有人都有
('contract:audit', '审批合同'),   -- 经理、法务、BOSS有
('risk:review', '风险审查');      -- 只有法务有

-- ==========================================
-- 4. 配置角色权限 (把能力分配给角色)
-- ==========================================

-- ADMIN (管理员): 拥有用户管理权限
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'ADMIN', id FROM `sys_permission` WHERE code = 'user:manage';

-- USER (普通员工/项目经理): 可以创建、查看
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'USER', id FROM `sys_permission` WHERE code IN ('contract:add', 'contract:view');

-- MANAGER (部门经理): 可以创建、查看、审批
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'MANAGER', id FROM `sys_permission` WHERE code IN ('contract:add', 'contract:view', 'contract:audit');

-- LEGAL (法务): 可以查看、审批、风险审查
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'LEGAL', id FROM `sys_permission` WHERE code IN ('contract:view', 'contract:audit', 'risk:review');

-- BOSS (大领导): 可以查看、审批
INSERT INTO `sys_role_permission` (`role_code`, `permission_id`)
SELECT 'BOSS', id FROM `sys_permission` WHERE code IN ('contract:view', 'contract:audit');