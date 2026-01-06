-- 全面检查所有审批场景的角色和人员完整性
USE contract_system;

-- 1. 查看所有场景配置（共30+个场景）
SELECT 
    scenario_id,
    sub_type_code,
    sub_type_name,
    CONCAT(amount_min, '-', IFNULL(amount_max, '∞')) AS amount_range,
    CASE WHEN is_fast_track = 1 THEN '快速通道' ELSE '标准' END AS track_type,
    description
FROM wf_scenario_config
ORDER BY sub_type_code, amount_min;

-- 2. 统计每个场景的节点数量
SELECT 
    scenario_id,
    COUNT(*) AS node_count,
    GROUP_CONCAT(
        CONCAT(node_order, ':', role_code, '(', node_level, ')') 
        ORDER BY node_order 
        SEPARATOR ', '
    ) AS nodes
FROM wf_scenario_node
GROUP BY scenario_id
ORDER BY scenario_id;

-- 3. 检查所有场景节点使用的角色列表（去重）
SELECT DISTINCT
    n.role_code,
    r.role_name,
    r.role_category,
    r.dept_type_required,
    COUNT(DISTINCT n.scenario_id) AS used_in_scenarios
FROM wf_scenario_node n
LEFT JOIN sys_role r ON n.role_code = r.role_code
WHERE n.role_code != 'INITIATOR'  -- INITIATOR不需要分配用户
GROUP BY n.role_code, r.role_name, r.role_category, r.dept_type_required
ORDER BY r.role_category, n.role_code;

-- 4. 检查每个角色是否有对应的用户（按城市分组）
SELECT 
    n.role_code,
    r.role_name,
    CASE 
        WHEN COUNT(DISTINCT u.id) = 0 THEN '❌ 无用户'
        ELSE CONCAT('✓ ', COUNT(DISTINCT u.id), '个用户')
    END AS user_status,
    GROUP_CONCAT(
        DISTINCT CONCAT(
            u.real_name, 
            '(',
            (SELECT name FROM sys_dept WHERE id = u.dept_id),
            ')'
        ) 
        SEPARATOR ', '
    ) AS users,
    GROUP_CONCAT(DISTINCT (SELECT code FROM sys_dept WHERE id = u.dept_id)) AS dept_codes
FROM wf_scenario_node n
LEFT JOIN sys_role r ON n.role_code = r.role_code
LEFT JOIN sys_user u ON n.role_code = u.primary_role AND u.is_active = 1
WHERE n.role_code != 'INITIATOR'
GROUP BY n.role_code, r.role_name
ORDER BY user_status, n.role_code;

-- 5. 按城市检查关键角色的人员情况
SELECT 
    CASE 
        WHEN d.code LIKE 'CITY-A%' THEN 'A市'
        WHEN d.code LIKE 'CITY-B%' THEN 'B市'
        WHEN d.code LIKE 'PROV%' THEN '省级'
        ELSE '其他'
    END AS city_level,
    u.primary_role,
    r.role_name,
    COUNT(*) AS user_count,
    GROUP_CONCAT(u.real_name SEPARATOR ', ') AS users,
    GROUP_CONCAT(d.name SEPARATOR ', ') AS departments
FROM sys_user u
JOIN sys_dept d ON u.dept_id = d.id
LEFT JOIN sys_role r ON u.primary_role = r.role_code
WHERE u.is_active = 1
  AND u.primary_role IN (
    SELECT DISTINCT role_code 
    FROM wf_scenario_node 
    WHERE role_code != 'INITIATOR'
  )
GROUP BY city_level, u.primary_role, r.role_name
ORDER BY city_level, u.primary_role;

-- 6. 找出缺失的角色（在节点中使用但没有用户）
SELECT 
    n.role_code,
    r.role_name,
    n.node_level,
    COUNT(DISTINCT n.scenario_id) AS missing_in_scenarios,
    GROUP_CONCAT(DISTINCT n.scenario_id SEPARATOR ', ') AS scenarios
FROM wf_scenario_node n
LEFT JOIN sys_role r ON n.role_code = r.role_code
LEFT JOIN sys_user u ON n.role_code = u.primary_role AND u.is_active = 1
WHERE n.role_code != 'INITIATOR'
  AND u.id IS NULL
GROUP BY n.role_code, r.role_name, n.node_level
ORDER BY missing_in_scenarios DESC, n.role_code;

-- 7. 按场景检查完整性（哪些场景可以完整执行）
SELECT 
    sc.scenario_id,
    sc.sub_type_name,
    COUNT(DISTINCT n.role_code) AS required_roles,
    COUNT(DISTINCT CASE 
        WHEN u.id IS NOT NULL THEN n.role_code 
    END) AS available_roles,
    CASE 
        WHEN COUNT(DISTINCT n.role_code) = COUNT(DISTINCT CASE WHEN u.id IS NOT NULL THEN n.role_code END)
        THEN '✓ 完整'
        ELSE CONCAT('❌ 缺失', 
            COUNT(DISTINCT n.role_code) - COUNT(DISTINCT CASE WHEN u.id IS NOT NULL THEN n.role_code END),
            '个角色')
    END AS status,
    GROUP_CONCAT(
        DISTINCT CASE 
            WHEN u.id IS NULL THEN n.role_code 
        END
        SEPARATOR ', '
    ) AS missing_roles
FROM wf_scenario_config sc
LEFT JOIN wf_scenario_node n ON sc.scenario_id = n.scenario_id
LEFT JOIN sys_user u ON n.role_code = u.primary_role 
    AND u.is_active = 1
    AND (
        (n.node_level = 'CITY' AND u.dept_id IN (SELECT id FROM sys_dept WHERE code LIKE 'CITY-%' AND type = 'DEPT'))
        OR (n.node_level = 'PROVINCE' AND u.dept_id IN (SELECT id FROM sys_dept WHERE code LIKE 'PROV-%' AND type = 'DEPT'))
    )
WHERE n.role_code != 'INITIATOR'
GROUP BY sc.scenario_id, sc.sub_type_name
ORDER BY status, sc.scenario_id;

-- 8. 详细列出每个场景缺失的角色和用户
SELECT 
    sc.scenario_id,
    sc.sub_type_name,
    n.node_order,
    n.role_code,
    r.role_name,
    n.node_level,
    CASE 
        WHEN u.id IS NULL THEN '❌ 缺失'
        ELSE CONCAT('✓ ', u.real_name, '(', d.name, ')')
    END AS user_status
FROM wf_scenario_config sc
JOIN wf_scenario_node n ON sc.scenario_id = n.scenario_id
LEFT JOIN sys_role r ON n.role_code = r.role_code
LEFT JOIN sys_user u ON n.role_code = u.primary_role 
    AND u.is_active = 1
    AND (
        (n.node_level = 'CITY' AND u.dept_id IN (
            SELECT id FROM sys_dept 
            WHERE (code LIKE 'CITY-A%' OR code LIKE 'CITY-B%') 
            AND type = 'DEPT'
        ))
        OR (n.node_level = 'PROVINCE' AND u.dept_id IN (
            SELECT id FROM sys_dept 
            WHERE code LIKE 'PROV-%' 
            AND type = 'DEPT'
        ))
    )
LEFT JOIN sys_dept d ON u.dept_id = d.id
WHERE n.role_code != 'INITIATOR'
ORDER BY sc.scenario_id, n.node_order;

