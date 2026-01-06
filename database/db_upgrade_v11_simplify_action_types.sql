/*
 Description: 11_简化动作类型（统一为INITIATE和APPROVE）
 Purpose: 将所有审批动作类型统一为APPROVE，简化系统逻辑
 Version: v11
 Date: 2025-01-06
 
 项目定位调整：
 - 固定起点：所有场景以"县级发起人"为起点
 - 动作类型：只有"发起"(INITIATE)和"审批"(APPROVE)两种
 - 审批流程：县级发起 → 市级审批 → [省级审批]
*/

USE contract_system;

-- =======================================================
-- 1. 统一动作类型
-- =======================================================

-- 将所有非INITIATE的动作类型统一改为APPROVE
UPDATE wf_scenario_node 
SET action_type = 'APPROVE' 
WHERE action_type IN ('REVIEW', 'VERIFY', 'FINAL_APPROVE');

-- =======================================================
-- 2. 验证更新结果
-- =======================================================

-- 查看动作类型分布
SELECT 
    action_type,
    COUNT(*) AS node_count
FROM wf_scenario_node 
GROUP BY action_type
ORDER BY action_type;

-- 验证只剩下INITIATE和APPROVE两种类型
SELECT 
    CASE 
        WHEN COUNT(DISTINCT action_type) = 2 
             AND SUM(CASE WHEN action_type = 'INITIATE' THEN 1 ELSE 0 END) > 0
             AND SUM(CASE WHEN action_type = 'APPROVE' THEN 1 ELSE 0 END) > 0
        THEN '✓ 动作类型简化成功：只有INITIATE和APPROVE'
        ELSE '✗ 警告：动作类型异常，请检查'
    END AS verification_result
FROM wf_scenario_node;

-- =======================================================
-- 完成提示
-- =======================================================
SELECT 'db_upgrade_v11_simplify_action_types 执行完成！动作类型已统一为INITIATE和APPROVE' AS message;

