-- 检查县级部门和市级部门的对应关系
USE contract_system;

-- 1. 查看所有县级部门
SELECT 
    '县级部门' AS dept_level,
    id,
    name,
    code,
    parent_id,
    (SELECT name FROM sys_dept WHERE id = d.parent_id) AS parent_name
FROM sys_dept d
WHERE d.type = 'DEPT' 
AND d.level = 4
ORDER BY parent_id, id;

-- 2. 查看所有市级部门
SELECT 
    '市级部门' AS dept_level,
    id,
    name,
    code,
    parent_id,
    (SELECT name FROM sys_dept WHERE id = d.parent_id) AS parent_name
FROM sys_dept d
WHERE d.type = 'DEPT' 
AND d.level = 3
AND (SELECT type FROM sys_dept WHERE id = d.parent_id) = 'CITY'
ORDER BY parent_id, id;

-- 3. 检查县级部门是否有对应的市级部门（通过关键字匹配）
SELECT 
    county.id AS county_dept_id,
    county.name AS county_dept_name,
    county.code AS county_dept_code,
    SUBSTRING_INDEX(county.code, '-', -1) AS dept_keyword,
    city.id AS city_dept_id,
    city.name AS city_dept_name,
    city.code AS city_dept_code,
    CASE 
        WHEN city.id IS NULL THEN '❌ 无对应市级部门'
        ELSE '✓ 有对应市级部门'
    END AS match_status
FROM sys_dept county
LEFT JOIN sys_dept county_company ON county.parent_id = county_company.id
LEFT JOIN sys_dept city_company ON county_company.parent_id = city_company.id
LEFT JOIN sys_dept city ON city.parent_id = city_company.id 
    AND city.type = 'DEPT'
    AND city.code LIKE CONCAT('%-', SUBSTRING_INDEX(county.code, '-', -1), '%')
WHERE county.type = 'DEPT' 
AND county.level = 4
ORDER BY county.parent_id, county.id;

