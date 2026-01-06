-- 检查用户 role 与部门级别是否匹配
USE contract_system;

SELECT 
    su.id,
    su.username,
    su.real_name,
    su.role as user_role,
    sd.name as dept_name,
    sd.level as dept_level,
    sd.type as dept_type,
    CASE 
        WHEN sd.level = 4 THEN 'COUNTY'
        WHEN sd.level = 3 AND sd.type = 'COUNTY' THEN 'COUNTY'
        WHEN sd.level = 3 AND sd.type = 'DEPT' THEN 'CITY'
        WHEN sd.level = 2 AND sd.type = 'CITY' THEN 'CITY'
        WHEN sd.level = 2 THEN 'PROVINCE'
        ELSE 'CITY'
    END as expected_role,
    CASE 
        WHEN su.role = (
            CASE 
                WHEN sd.level = 4 THEN 'COUNTY'
                WHEN sd.level = 3 AND sd.type = 'COUNTY' THEN 'COUNTY'
                WHEN sd.level = 3 AND sd.type = 'DEPT' THEN 'CITY'
                WHEN sd.level = 2 AND sd.type = 'CITY' THEN 'CITY'
                WHEN sd.level = 2 THEN 'PROVINCE'
                ELSE 'CITY'
            END
        ) THEN '✓ 匹配'
        ELSE '✗ 不匹配'
    END as match_status
FROM sys_user su
JOIN sys_dept sd ON su.dept_id = sd.id
WHERE su.id > 1 AND su.role NOT IN ('ADMIN', 'BOSS')
ORDER BY sd.level, sd.id;

-- 统计不匹配的数量
SELECT 
    COUNT(*) as mismatch_count
FROM sys_user su
JOIN sys_dept sd ON su.dept_id = sd.id
WHERE su.id > 1 
    AND su.role NOT IN ('ADMIN', 'BOSS')
    AND su.role != (
        CASE 
            WHEN sd.level = 4 THEN 'COUNTY'
            WHEN sd.level = 3 AND sd.type = 'COUNTY' THEN 'COUNTY'
            WHEN sd.level = 3 AND sd.type = 'DEPT' THEN 'CITY'
            WHEN sd.level = 2 AND sd.type = 'CITY' THEN 'CITY'
            WHEN sd.level = 2 THEN 'PROVINCE'
            ELSE 'CITY'
        END
    );

