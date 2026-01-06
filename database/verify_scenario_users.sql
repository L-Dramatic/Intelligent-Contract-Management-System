-- 验证场景配置是否都有对应的用户
USE contract_system;

-- 1. 列出所有场景节点使用的角色（除了INITIATOR）
SELECT DISTINCT 
    n.role_code,
    r.role_name,
    r.dept_type_required AS required_dept_type
FROM wf_scenario_node n
JOIN sys_role r ON n.role_code = r.role_code
WHERE n.role_code != 'INITIATOR'
ORDER BY n.role_code;

-- 2. 检查每个角色是否有A市的用户
SELECT 
    n.role_code,
    r.role_name,
    COUNT(DISTINCT u.id) AS user_count_in_A,
    CASE 
        WHEN COUNT(DISTINCT u.id) > 0 THEN '✓ 有用户'
        ELSE '❌ 无用户'
    END AS status,
    GROUP_CONCAT(DISTINCT u.real_name SEPARATOR ', ') AS users
FROM wf_scenario_node n
JOIN sys_role r ON n.role_code = r.role_code
LEFT JOIN sys_user u ON n.role_code = u.primary_role 
    AND u.is_active = 1
    AND u.dept_id IN (
        SELECT id FROM sys_dept 
        WHERE code LIKE 'CITY-A%' 
        OR (code = 'CITY-A' AND type = 'CITY')
    )
WHERE n.role_code != 'INITIATOR'
GROUP BY n.role_code, r.role_name
ORDER BY user_count_in_A, n.role_code;

-- 3. 列出缺失用户的角色
SELECT 
    n.role_code,
    r.role_name,
    r.dept_type_required,
    COUNT(DISTINCT n.scenario_id) AS used_in_scenarios,
    GROUP_CONCAT(DISTINCT n.scenario_id ORDER BY n.scenario_id SEPARATOR ', ') AS scenarios
FROM wf_scenario_node n
JOIN sys_role r ON n.role_code = r.role_code
LEFT JOIN sys_user u ON n.role_code = u.primary_role 
    AND u.is_active = 1
    AND u.dept_id IN (
        SELECT id FROM sys_dept 
        WHERE code LIKE 'CITY-A%' 
        OR (code = 'CITY-A' AND type = 'CITY')
    )
WHERE n.role_code != 'INITIATOR'
  AND u.id IS NULL
GROUP BY n.role_code, r.role_name, r.dept_type_required
ORDER BY used_in_scenarios DESC;

