/*
 Description: 最终修复角色问题
 Purpose: 
   1. 移除所有高管角色节点（VICE_PRESIDENT, GENERAL_MANAGER），不替换为部门经理，直接删除或替换为其他职位
   2. 修复所有重复职位问题
 Version: v17
 Date: 2025-12-22
*/

USE contract_system;

-- =======================================================
-- 0. 清理现有场景节点数据（保留场景配置）
-- =======================================================
DELETE FROM wf_scenario_node;

-- =======================================================
-- 1. 添加兜底场景配置
-- =======================================================
INSERT INTO `wf_scenario_config` (`scenario_id`, `sub_type_code`, `sub_type_name`, `amount_min`, `amount_max`, `is_fast_track`, `description`) VALUES 
('FALLBACK-DEFAULT', 'FALLBACK', '通用兜底', 0, NULL, 0, '未匹配到任何场景时的通用审批流程')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- =======================================================
-- 2. Type A - 工程施工合同（10个场景）
-- =======================================================

-- A1-Tier1: 土建工程 < 1万
-- 路径: 发起人 → 项目经理
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A1-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A1-Tier1', 2, 'PROJECT_MANAGER', 'CITY', 'APPROVE', 1);

-- A1-Tier2: 土建工程 1-5万
-- 路径: 发起人 → 项目经理 → 成本审计（原高管节点删除）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A1-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A1-Tier2', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('A1-Tier2', 3, 'COST_AUDITOR', 'CITY', 'APPROVE', 1);

-- A1-Tier3: 土建工程 5-50万
-- 路径: 发起人 → 项目经理 → 成本审计 → 法务审查（原高管节点删除）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A1-Tier3', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A1-Tier3', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('A1-Tier3', 3, 'COST_AUDITOR', 'CITY', 'APPROVE', 1),
('A1-Tier3', 4, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- A1-Tier4: 土建工程 > 50万
-- 路径: 发起人 → 项目经理 → 成本审计 → 法务审查（原高管节点删除）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A1-Tier4', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A1-Tier4', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('A1-Tier4', 3, 'COST_AUDITOR', 'CITY', 'APPROVE', 1),
('A1-Tier4', 4, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- A2-Tier1: 装修工程 < 1万
-- 路径: 发起人 → 项目经理
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A2-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A2-Tier1', 2, 'PROJECT_MANAGER', 'CITY', 'APPROVE', 1);

-- A2-Tier2: 装修工程 1-5万
-- 路径: 发起人 → 项目经理 → 成本审计（替换第二个项目经理，删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A2-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A2-Tier2', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('A2-Tier2', 3, 'COST_AUDITOR', 'CITY', 'APPROVE', 1);

-- A2-Tier3: 装修工程 5-50万
-- 路径: 发起人 → 项目经理 → 成本审计（替换第二个项目经理） → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A2-Tier3', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A2-Tier3', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('A2-Tier3', 3, 'COST_AUDITOR', 'CITY', 'REVIEW', 1),
('A2-Tier3', 4, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- A2-Tier4: 装修工程 > 50万
-- 路径: 发起人 → 项目经理 → 成本审计（替换第二个项目经理） → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A2-Tier4', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A2-Tier4', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('A2-Tier4', 3, 'COST_AUDITOR', 'CITY', 'REVIEW', 1),
('A2-Tier4', 4, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- A3-Tier1: 零星维修 < 1万
-- 路径: 发起人 → 设施协调员
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A3-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A3-Tier1', 2, 'FACILITY_COORDINATOR', 'CITY', 'APPROVE', 1);

-- A3-Tier2: 零星维修 1-5万
-- 路径: 发起人 → 设施协调员（删除高管节点）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('A3-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('A3-Tier2', 2, 'FACILITY_COORDINATOR', 'CITY', 'APPROVE', 1);

-- =======================================================
-- 3. Type B - 代维服务合同（13个场景）
-- =======================================================

-- B1-Tier1: 光缆代维 < 1万
-- 路径: 发起人 → 网络工程师
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B1-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B1-Tier1', 2, 'NETWORK_ENGINEER', 'CITY', 'APPROVE', 1);

-- B1-Tier2: 光缆代维 1-5万
-- 路径: 发起人 → 网络工程师 → 项目经理（网络规划改为项目经理，删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B1-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B1-Tier2', 2, 'NETWORK_ENGINEER', 'CITY', 'REVIEW', 1),
('B1-Tier2', 3, 'PROJECT_MANAGER', 'CITY', 'APPROVE', 1);

-- B1-Tier3: 光缆代维 5-50万
-- 路径: 发起人 → 网络工程师 → 项目经理（网络规划改为项目经理） → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B1-Tier3', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B1-Tier3', 2, 'NETWORK_ENGINEER', 'CITY', 'REVIEW', 1),
('B1-Tier3', 3, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('B1-Tier3', 4, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- B2-Tier1: 基站代维 < 1万
-- 路径: 发起人 → 射频工程师
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B2-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B2-Tier1', 2, 'RF_ENGINEER', 'CITY', 'APPROVE', 1);

-- B2-Tier2: 基站代维 1-5万
-- 路径: 发起人 → 射频工程师 → 网络工程师（替换第二个射频工程师，删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B2-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B2-Tier2', 2, 'RF_ENGINEER', 'CITY', 'REVIEW', 1),
('B2-Tier2', 3, 'NETWORK_ENGINEER', 'CITY', 'APPROVE', 1);

-- B2-Tier3: 基站代维 5-50万
-- 路径: 发起人 → 射频工程师 → 网络工程师（替换第二个射频工程师） → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B2-Tier3', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B2-Tier3', 2, 'RF_ENGINEER', 'CITY', 'REVIEW', 1),
('B2-Tier3', 3, 'NETWORK_ENGINEER', 'CITY', 'REVIEW', 1),
('B2-Tier3', 4, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- B2-Tier4: 基站代维 > 50万
-- 路径: 发起人 → 射频工程师 → 网络工程师（替换第二个射频工程师） → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B2-Tier4', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B2-Tier4', 2, 'RF_ENGINEER', 'CITY', 'REVIEW', 1),
('B2-Tier4', 3, 'NETWORK_ENGINEER', 'CITY', 'REVIEW', 1),
('B2-Tier4', 4, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- B3-Tier1: 家宽代维 < 1万
-- 路径: 发起人 → 网络工程师
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B3-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B3-Tier1', 2, 'NETWORK_ENGINEER', 'CITY', 'APPROVE', 1);

-- B3-Tier2: 家宽代维 1-5万
-- 路径: 发起人 → 网络工程师 → 客服主管（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B3-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B3-Tier2', 2, 'NETWORK_ENGINEER', 'CITY', 'REVIEW', 1),
('B3-Tier2', 3, 'CUSTOMER_SERVICE_LEAD', 'CITY', 'APPROVE', 1);

-- B4-Tier1: 应急保障 < 1万 【快速通道】
-- 路径: 发起人 → 项目经理
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B4-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B4-Tier1', 2, 'PROJECT_MANAGER', 'CITY', 'APPROVE', 1);

-- B4-Tier2: 应急保障 1-5万 【快速通道】
-- 路径: 发起人 → 项目经理（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B4-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B4-Tier2', 2, 'PROJECT_MANAGER', 'CITY', 'APPROVE', 1);

-- B4-Tier3: 应急保障 5-50万 【快速通道】
-- 路径: 发起人 → 项目经理 → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B4-Tier3', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B4-Tier3', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('B4-Tier3', 3, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- B4-Tier4: 应急保障 > 50万 【快速通道】
-- 路径: 发起人 → 项目经理 → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('B4-Tier4', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('B4-Tier4', 2, 'PROJECT_MANAGER', 'CITY', 'REVIEW', 1),
('B4-Tier4', 3, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- =======================================================
-- 4. Type C - IT/DICT合同（11个场景）
-- =======================================================

-- C1-Tier1: 定制开发 < 1万
-- 路径: 发起人 → 解决方案架构师 → 法务审查
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C1-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C1-Tier1', 2, 'SOLUTION_ARCHITECT', 'CITY', 'REVIEW', 1),
('C1-Tier1', 3, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- C1-Tier2: 定制开发 1-5万
-- 路径: 发起人 → 解决方案架构师 → 法务审查 → DICT项目经理（替换第二个解决方案架构师，删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C1-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C1-Tier2', 2, 'SOLUTION_ARCHITECT', 'CITY', 'REVIEW', 1),
('C1-Tier2', 3, 'LEGAL_REVIEWER', 'CITY', 'REVIEW', 1),
('C1-Tier2', 4, 'DICT_PM', 'CITY', 'APPROVE', 1);

-- C1-Tier3: 定制开发 5-50万
-- 路径: 发起人 → 解决方案架构师 → 法务审查 → DICT项目经理（替换第二个解决方案架构师） → 成本审计（替换第二个法务审查，删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C1-Tier3', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C1-Tier3', 2, 'SOLUTION_ARCHITECT', 'CITY', 'REVIEW', 1),
('C1-Tier3', 3, 'LEGAL_REVIEWER', 'CITY', 'REVIEW', 1),
('C1-Tier3', 4, 'DICT_PM', 'CITY', 'REVIEW', 1),
('C1-Tier3', 5, 'COST_AUDITOR', 'CITY', 'APPROVE', 1);

-- C1-Tier4: 定制开发 > 50万
-- 路径: 发起人 → 解决方案架构师 → 法务审查 → DICT项目经理（替换第二个解决方案架构师） → 成本审计（替换第二个法务审查，删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C1-Tier4', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C1-Tier4', 2, 'SOLUTION_ARCHITECT', 'CITY', 'REVIEW', 1),
('C1-Tier4', 3, 'LEGAL_REVIEWER', 'CITY', 'REVIEW', 1),
('C1-Tier4', 4, 'DICT_PM', 'CITY', 'REVIEW', 1),
('C1-Tier4', 5, 'COST_AUDITOR', 'CITY', 'APPROVE', 1);

-- C2-Tier1: 商用软件采购 < 1万
-- 路径: 发起人 → 采购专员 → 供应商管理
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C2-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C2-Tier1', 2, 'PROCUREMENT_SPECIALIST', 'CITY', 'REVIEW', 1),
('C2-Tier1', 3, 'VENDOR_MANAGER', 'CITY', 'APPROVE', 1);

-- C2-Tier2: 商用软件采购 1-5万
-- 路径: 发起人 → 采购专员 → 供应商管理 → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C2-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C2-Tier2', 2, 'PROCUREMENT_SPECIALIST', 'CITY', 'REVIEW', 1),
('C2-Tier2', 3, 'VENDOR_MANAGER', 'CITY', 'REVIEW', 1),
('C2-Tier2', 4, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- C2-Tier3: 商用软件采购 5-50万
-- 路径: 发起人 → 采购专员 → 供应商管理 → 法务审查 → 成本审计（替换第二个法务审查，删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C2-Tier3', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C2-Tier3', 2, 'PROCUREMENT_SPECIALIST', 'CITY', 'REVIEW', 1),
('C2-Tier3', 3, 'VENDOR_MANAGER', 'CITY', 'REVIEW', 1),
('C2-Tier3', 4, 'LEGAL_REVIEWER', 'CITY', 'REVIEW', 1),
('C2-Tier3', 5, 'COST_AUDITOR', 'CITY', 'APPROVE', 1);

-- C3-Tier1: DICT集成 < 1万
-- 路径: 发起人 → 解决方案架构师 → 财务应收检查
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C3-Tier1', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C3-Tier1', 2, 'SOLUTION_ARCHITECT', 'CITY', 'REVIEW', 1),
('C3-Tier1', 3, 'FINANCE_RECEIVABLE', 'CITY', 'APPROVE', 1);

-- C3-Tier2: DICT集成 1-5万
-- 路径: 发起人 → 解决方案架构师 → 财务应收检查 → DICT项目经理（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C3-Tier2', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C3-Tier2', 2, 'SOLUTION_ARCHITECT', 'CITY', 'REVIEW', 1),
('C3-Tier2', 3, 'FINANCE_RECEIVABLE', 'CITY', 'REVIEW', 1),
('C3-Tier2', 4, 'DICT_PM', 'CITY', 'APPROVE', 1);

-- C3-Tier3: DICT集成 5-50万
-- 路径: 发起人 → 解决方案架构师 → 财务应收检查 → DICT项目经理 → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C3-Tier3', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C3-Tier3', 2, 'SOLUTION_ARCHITECT', 'CITY', 'REVIEW', 1),
('C3-Tier3', 3, 'FINANCE_RECEIVABLE', 'CITY', 'REVIEW', 1),
('C3-Tier3', 4, 'DICT_PM', 'CITY', 'REVIEW', 1),
('C3-Tier3', 5, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- C3-Tier4: DICT集成 > 50万
-- 路径: 发起人 → 解决方案架构师 → 财务应收检查 → DICT项目经理 → 法务审查（删除高管）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('C3-Tier4', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('C3-Tier4', 2, 'SOLUTION_ARCHITECT', 'CITY', 'REVIEW', 1),
('C3-Tier4', 3, 'FINANCE_RECEIVABLE', 'CITY', 'REVIEW', 1),
('C3-Tier4', 4, 'DICT_PM', 'CITY', 'REVIEW', 1),
('C3-Tier4', 5, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- =======================================================
-- 5. 兜底场景节点配置
-- =======================================================

-- FALLBACK-DEFAULT: 通用兜底审批流程
-- 路径: 发起人 → 法务审查（删除高管节点）
INSERT INTO `wf_scenario_node` (`scenario_id`, `node_order`, `role_code`, `node_level`, `action_type`, `is_mandatory`) VALUES 
('FALLBACK-DEFAULT', 1, 'INITIATOR', 'COUNTY', 'INITIATE', 1),
('FALLBACK-DEFAULT', 2, 'LEGAL_REVIEWER', 'CITY', 'APPROVE', 1);

-- =======================================================
-- 6. 验证数据完整性
-- =======================================================
SELECT 
    '场景配置统计' AS category,
    COUNT(DISTINCT scenario_id) AS total_scenarios 
FROM wf_scenario_config WHERE is_active = 1;

SELECT 
    '节点配置统计' AS category,
    COUNT(*) AS total_nodes,
    COUNT(DISTINCT scenario_id) AS scenarios_with_nodes
FROM wf_scenario_node;

-- 验证每个场景的节点数
SELECT 
    scenario_id,
    COUNT(*) AS node_count,
    MIN(node_order) AS first_node,
    MAX(node_order) AS last_node
FROM wf_scenario_node 
GROUP BY scenario_id 
ORDER BY scenario_id;

-- 检查是否还有高管角色
SELECT 
    '高管角色检查' AS category,
    role_code,
    COUNT(*) AS count
FROM wf_scenario_node
WHERE role_code IN ('VICE_PRESIDENT', 'GENERAL_MANAGER', 'DEPT_MANAGER', 'T1M')
GROUP BY role_code;

SELECT 'db_upgrade_v17_fix_roles_final 执行完成！已移除所有高管角色，修复所有重复职位问题' AS message;

