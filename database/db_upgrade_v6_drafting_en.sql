/*
 Description: Contract drafting feature database upgrade
 Features: Template system, AI session management, Edit history
*/

USE contract_system;

-- 1. Contract Template Table
DROP TABLE IF EXISTS `t_contract_template`;
CREATE TABLE `t_contract_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(20) NOT NULL COMMENT 'Main type: TYPE_A, TYPE_B, TYPE_C',
  `sub_type_code` varchar(10) NOT NULL COMMENT 'Sub type code: A1, A2, A3, B1-B4, C1-C3',
  `name` varchar(100) NOT NULL COMMENT 'Template name',
  `description` varchar(500) DEFAULT NULL COMMENT 'Template description',
  `content` longtext NOT NULL COMMENT 'Template content (HTML/Markdown)',
  `fields_schema` json DEFAULT NULL COMMENT 'Editable fields definition',
  `locked_sections` json DEFAULT NULL COMMENT 'Locked sections marker',
  `is_default` tinyint DEFAULT 1 COMMENT 'Is default template',
  `is_active` tinyint DEFAULT 1 COMMENT 'Is active',
  `version` varchar(20) DEFAULT 'v1.0' COMMENT 'Template version',
  `created_by` bigint DEFAULT NULL COMMENT 'Creator ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_sub_type_code` (`sub_type_code`)
) COMMENT='Contract Template Table';

-- 2. AI Session Table
DROP TABLE IF EXISTS `t_ai_session`;
CREATE TABLE `t_ai_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) NOT NULL COMMENT 'Session unique ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `contract_id` bigint DEFAULT NULL COMMENT 'Related contract ID',
  `mode` varchar(20) DEFAULT 'ASK' COMMENT 'Current mode: ASK, AGENT',
  `context_data` json DEFAULT NULL COMMENT 'Session context',
  `contract_snapshot` longtext DEFAULT NULL COMMENT 'Contract content snapshot',
  `sub_type_code` varchar(10) DEFAULT NULL COMMENT 'Contract sub type',
  `message_count` int DEFAULT 0 COMMENT 'Message count',
  `last_active_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Last active time',
  `expired_at` datetime DEFAULT NULL COMMENT 'Expiration time',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_last_active` (`last_active_at`)
) COMMENT='AI Session Table';

-- 3. AI Message Table
DROP TABLE IF EXISTS `t_ai_message`;
CREATE TABLE `t_ai_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) NOT NULL COMMENT 'Session ID',
  `role` varchar(20) NOT NULL COMMENT 'Role: USER, ASSISTANT, SYSTEM',
  `content` text NOT NULL COMMENT 'Message content',
  `mode` varchar(20) DEFAULT 'ASK' COMMENT 'Mode: ASK, AGENT',
  `agent_action` json DEFAULT NULL COMMENT 'Agent action details',
  `token_count` int DEFAULT 0 COMMENT 'Token usage',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_created_at` (`created_at`)
) COMMENT='AI Message Table';

-- 4. Contract Edit History Table
DROP TABLE IF EXISTS `t_contract_edit_history`;
CREATE TABLE `t_contract_edit_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contract_id` bigint NOT NULL COMMENT 'Contract ID',
  `session_id` varchar(64) DEFAULT NULL COMMENT 'Related AI session',
  `edit_type` varchar(20) NOT NULL COMMENT 'Edit type: MANUAL, AI_AGENT',
  `action` varchar(50) NOT NULL COMMENT 'Action: MODIFY, INSERT, DELETE, REPLACE',
  `field_path` varchar(200) DEFAULT NULL COMMENT 'Modified field path',
  `location_desc` varchar(200) DEFAULT NULL COMMENT 'Location description',
  `old_value` longtext DEFAULT NULL COMMENT 'Old value',
  `new_value` longtext DEFAULT NULL COMMENT 'New value',
  `full_content_before` longtext DEFAULT NULL COMMENT 'Full content before edit',
  `is_undone` tinyint DEFAULT 0 COMMENT 'Is undone',
  `undo_token` varchar(64) DEFAULT NULL COMMENT 'Undo token',
  `operator_id` bigint NOT NULL COMMENT 'Operator ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_undo_token` (`undo_token`),
  KEY `idx_created_at` (`created_at`)
) COMMENT='Contract Edit History Table';

-- 5. Contract Type Table
DROP TABLE IF EXISTS `t_contract_type`;
CREATE TABLE `t_contract_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_code` varchar(20) NOT NULL COMMENT 'Main type code',
  `type_name` varchar(50) NOT NULL COMMENT 'Main type name',
  `sub_type_code` varchar(10) NOT NULL COMMENT 'Sub type code',
  `sub_type_name` varchar(100) NOT NULL COMMENT 'Sub type name',
  `description` varchar(500) DEFAULT NULL COMMENT 'Description',
  `sort_order` int DEFAULT 0 COMMENT 'Sort order',
  `is_active` tinyint DEFAULT 1 COMMENT 'Is active',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_type_code` (`sub_type_code`),
  KEY `idx_type_code` (`type_code`)
) COMMENT='Contract Type Definition Table';

-- 6. Initialize Contract Type Data
INSERT INTO `t_contract_type` (`type_code`, `type_name`, `sub_type_code`, `sub_type_name`, `description`, `sort_order`) VALUES
('TYPE_A', 'Procurement', 'A1', 'Civil Engineering', 'Base station construction, transmission pipeline, equipment room construction', 1),
('TYPE_A', 'Procurement', 'A2', 'Decoration', 'Business hall decoration, office decoration', 2),
('TYPE_A', 'Procurement', 'A3', 'Minor Repairs', 'Small repairs and daily maintenance', 3),
('TYPE_B', 'Engineering', 'B1', 'Fiber Optic Maintenance', 'Optical cable maintenance and emergency repair', 4),
('TYPE_B', 'Engineering', 'B2', 'Base Station Maintenance', 'Base station equipment maintenance', 5),
('TYPE_B', 'Engineering', 'B3', 'Home Broadband Maintenance', 'Home broadband installation and maintenance', 6),
('TYPE_B', 'Engineering', 'B4', 'Emergency Support', 'Major event communication support', 7),
('TYPE_C', 'Services', 'C1', 'Custom Development', 'Software system custom development', 8),
('TYPE_C', 'Services', 'C2', 'Commercial Software', 'Commercial software licensing and SaaS', 9),
('TYPE_C', 'Services', 'C3', 'DICT Integration', 'Digital transformation and ICT integration', 10);

-- 7. Initialize Default Templates
INSERT INTO `t_contract_template` (`type`, `sub_type_code`, `name`, `description`, `content`, `is_default`, `is_active`, `version`) VALUES
('TYPE_A', 'A1', 'Civil Engineering Contract', 'For base station and infrastructure construction', '# Civil Engineering Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Project Overview\n\n1.1 **Project Name**: {{projectName}}\n1.2 **Location**: {{projectLocation}}\n1.3 **Scope**: {{projectScope}}\n\n## Article 2: Contract Price\n\n2.1 **Total Price**: RMB {{amount}} Yuan\n2.2 **Safety Fee Rate**: {{safetyFeeRate}}%\n\n## Article 3: Duration\n\n3.1 **Start Date**: {{startDate}}\n3.2 **End Date**: {{endDate}}\n\n## Article 4: Quality Standards\n\nEngineering quality shall comply with national standards.\n\n## Article 5: Payment Terms\n\n5.1 **Advance**: {{prepaymentRate}}%\n5.2 **Progress**: {{progressPaymentRate}}%\n5.3 **Settlement**: {{settlementRate}}%\n\n## Article 6: Breach of Contract\n\nStandard terms apply.\n\n**Party A (Seal)**:                    **Party B (Seal)**:\n\nDate:                              Date:', 1, 1, 'v1.0'),

('TYPE_A', 'A2', 'Decoration Contract', 'For decoration projects', '# Decoration Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Project Overview\n\n1.1 **Project Name**: {{projectName}}\n1.2 **Location**: {{projectLocation}}\n1.3 **Area**: {{area}} sqm\n\n## Article 2: Contract Price\n\n2.1 **Total Price**: RMB {{amount}} Yuan\n\n## Article 3: Duration\n\n3.1 **Start Date**: {{startDate}}\n3.2 **End Date**: {{endDate}}\n\n## Article 4: Quality Standards\n\nDecoration shall comply with GB 50210.\n\n**Party A (Seal)**:                    **Party B (Seal)**:', 1, 1, 'v1.0'),

('TYPE_A', 'A3', 'Minor Repair Contract', 'For small repairs', '# Minor Repair Service Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Service Scope\n\n{{serviceScope}}\n\n## Article 2: Contract Price\n\nRMB {{amount}} Yuan\n\n## Article 3: Service Period\n\n{{startDate}} to {{endDate}}\n\n**Party A (Seal)**:                    **Party B (Seal)**:', 1, 1, 'v1.0'),

('TYPE_B', 'B1', 'Fiber Optic Maintenance Contract', 'For optical cable maintenance', '# Fiber Optic Maintenance Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Maintenance Scope\n\n1.1 **Area**: {{maintenanceArea}}\n1.2 **Cable Length**: {{cableLength}} km\n\n## Article 2: Service Content\n\n- Daily inspection\n- Fault handling\n- Emergency repair\n\n## Article 3: Contract Price\n\nRMB {{amount}} Yuan per year\n\n## Article 4: SLA Targets\n\n- Fault Rate: max {{faultRate}} per 100km/year\n- Repair Rate: min {{repairRate}}%\n\n**Party A (Seal)**:                    **Party B (Seal)**:', 1, 1, 'v1.0'),

('TYPE_B', 'B2', 'Base Station Maintenance Contract', 'For base station maintenance', '# Base Station Maintenance Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Maintenance Scope\n\n1.1 **Area**: {{maintenanceArea}}\n1.2 **Station Count**: {{stationCount}}\n\n## Article 2: Contract Price\n\nRMB {{amount}} Yuan per year\n\n## Article 3: SLA Targets\n\n- Availability: min {{availabilityRate}}%\n- Repair Time: max {{repairTime}} hours\n\n**Party A (Seal)**:                    **Party B (Seal)**:', 1, 1, 'v1.0'),

('TYPE_B', 'B3', 'Home Broadband Maintenance Contract', 'For home broadband services', '# Home Broadband Maintenance Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Service Scope\n\n1.1 **Area**: {{serviceArea}}\n1.2 **User Count**: {{userCount}}\n\n## Article 2: Contract Price\n\nRMB {{amount}} Yuan\n\n## Article 3: Service Requirements\n\n- Install Time: {{installTime}} hours\n- Repair Time: {{repairTime}} hours\n\n**Party A (Seal)**:                    **Party B (Seal)**:', 1, 1, 'v1.0'),

('TYPE_B', 'B4', 'Emergency Support Contract', 'For emergency communication support', '# Emergency Communication Support Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Support Task\n\n1.1 **Event**: {{eventName}}\n1.2 **Location**: {{eventLocation}}\n1.3 **Period**: {{startDate}} to {{endDate}}\n\n## Article 2: Contract Price\n\nRMB {{amount}} Yuan\n\n## Article 3: Requirements\n\n- Response Time: {{responseTime}} minutes\n\n**Party A (Seal)**:                    **Party B (Seal)**:', 1, 1, 'v1.0'),

('TYPE_C', 'C1', 'Custom Development Contract', 'For software development', '# Software Development Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Project Overview\n\n1.1 **Project Name**: {{projectName}}\n1.2 **Goal**: {{projectGoal}}\n\n## Article 2: Development Content\n\n{{functionalRequirements}}\n\n## Article 3: Contract Price\n\nRMB {{amount}} Yuan\n\n## Article 4: Schedule\n\n- Requirements: {{requirementDays}} days\n- Development: {{developmentDays}} days\n- Testing: {{testingDays}} days\n\n## Article 5: IP Rights\n\nAll IP belongs to Party A.\n\n## Article 6: Warranty\n\nFree maintenance for {{maintenancePeriod}} months.\n\n**Party A (Seal)**:                    **Party B (Seal)**:', 1, 1, 'v1.0'),

('TYPE_C', 'C2', 'Commercial Software Contract', 'For software procurement', '# Commercial Software Procurement Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Software Details\n\n1.1 **Software Name**: {{softwareName}}\n1.2 **Version**: {{version}}\n1.3 **License Type**: {{licenseType}}\n1.4 **License Count**: {{licenseCount}}\n\n## Article 2: Contract Price\n\nRMB {{amount}} Yuan\n\n## Article 3: Delivery\n\nWithin {{deliveryDays}} days after signing.\n\n## Article 4: Support\n\nTechnical support for {{servicePeriod}}.\n\n**Party A (Seal)**:                    **Party B (Seal)**:', 1, 1, 'v1.0'),

('TYPE_C', 'C3', 'DICT Integration Contract', 'For ICT integration projects', '# DICT Integration Contract\n\n**Contract No**: {{contractNo}}\n\n## Parties\n\n**Party A**: {{partyA}}\n**Party B**: {{partyB}}\n\n## Article 1: Project Overview\n\n1.1 **Project Name**: {{projectName}}\n1.2 **End Customer**: {{endCustomer}}\n\n## Article 2: Solution\n\n{{solutionDescription}}\n\n## Article 3: Contract Price\n\nRMB {{amount}} Yuan\n\n## Article 4: Schedule\n\n- Start: {{startDate}}\n- Go-live: {{goliveDate}}\n- Acceptance: {{acceptanceDate}}\n\n## Article 5: Acceptance Criteria\n\n- Availability: {{availabilityTarget}}%\n- Test Pass Rate: {{testPassRate}}%\n\n**Party A (Seal)**:                    **Party B (Seal)**:', 1, 1, 'v1.0');
