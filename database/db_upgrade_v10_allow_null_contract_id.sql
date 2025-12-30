-- =======================================================
-- 数据库升级脚本 v10
-- 功能：允许合同编辑历史表的contract_id为NULL
-- 说明：支持新建合同（未保存）的撤销功能
-- 创建时间：2024
-- =======================================================

-- 修改 t_contract_edit_history 表，允许 contract_id 为 NULL
-- 这样新建合同（contractId为null）的编辑历史也能保存到数据库，支持数据持久化

ALTER TABLE `t_contract_edit_history` 
MODIFY COLUMN `contract_id` bigint DEFAULT NULL COMMENT '合同ID（新建合同时为NULL）';

-- 注意：
-- 1. 此修改允许contract_id为NULL，支持新建合同的编辑历史持久化
-- 2. 撤销功能需要判断contract_id是否为NULL：
--    - 如果为NULL，只返回内容，不更新合同表
--    - 如果不为NULL，正常更新合同表
-- 3. 建议定期清理过期的NULL记录（可以通过定时任务清理超过24小时的记录）


