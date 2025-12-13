USE contract_system;

-- ==========================================
-- 1. AI 与 知识库模块 (对应 SRS 4.3.1)
-- ==========================================

-- 知识库表 (用于存储 RAG 的原始文本，如法规、条款、模板)
CREATE TABLE IF NOT EXISTS `t_knowledge_base` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` longtext NOT NULL COMMENT '内容文本',
  `type` varchar(50) NOT NULL COMMENT '类型: TERM(术语), TEMPLATE(模板), REGULATION(法规)',
  `vector_id` varchar(100) DEFAULT NULL COMMENT '向量数据库中的ID (关联 Chroma)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='电信领域知识库表';

-- AI 生成日志表 (对应 SRS 4.1.2，用于审计 Token 消耗和生成质量)
CREATE TABLE IF NOT EXISTS `t_ai_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contract_id` bigint DEFAULT NULL COMMENT '关联的合同ID',
  `user_id` bigint NOT NULL COMMENT '调用人',
  `function_type` varchar(50) COMMENT '功能: GENERATE(生成), REVIEW(审查)',
  `prompt_summary` text COMMENT '提示词摘要',
  `result_summary` text COMMENT 'AI返回结果摘要',
  `token_usage` int DEFAULT 0 COMMENT 'Token消耗量',
  `status` tinyint DEFAULT 1 COMMENT '1:成功, 0:失败',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='AI服务调用日志表';

-- ==========================================
-- 2. 审批日志模块 (对应 SRS 6.5 审计需求)
-- ==========================================

-- 审批历史记录表 (wf_task 是待办，这个表是已办历史)
CREATE TABLE IF NOT EXISTS `wf_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `instance_id` bigint NOT NULL COMMENT '流程实例ID',
  `node_name` varchar(50) NOT NULL COMMENT '节点名称',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(50) COMMENT '审批人姓名(冗余字段，防用户被删)',
  `action` varchar(20) NOT NULL COMMENT '动作: PASS(通过), REJECT(驳回), TRANSFER(转交)',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `operate_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='审批操作日志表';

-- ==========================================
-- 3. 附件管理 (合同文件、扫描件)
-- ==========================================
CREATE TABLE IF NOT EXISTS `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_name` varchar(200) NOT NULL,
  `file_url` varchar(500) NOT NULL,
  `file_size` bigint DEFAULT 0,
  `uploader_id` bigint NOT NULL,
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='文件存储表';