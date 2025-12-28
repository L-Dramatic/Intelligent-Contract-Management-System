-- ============================================================
-- 快速修复脚本: 为 t_contract 表添加 updated_at 字段
-- 使用方法: 在 MySQL 客户端执行此脚本
-- ============================================================

USE contract_system;

-- 添加 updated_at 字段（如果字段已存在会报错，忽略即可）
ALTER TABLE `t_contract` 
ADD COLUMN `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' 
AFTER `created_at`;

-- 验证字段是否添加成功
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'contract_system' 
  AND TABLE_NAME = 't_contract' 
  AND COLUMN_NAME = 'updated_at';

