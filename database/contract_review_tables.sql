-- Active: 1765636755796@@127.0.0.1@3306@contract_system
/*
 * 合同审查相关表结构
 * 
 * 包含：
 * 1. t_contract_review - 合同审查报告表
 * 2. t_contract_review_rule - 审查规则配置表
 */

USE contract_system;

-- =======================================================
-- 1. 合同审查报告表
-- =======================================================
DROP TABLE IF EXISTS `t_contract_review`;

CREATE TABLE `t_contract_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `contract_id` bigint NOT NULL COMMENT '关联的合同ID',
  `risk_level` varchar(20) DEFAULT NULL COMMENT '风险等级: LOW, MEDIUM, HIGH',
  `score` int DEFAULT NULL COMMENT '综合评分(0-100)',
  `review_content` json DEFAULT NULL COMMENT '审查内容(JSON格式，包含风险项列表)',
  `review_type` varchar(20) NOT NULL DEFAULT 'AUTO' COMMENT '审查类型: AUTO(自动), MANUAL(手动)',
  `rag_used` tinyint(1) DEFAULT 1 COMMENT '是否使用了RAG检索',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING, COMPLETED, FAILED',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `triggered_by` bigint DEFAULT NULL COMMENT '触发人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同审查报告表';

-- =======================================================
-- 2. 审查规则配置表
-- =======================================================
DROP TABLE IF EXISTS `t_contract_review_rule`;

CREATE TABLE `t_contract_review_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `contract_type` varchar(20) NOT NULL COMMENT '合同类型: TYPE_A, TYPE_B, TYPE_C',
  `rule_category` varchar(20) NOT NULL COMMENT '规则类别: ATTACHMENT(附件), LOGIC(逻辑), RISK(风险)',
  `rule_code` varchar(50) NOT NULL COMMENT '规则编码: DOC_A_001, RISK_A_001等',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_config` json NOT NULL COMMENT '规则配置(JSON格式，包含关键词、阈值、条件等)',
  `mandate_level` varchar(20) NOT NULL DEFAULT 'HIGH' COMMENT '强制级别: CRITICAL(阻断), HIGH(警告需确认), MEDIUM(警告), LOW(提示)',
  `priority` int DEFAULT 100 COMMENT '优先级(数值越小优先级越高)',
  `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `description` varchar(500) DEFAULT NULL COMMENT '规则描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`),
  KEY `idx_contract_type` (`contract_type`),
  KEY `idx_rule_category` (`rule_category`),
  KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同审查规则配置表';

-- =======================================================
-- 3. 初始化Type A规则数据（工程施工合同）
-- =======================================================
INSERT INTO `t_contract_review_rule` 
(`contract_type`, `rule_category`, `rule_code`, `rule_name`, `rule_config`, `mandate_level`, `priority`, `description`) 
VALUES 
('TYPE_A', 'ATTACHMENT', 'DOC_A_001', '安全生产协议', 
 '{"keywords": ["安全生产协议", "Safety Responsibility Agreement"], "filePattern": ".*安全生产.*"}', 
 'CRITICAL', 10, '工程施工合同必须包含安全生产协议'),

('TYPE_A', 'ATTACHMENT', 'DOC_A_002', '廉洁诚信承诺', 
 '{"keywords": ["廉洁诚信", "Integrity Pact"], "filePattern": ".*廉洁.*"}', 
 'CRITICAL', 10, '工程施工合同必须包含廉洁诚信承诺'),

('TYPE_A', 'ATTACHMENT', 'DOC_A_003', '农民工工资账户', 
 '{"keywords": ["农民工工资", "Migrant Worker Wage", "工资专户"], "filePattern": ".*工资.*账户.*"}', 
 'CRITICAL', 10, '工程施工合同必须提供农民工工资专户信息'),

('TYPE_A', 'ATTACHMENT', 'DOC_A_004', '履约保证金', 
 '{"keywords": ["履约保证金", "Performance Bond"], "filePattern": ".*履约.*保证.*"}', 
 'HIGH', 20, '建议包含履约保证金相关文件'),

('TYPE_A', 'LOGIC', 'LOGIC_A_001', '安全费比例检查', 
 '{"field": "safetyFeeRatio", "operator": ">=", "threshold": 0.015, "message": "安全费比例必须≥1.5%"}', 
 'CRITICAL', 10, '安全生产费用占合同总额的比例必须≥1.5%'),

('TYPE_A', 'LOGIC', 'LOGIC_A_002', '质保金比例检查', 
 '{"field": "retainageRate", "operator": "==", "threshold": 0.03, "tolerance": 0.001, "message": "质保金比例应为3%"}', 
 'HIGH', 20, '质保金比例标准为3%'),

('TYPE_A', 'RISK', 'RISK_A_001', '违法转包检测', 
 '{"keywords": ["转包", "分包给", "subcontract to"], "context": "negative", "message": "检测到可能的违法转包条款"}', 
 'CRITICAL', 10, '检测合同中是否存在违法转包条款'),

('TYPE_A', 'RISK', 'RISK_A_002', '安全责任转移检测', 
 '{"keywords": ["甲方承担连带责任", "Party A bears joint liability"], "context": "safety", "message": "检测到安全责任不当转移"}', 
 'CRITICAL', 10, '检测是否存在将安全责任不当转移给甲方的条款'),

('TYPE_A', 'RISK', 'RISK_A_007', '审计拒绝检测', 
 '{"keywords": ["拒绝审计", "refuse audit"], "context": "negative", "message": "检测到拒绝审计相关条款"}', 
 'HIGH', 20, '检测是否存在拒绝审计的条款');

-- =======================================================
-- 4. 初始化Type B规则数据（代维服务合同）
-- =======================================================
INSERT INTO `t_contract_review_rule` 
(`contract_type`, `rule_category`, `rule_code`, `rule_name`, `rule_config`, `mandate_level`, `priority`, `description`) 
VALUES 
-- 附件检查规则
('TYPE_B', 'ATTACHMENT', 'DOC_B_001', '技术规范书', 
 '{"keywords": ["技术规范书", "Technical Specification"], "filePattern": ".*技术规范.*"}', 
 'CRITICAL', 10, '代维服务合同必须包含技术规范书'),

('TYPE_B', 'ATTACHMENT', 'DOC_B_002', '考核细则', 
 '{"keywords": ["考核细则", "Appraisal Rules", "KPI"], "filePattern": ".*考核.*"}', 
 'CRITICAL', 10, '代维服务合同必须包含考核细则'),

('TYPE_B', 'ATTACHMENT', 'DOC_B_003', '报价明细', 
 '{"keywords": ["报价明细", "Quotation", "Price List", "单价表"], "filePattern": ".*报价.*|.*单价.*"}', 
 'CRITICAL', 10, '代维服务合同必须包含报价明细'),

('TYPE_B', 'ATTACHMENT', 'DOC_B_004', '安全生产责任', 
 '{"keywords": ["安全生产责任", "Safety Responsibility"], "filePattern": ".*安全.*责任.*"}', 
 'CRITICAL', 10, '代维服务合同必须包含安全生产责任书'),

('TYPE_B', 'LOGIC', 'LOGIC_B_001', 'SLA可用性目标', 
 '{"field": "slaAvailability", "operator": ">=", "threshold": 0.995, "message": "SLA可用性目标必须≥99.5%"}', 
 'CRITICAL', 10, '网络可用性SLA目标必须≥99.5%'),

('TYPE_B', 'LOGIC', 'LOGIC_B_002', '框架上限检查', 
 '{"field": "frameworkCapUsage", "operator": "<=", "threshold": 1.0, "message": "订单金额不得超过框架上限"}', 
 'CRITICAL', 10, '订单累计金额不得超过框架协议上限'),

('TYPE_B', 'RISK', 'RISK_B_001', '伪造记录风险', 
 '{"keywords": ["伪造", "falsify"], "context": "no_penalty", "message": "检测到伪造记录相关条款可能不追责"}', 
 'CRITICAL', 10, '检测是否存在对伪造记录不予处罚的条款'),

('TYPE_B', 'RISK', 'RISK_B_004', '甲供材管理风险', 
 '{"keywords": ["甲供材", "Party A Material"], "context": "liability_shift", "message": "检测到甲供材责任不当转移"}', 
 'CRITICAL', 10, '检测是否存在甲供材丢失乙方免责的条款');

-- =======================================================
-- 5. 初始化Type C规则数据（IT服务合同）
-- =======================================================
INSERT INTO `t_contract_review_rule` 
(`contract_type`, `rule_category`, `rule_code`, `rule_name`, `rule_config`, `mandate_level`, `priority`, `description`) 
VALUES 
('TYPE_C', 'ATTACHMENT', 'DOC_C_001', '数据安全承诺', 
 '{"keywords": ["数据安全", "Data Security", "数据保护"], "filePattern": ".*数据安全.*"}', 
 'CRITICAL', 10, 'IT服务合同必须包含数据安全承诺'),

('TYPE_C', 'ATTACHMENT', 'DOC_C_002', '开源承诺', 
 '{"keywords": ["开源", "Open Source", "开源声明"], "filePattern": ".*开源.*"}', 
 'CRITICAL', 10, 'IT服务合同必须包含开源软件使用承诺'),

('TYPE_C', 'ATTACHMENT', 'DOC_C_004', '报价单', 
 '{"keywords": ["报价", "Rate Card", "Price List"], "filePattern": ".*报价.*"}', 
 'CRITICAL', 10, 'IT服务合同必须包含报价单'),

('TYPE_C', 'ATTACHMENT', 'DOC_C_005', '廉洁承诺', 
 '{"keywords": ["廉洁", "Integrity Pact"], "filePattern": ".*廉洁.*"}', 
 'CRITICAL', 10, 'IT服务合同必须包含廉洁承诺'),

('TYPE_C', 'LOGIC', 'LOGIC_C_001', 'DICT背靠背付款', 
 '{"field": "dictFlag", "condition": "true", "requiredField": "paymentMode", "requiredValue": "Back2Back", "message": "DICT项目必须采用背靠背付款方式"}', 
 'CRITICAL', 10, 'DICT项目必须采用背靠背付款条款'),

('TYPE_C', 'LOGIC', 'LOGIC_C_002', 'UAT期限检查', 
 '{"field": "uatDuration", "operator": ">=", "threshold": 3, "message": "稳定运行期必须≥3个月"}', 
 'HIGH', 20, '软件稳定运行期（UAT）必须≥3个月'),

('TYPE_C', 'RISK', 'RISK_C_001', '数据出境风险', 
 '{"keywords": ["境外", "Cross-border", "Overseas", "海外"], "context": "data_transfer", "message": "检测到数据可能出境的条款"}', 
 'CRITICAL', 10, '检测是否存在数据跨境传输的条款'),

('TYPE_C', 'RISK', 'RISK_C_002', 'GPL许可证风险', 
 '{"keywords": ["GPL", "AGPL", "SSPL"], "context": "license", "message": "检测到传染性开源许可证"}', 
 'CRITICAL', 10, '检测是否使用了GPL等传染性开源许可证'),

('TYPE_C', 'RISK', 'RISK_C_003', '知识产权归属风险', 
 '{"keywords": ["乙方拥有", "Party B owns", "乙方所有"], "context": "custom_code", "message": "检测到定制代码知识产权可能归乙方"}', 
 'CRITICAL', 10, '检测定制代码知识产权是否正确归属甲方');

