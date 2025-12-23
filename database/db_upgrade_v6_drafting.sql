/*
 Description: 合同起草功能数据库升级脚本
 Features: 
   - 合同模板系统
   - AI会话管理
   - 合同编辑历史（用于撤销）
*/

USE contract_system;

-- =======================================================
-- 1. 合同模板表
-- =======================================================
DROP TABLE IF EXISTS `t_contract_template`;
CREATE TABLE `t_contract_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(20) NOT NULL COMMENT '主类型: TYPE_A, TYPE_B, TYPE_C',
  `sub_type_code` varchar(10) NOT NULL COMMENT '子类型代码: A1, A2, A3, B1-B4, C1-C3',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `description` varchar(500) DEFAULT NULL COMMENT '模板说明',
  `content` longtext NOT NULL COMMENT '模板内容（HTML/Markdown格式）',
  `fields_schema` json DEFAULT NULL COMMENT '可编辑字段定义（JSON Schema）',
  `locked_sections` json DEFAULT NULL COMMENT '锁定区域标记（不可修改的条款）',
  `is_default` tinyint DEFAULT 1 COMMENT '是否默认模板',
  `is_active` tinyint DEFAULT 1 COMMENT '是否启用',
  `version` varchar(20) DEFAULT 'v1.0' COMMENT '模板版本',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subtype_default` (`sub_type_code`, `is_default`),
  KEY `idx_type` (`type`),
  KEY `idx_sub_type_code` (`sub_type_code`)
) COMMENT='合同模板表';

-- =======================================================
-- 2. AI会话表（上下文管理）
-- =======================================================
DROP TABLE IF EXISTS `t_ai_session`;
CREATE TABLE `t_ai_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) NOT NULL COMMENT '会话唯一标识',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `contract_id` bigint DEFAULT NULL COMMENT '关联的合同ID（起草中可能还没保存）',
  `mode` varchar(20) DEFAULT 'ASK' COMMENT '当前模式: ASK, AGENT',
  `context_data` json DEFAULT NULL COMMENT '会话上下文（历史消息摘要）',
  `contract_snapshot` longtext DEFAULT NULL COMMENT '合同内容快照（用于上下文）',
  `sub_type_code` varchar(10) DEFAULT NULL COMMENT '合同子类型（用于上下文）',
  `message_count` int DEFAULT 0 COMMENT '消息数量',
  `last_active_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  `expired_at` datetime DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_last_active` (`last_active_at`)
) COMMENT='AI会话表';

-- =======================================================
-- 3. AI会话消息表（对话历史）
-- =======================================================
DROP TABLE IF EXISTS `t_ai_message`;
CREATE TABLE `t_ai_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `role` varchar(20) NOT NULL COMMENT '角色: USER, ASSISTANT, SYSTEM',
  `content` text NOT NULL COMMENT '消息内容',
  `mode` varchar(20) DEFAULT 'ASK' COMMENT '模式: ASK, AGENT',
  `agent_action` json DEFAULT NULL COMMENT 'Agent操作详情（修改了什么）',
  `token_count` int DEFAULT 0 COMMENT 'Token消耗',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_created_at` (`created_at`)
) COMMENT='AI会话消息表';

-- =======================================================
-- 4. 合同编辑历史表（用于Agent撤销）
-- =======================================================
DROP TABLE IF EXISTS `t_contract_edit_history`;
CREATE TABLE `t_contract_edit_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `session_id` varchar(64) DEFAULT NULL COMMENT '关联的AI会话',
  `edit_type` varchar(20) NOT NULL COMMENT '编辑类型: MANUAL, AI_AGENT',
  `action` varchar(50) NOT NULL COMMENT '操作: MODIFY, INSERT, DELETE, REPLACE',
  `field_path` varchar(200) DEFAULT NULL COMMENT '修改的字段路径',
  `location_desc` varchar(200) DEFAULT NULL COMMENT '位置描述（第几条第几款）',
  `old_value` longtext DEFAULT NULL COMMENT '修改前的值',
  `new_value` longtext DEFAULT NULL COMMENT '修改后的值',
  `full_content_before` longtext DEFAULT NULL COMMENT '修改前的完整内容（用于撤销）',
  `is_undone` tinyint DEFAULT 0 COMMENT '是否已撤销',
  `undo_token` varchar(64) DEFAULT NULL COMMENT '撤销令牌',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_undo_token` (`undo_token`),
  KEY `idx_created_at` (`created_at`)
) COMMENT='合同编辑历史表';

-- =======================================================
-- 5. 合同类型枚举表（方便前端获取）
-- =======================================================
DROP TABLE IF EXISTS `t_contract_type`;
CREATE TABLE `t_contract_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_code` varchar(20) NOT NULL COMMENT '主类型代码: TYPE_A, TYPE_B, TYPE_C',
  `type_name` varchar(50) NOT NULL COMMENT '主类型名称',
  `sub_type_code` varchar(10) NOT NULL COMMENT '子类型代码: A1, A2...',
  `sub_type_name` varchar(100) NOT NULL COMMENT '子类型名称',
  `description` varchar(500) DEFAULT NULL COMMENT '说明',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `is_active` tinyint DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_type_code` (`sub_type_code`),
  KEY `idx_type_code` (`type_code`)
) COMMENT='合同类型定义表';

-- =======================================================
-- 6. 初始化合同类型数据
-- =======================================================
INSERT INTO `t_contract_type` (`type_code`, `type_name`, `sub_type_code`, `sub_type_name`, `description`, `sort_order`) VALUES
-- TYPE_A 采购类
('TYPE_A', '采购类', 'A1', '土建工程', '基站建设、传输管线、机房土建等工程施工合同', 1),
('TYPE_A', '采购类', 'A2', '装修工程', '营业厅装修、办公场所装修等合同', 2),
('TYPE_A', '采购类', 'A3', '零星维修', '小额维修、日常维护等合同', 3),

-- TYPE_B 工程类
('TYPE_B', '工程类', 'B1', '光缆代维', '光缆线路日常维护、故障抢修等服务合同', 4),
('TYPE_B', '工程类', 'B2', '基站代维', '基站设备日常维护、巡检等服务合同', 5),
('TYPE_B', '工程类', 'B3', '家宽代维', '家庭宽带安装、维护等服务合同', 6),
('TYPE_B', '工程类', 'B4', '应急保障', '重大活动通信保障、应急抢修等服务合同', 7),

-- TYPE_C 服务类
('TYPE_C', '服务类', 'C1', '定制开发', '软件系统定制开发、二次开发等合同', 8),
('TYPE_C', '服务类', 'C2', '商用软件采购', '商用软件许可、SaaS服务等采购合同', 9),
('TYPE_C', '服务类', 'C3', 'DICT集成', '数字化转型、ICT集成项目合同', 10);
