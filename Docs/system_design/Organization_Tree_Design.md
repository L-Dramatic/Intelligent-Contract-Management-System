# 中国移动省级公司组织树设计方案

> **文档用途**：组织架构设计与数据库初始化方案  
> **版本**：1.0  
> **最后更新**：2025-12-22  
> **作者**：系统架构师

---

## 1. 设计原则

### 1.1 核心原则

| 原则 | 说明 |
|------|------|
| **单树结构** | 以省公司为根节点，所有市级、县级、部门构成一棵完整的树 |
| **部门与职位分离** | 组织树只存储部门层级，职位/角色通过用户表关联 |
| **标准化模板** | 提供标准组织架构模板，管理员可根据实际情况调整 |
| **县市平级** | 市级职能部门与县级分公司是兄弟节点，共同向市级分公司汇报 |
| **名称统一** | 部门命名标准化（如"网络部"），便于县→市自动匹配 |

### 1.2 关键约束

- **BR-100**：组织树必须有且仅有一个根节点（省公司）
- **BR-101**：县级分公司必须隶属于市级分公司
- **BR-102**：同一层级的部门名称不能重复
- **BR-103**：禁止循环引用（部门不能移动到自己的子节点下）

---

## 2. 组织架构设计

### 2.1 四级层级结构

```
第1级：省公司（根节点）
  ├─ 第2级：省级职能部门（6个核心部门）
  ├─ 第2级：市级分公司（N个，模板提供2个示例）
       ├─ 第3级：市级职能部门（7个标准部门）
       ├─ 第3级：县级分公司（M个，每市不同）
            └─ 第4级：县级职能部门（4个精简部门）
```

### 2.2 完整树形结构示意图

```
中国移动XX省公司（ROOT, id=1, level=1）
│
├── 省公司网络部（id=10, level=2）
├── 省公司市场部（id=11, level=2）
├── 省公司政企部（id=12, level=2）
├── 省公司法务部（id=13, level=2）
├── 省公司财务部（id=14, level=2）
├── 省公司综合部（id=15, level=2）
│
├── A市分公司（id=100, level=2）
│   ├── A市网络部（id=101, level=3）
│   ├── A市市场经营部（id=102, level=3）
│   ├── A市政企客户部（id=103, level=3）
│   ├── A市法务部（id=104, level=3）
│   ├── A市财务部（id=105, level=3）
│   ├── A市综合部（id=106, level=3）
│   ├── A市采购部（id=107, level=3）
│   ├── C县分公司（id=150, level=3）← 与市级部门平级
│   │   ├── C县综合部（id=151, level=4）
│   │   ├── C县市场部（id=152, level=4）
│   │   ├── C县网络部（id=153, level=4）
│   │   └── C县政企部（id=154, level=4）
│   └── D县分公司（id=160, level=3）
│       ├── D县综合部（id=161, level=4）
│       ├── D县市场部（id=162, level=4）
│       ├── D县网络部（id=163, level=4）
│       └── D县政企部（id=164, level=4）
│
└── B市分公司（id=200, level=2）
    ├── B市网络部（id=201, level=3）
    ├── B市市场经营部（id=202, level=3）
    ├── B市政企客户部（id=203, level=3）
    ├── B市法务部（id=204, level=3）
    ├── B市财务部（id=205, level=3）
    ├── B市综合部（id=206, level=3）
    ├── B市采购部（id=207, level=3）
    └── E县分公司（id=250, level=3）
        ├── E县综合部（id=251, level=4）
        ├── E县市场部（id=252, level=4）
        ├── E县网络部（id=253, level=4）
        └── E县政企部（id=254, level=4）
```

---

## 3. 部门设置说明

### 3.1 省级部门（6个核心部门）

| 部门代码 | 部门名称 | 主要职责 | 对应审批角色 |
|---------|---------|---------|------------|
| PROV-NET | 省公司网络部 | 全省网络规划、重大工程审查 | 省级技术审查 |
| PROV-MKT | 省公司市场部 | 全省营销策略、品牌管理 | 省级市场审查 |
| PROV-GOV | 省公司政企部 | 重大政企项目（>200万） | 省级政企审查 |
| PROV-LEGAL | 省公司法务部 | 重大合同法务审查、合规管理 | 省级法务审查 |
| PROV-FIN | 省公司财务部 | 全省预算管理、超大额审计 | 省级财务审查 |
| PROV-ADMIN | 省公司综合部 | 省级人事、行政、后勤 | 省级管理支撑 |

**设计依据**：
- 基于调研文档第19-31行，市级分公司的部门设置
- 省级部门数量精简，聚焦核心管控职能

### 3.2 市级部门（7个标准部门）

| 部门代码 | 部门名称 | 主要职责 | 对应Master Workflow角色 |
|---------|---------|---------|----------------------|
| CITY-X-NET | X市网络部 | 网络运维、基站建设、代维管理 | RF Engineer, Network Engineer, Network Planning |
| CITY-X-MKT | X市市场经营部 | B2C业务、渠道管理、终端销售 | Market Specialist |
| CITY-X-GOV | X市政企客户部 | B2B业务、DICT项目、政企客户 | Solution Architect, DICT PM |
| CITY-X-LEGAL | X市法务部 | 合同法务审查、法律风险控制 | Legal Reviewer (LR) |
| CITY-X-FIN | X市财务部 | 成本审计、财务审核、应收检查 | Cost Audit (CA), Finance Receivable Check (FRC) |
| CITY-X-ADMIN | X市综合部 | 人事、行政、印章管理、后勤 | Admin Support |
| CITY-X-PROC | X市采购部 | 供应商管理、采购审查 | Procurement Specialist (PS), Vendor Management (VM) |

**设计依据**：
- 基于调研文档第33-96行，市级分公司的部门设置
- 覆盖Master Workflow配置矩阵中的所有审批角色
- 网络部承担A/B类合同的技术审查
- 政企部承担C类合同的技术审查
- 法务部、财务部、采购部为共性支撑部门

### 3.3 县级部门（4个精简部门）

| 部门代码 | 部门名称 | 主要职责 | 说明 |
|---------|---------|---------|------|
| COUNTY-X-ADMIN | X县综合部 | 人事、行政、财务报账、后勤 | 多职能合并（调研文档第115行） |
| COUNTY-X-MKT | X县市场部 | 营销、渠道拓展、业务发展 | 发起B2C类合同 |
| COUNTY-X-NET | X县网络部 | 网络维护、宽带装维、故障处理 | 发起A/B类合同 |
| COUNTY-X-GOV | X县政企部 | 政企客户拓展、项目实施 | 发起C类合同 |

**设计依据**：
- 基于调研文档第111-119行，县级分公司采用"小而全"、"一专多能"的精简模式
- 县级分公司主要职责是发起合同，审批权在市级

---

## 4. 数据库设计

### 4.1 组织架构表（sys_dept）

```sql
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID（0表示根节点）',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `code` varchar(50) DEFAULT NULL COMMENT '部门代码',
  `type` varchar(20) DEFAULT 'DEPT' COMMENT '类型: PROVINCE（省公司）, CITY（市公司）, COUNTY（县公司）, DEPT（职能部门）',
  `level` int DEFAULT 1 COMMENT '树层级（1=根节点，2=省级部门/市级分公司，3=市级部门/县级分公司，4=县级部门）',
  `manager_id` bigint DEFAULT NULL COMMENT '部门负责人用户ID',
  `sort_order` int DEFAULT 0 COMMENT '排序序号',
  `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除（软删除）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_type` (`type`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织架构表';
```

**字段说明**：
- `type`：区分组织类型，用于快速判断是公司实体还是职能部门
- `level`：树的深度，用于权限控制和查询优化
- `code`：部门代码，便于系统集成和数据导入
- `sort_order`：同级部门的显示顺序

### 4.2 角色定义表（sys_role）

```sql
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色编码（对应Master Workflow中的Role Code）',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_category` varchar(50) DEFAULT NULL COMMENT '角色类别（BUSINESS/TECHNICAL/LEGAL/FINANCE/IT/MANAGEMENT/EXECUTIVE）',
  `dept_type_required` varchar(50) DEFAULT NULL COMMENT '要求的部门类型（如NET-网络部, LEGAL-法务部）',
  `z_level_min` varchar(10) DEFAULT NULL COMMENT '最低Z岗级要求',
  `description` varchar(200) DEFAULT NULL COMMENT '角色说明',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色定义表';
```

### 4.3 用户表（sys_user）

```sql
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID（关联sys_dept.id）',
  `primary_role` varchar(50) DEFAULT NULL COMMENT '主要角色编码（关联sys_role.role_code）',
  `z_level` varchar(10) DEFAULT NULL COMMENT 'Z岗级（Z8, Z9, Z10, Z11, Z12, Z13, Z14, Z15）',
  `direct_leader_id` bigint DEFAULT NULL COMMENT '直属上级用户ID',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `is_active` tinyint DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_primary_role` (`primary_role`),
  KEY `idx_z_level` (`z_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 4.4 用户角色关联表（sys_user_role）

```sql
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `effective_dept_id` bigint DEFAULT NULL COMMENT '生效部门ID（可选，用于跨部门兼职）',
  `is_primary` tinyint DEFAULT 0 COMMENT '是否主要角色',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role_dept` (`user_id`, `role_code`, `effective_dept_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表（支持一人多角色）';
```

---

## 5. 初始化数据脚本

### 5.1 组织架构初始化

```sql
-- ==================== 组织架构初始化（标准模板） ====================
-- 说明：这是中国移动省级公司的标准组织架构模板
--      部署后，管理员可根据本省实际情况调整

-- 第1层：省公司（根节点）
INSERT INTO sys_dept (id, parent_id, name, code, type, level, manager_id, sort_order) VALUES 
(1, 0, '中国移动XX省公司', 'PROVINCE', 'PROVINCE', 1, NULL, 0);

-- 第2层：省级职能部门
INSERT INTO sys_dept (id, parent_id, name, code, type, level, manager_id, sort_order) VALUES 
(10, 1, '省公司网络部', 'PROV-NET', 'DEPT', 2, NULL, 1),
(11, 1, '省公司市场部', 'PROV-MKT', 'DEPT', 2, NULL, 2),
(12, 1, '省公司政企部', 'PROV-GOV', 'DEPT', 2, NULL, 3),
(13, 1, '省公司法务部', 'PROV-LEGAL', 'DEPT', 2, NULL, 4),
(14, 1, '省公司财务部', 'PROV-FIN', 'DEPT', 2, NULL, 5),
(15, 1, '省公司综合部', 'PROV-ADMIN', 'DEPT', 2, NULL, 6);

-- 第2层：市级分公司（模板提供2个示例）
INSERT INTO sys_dept (id, parent_id, name, code, type, level, manager_id, sort_order) VALUES 
(100, 1, 'A市分公司', 'CITY-A', 'CITY', 2, NULL, 10),
(200, 1, 'B市分公司', 'CITY-B', 'CITY', 2, NULL, 20);

-- 第3层：A市职能部门
INSERT INTO sys_dept (id, parent_id, name, code, type, level, manager_id, sort_order) VALUES 
(101, 100, 'A市网络部', 'CITY-A-NET', 'DEPT', 3, NULL, 1),
(102, 100, 'A市市场经营部', 'CITY-A-MKT', 'DEPT', 3, NULL, 2),
(103, 100, 'A市政企客户部', 'CITY-A-GOV', 'DEPT', 3, NULL, 3),
(104, 100, 'A市法务部', 'CITY-A-LEGAL', 'DEPT', 3, NULL, 4),
(105, 100, 'A市财务部', 'CITY-A-FIN', 'DEPT', 3, NULL, 5),
(106, 100, 'A市综合部', 'CITY-A-ADMIN', 'DEPT', 3, NULL, 6),
(107, 100, 'A市采购部', 'CITY-A-PROC', 'DEPT', 3, NULL, 7);

-- 第3层：B市职能部门（结构与A市相同）
INSERT INTO sys_dept (id, parent_id, name, code, type, level, manager_id, sort_order) VALUES 
(201, 200, 'B市网络部', 'CITY-B-NET', 'DEPT', 3, NULL, 1),
(202, 200, 'B市市场经营部', 'CITY-B-MKT', 'DEPT', 3, NULL, 2),
(203, 200, 'B市政企客户部', 'CITY-B-GOV', 'DEPT', 3, NULL, 3),
(204, 200, 'B市法务部', 'CITY-B-LEGAL', 'DEPT', 3, NULL, 4),
(205, 200, 'B市财务部', 'CITY-B-FIN', 'DEPT', 3, NULL, 5),
(206, 200, 'B市综合部', 'CITY-B-ADMIN', 'DEPT', 3, NULL, 6),
(207, 200, 'B市采购部', 'CITY-B-PROC', 'DEPT', 3, NULL, 7);

-- 第3层：县级分公司（示例：A市下属2个县，B市下属1个县）
INSERT INTO sys_dept (id, parent_id, name, code, type, level, manager_id, sort_order) VALUES 
(150, 100, 'C县分公司', 'COUNTY-C', 'COUNTY', 3, NULL, 10),
(160, 100, 'D县分公司', 'COUNTY-D', 'COUNTY', 3, NULL, 11),
(250, 200, 'E县分公司', 'COUNTY-E', 'COUNTY', 3, NULL, 10);

-- 第4层：C县职能部门
INSERT INTO sys_dept (id, parent_id, name, code, type, level, manager_id, sort_order) VALUES 
(151, 150, 'C县综合部', 'COUNTY-C-ADMIN', 'DEPT', 4, NULL, 1),
(152, 150, 'C县市场部', 'COUNTY-C-MKT', 'DEPT', 4, NULL, 2),
(153, 150, 'C县网络部', 'COUNTY-C-NET', 'DEPT', 4, NULL, 3),
(154, 150, 'C县政企部', 'COUNTY-C-GOV', 'DEPT', 4, NULL, 4);

-- 第4层：D县职能部门（结构与C县相同）
INSERT INTO sys_dept (id, parent_id, name, code, type, level, manager_id, sort_order) VALUES 
(161, 160, 'D县综合部', 'COUNTY-D-ADMIN', 'DEPT', 4, NULL, 1),
(162, 160, 'D县市场部', 'COUNTY-D-MKT', 'DEPT', 4, NULL, 2),
(163, 160, 'D县网络部', 'COUNTY-D-NET', 'DEPT', 4, NULL, 3),
(164, 160, 'D县政企部', 'COUNTY-D-GOV', 'DEPT', 4, NULL, 4);

-- 第4层：E县职能部门
INSERT INTO sys_dept (id, parent_id, name, code, type, level, manager_id, sort_order) VALUES 
(251, 250, 'E县综合部', 'COUNTY-E-ADMIN', 'DEPT', 4, NULL, 1),
(252, 250, 'E县市场部', 'COUNTY-E-MKT', 'DEPT', 4, NULL, 2),
(253, 250, 'E县网络部', 'COUNTY-E-NET', 'DEPT', 4, NULL, 3),
(254, 250, 'E县政企部', 'COUNTY-E-GOV', 'DEPT', 4, NULL, 4);
```

### 5.2 角色定义初始化

```sql
-- ==================== 角色定义初始化 ====================
-- 对应Master Workflow配置矩阵中的角色

INSERT INTO sys_role (role_code, role_name, role_category, dept_type_required, z_level_min, description) VALUES 
-- 发起人
('INITIATOR', '合同发起人', 'BUSINESS', NULL, 'Z8', '县级/市级业务人员，发起合同审批'),

-- 管理层
('DEPT_MANAGER', '部门经理', 'MANAGEMENT', NULL, 'Z12', '市级部门经理，部门级审批'),
('VICE_PRESIDENT', '副总经理', 'EXECUTIVE', NULL, 'Z13', '市级分公司副总经理，高级审批'),
('GENERAL_MANAGER', '总经理', 'EXECUTIVE', NULL, 'Z14', '市级分公司总经理，最终审批'),
('T1M', '三重一大会议', 'EXECUTIVE', NULL, 'Z13', '市级分公司执行委员会集体决策'),

-- 技术审查（网络类）
('PROJECT_MANAGER', '项目经理', 'TECHNICAL', 'NET', 'Z10', '工程项目技术审查'),
('RF_ENGINEER', '射频工程师', 'TECHNICAL', 'NET', 'Z9', '基站代维技术审查'),
('NETWORK_ENGINEER', '网络工程师', 'TECHNICAL', 'NET', 'Z9', '光缆代维技术审查'),
('NETWORK_PLANNING', '网络规划', 'TECHNICAL', 'NET', 'Z10', '网络战略对齐审查'),
('FACILITY_COORDINATOR', '设施协调员', 'TECHNICAL', 'ADMIN', 'Z9', '零星维修技术审查'),
('BROADBAND_SPECIALIST', '家宽专家', 'TECHNICAL', 'NET', 'Z9', '家宽代维技术审查'),
('OPS_CENTER', '运营中心', 'TECHNICAL', 'NET', 'Z10', '应急保障快速通道'),

-- 设计与质量
('DESIGN_REVIEWER', '设计审查', 'TECHNICAL', 'NET', 'Z10', '装修工程设计标准审查'),
('SITE_ACQUISITION', '站点获取', 'TECHNICAL', 'NET', 'Z10', '基站物业租赁审查'),

-- 法务审查
('LEGAL_REVIEWER', '法务审查员', 'LEGAL', 'LEGAL', 'Z11', '合同法律合规性审查'),

-- 财务审查
('COST_AUDITOR', '成本审计员', 'FINANCE', 'FIN', 'Z10', '工程造价审计'),
('FINANCE_RECEIVABLE', '财务应收检查', 'FINANCE', 'FIN', 'Z10', 'DICT背靠背项目应收验证'),

-- IT审查
('TECHNICAL_LEAD', 'IT技术负责人', 'IT', 'GOV', 'Z11', 'IT项目代码架构审查'),
('SECURITY_REVIEWER', 'IT安全审查', 'IT', 'GOV', 'Z10', 'IT项目网络安全合规审查'),
('IT_ARCHITECT', 'IT架构师', 'IT', 'GOV', 'Z11', 'IT项目企业架构审查'),

-- 采购审查
('PROCUREMENT_SPECIALIST', '采购专员', 'PROCUREMENT', 'PROC', 'Z9', '商用软件采购审查'),
('VENDOR_MANAGER', '供应商管理', 'PROCUREMENT', 'PROC', 'Z10', '供应商资质审查'),

-- DICT审查
('SOLUTION_ARCHITECT', '解决方案架构师', 'DICT', 'GOV', 'Z11', 'DICT项目技术方案审查'),
('DICT_PM', 'DICT项目经理', 'DICT', 'GOV', 'Z10', 'DICT项目治理审查'),

-- 客服审查
('CUSTOMER_SERVICE_LEAD', '客服主管', 'BUSINESS', 'MKT', 'Z10', '家宽代维服务影响审查');
```

---

## 6. 县级→市级自动匹配逻辑

### 6.1 匹配原则

**场景**：C县网络部发起合同 → 需要A市网络部审批

**匹配规则**：
1. 找到发起人所在的县级分公司
2. 向上找到县级分公司的上级市级分公司
3. 在市级分公司下查找**名称匹配**的部门（如"网络部"）

### 6.2 SQL查询示例

```sql
-- 查询：C县网络部（dept_id=153）对应的市级网络部

-- Step 1: 找到县级分公司
SELECT parent_id AS county_company_id
FROM sys_dept
WHERE id = 153;  -- C县网络部
-- 结果：county_company_id = 150（C县分公司）

-- Step 2: 找到市级分公司
SELECT parent_id AS city_company_id
FROM sys_dept
WHERE id = 150;  -- C县分公司
-- 结果：city_company_id = 100（A市分公司）

-- Step 3: 在市级分公司下找同名部门
SELECT id, name
FROM sys_dept
WHERE parent_id = 100  -- A市分公司
  AND name LIKE '%网络部%'
  AND type = 'DEPT';
-- 结果：id=101, name='A市网络部'
```

### 6.3 通用查询函数

```sql
-- 存储过程：查找县级部门对应的市级部门
DELIMITER $$
CREATE PROCEDURE find_city_level_dept(
  IN p_county_dept_id BIGINT,  -- 县级部门ID
  OUT p_city_dept_id BIGINT    -- 对应的市级部门ID
)
BEGIN
  DECLARE v_county_company_id BIGINT;
  DECLARE v_city_company_id BIGINT;
  DECLARE v_dept_name_pattern VARCHAR(100);
  
  -- 1. 获取县级部门名称关键字（如"网络部"）
  SELECT SUBSTRING_INDEX(name, '县', -1) INTO v_dept_name_pattern
  FROM sys_dept
  WHERE id = p_county_dept_id;
  
  -- 2. 获取县级分公司ID
  SELECT parent_id INTO v_county_company_id
  FROM sys_dept
  WHERE id = p_county_dept_id;
  
  -- 3. 获取市级分公司ID
  SELECT parent_id INTO v_city_company_id
  FROM sys_dept
  WHERE id = v_county_company_id;
  
  -- 4. 查找市级对应部门
  SELECT id INTO p_city_dept_id
  FROM sys_dept
  WHERE parent_id = v_city_company_id
    AND name LIKE CONCAT('%', v_dept_name_pattern)
    AND type = 'DEPT'
  LIMIT 1;
  
END$$
DELIMITER ;

-- 使用示例
CALL find_city_level_dept(153, @city_dept_id);
SELECT @city_dept_id;  -- 结果：101（A市网络部）
```

---

## 7. 审批人查找逻辑

### 7.1 查找流程

```
1. 根据合同类型 + 金额 → 匹配审批场景（查询Master Workflow矩阵）
2. 获取审批节点列表（每个节点包含：角色编码 + 级别）
3. 对于每个节点：
   3.1 如果是市级（CITY），找到市级对应部门
   3.2 在该部门中查找具备指定角色的用户
   3.3 按Z岗级、工作量等规则筛选
4. 创建审批任务，分配给选定的审批人
```

### 7.2 SQL查询示例

**场景**：查找A市网络部的"射频工程师"

```sql
-- 查询1：在指定部门中查找具备指定角色的用户
SELECT u.id, u.real_name, u.z_level
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
WHERE u.dept_id = 101  -- A市网络部
  AND ur.role_code = 'RF_ENGINEER'
  AND u.is_active = 1;

-- 查询2：如果有多个候选人，按Z岗级和工作负载筛选
SELECT u.id, u.real_name, u.z_level, COUNT(wt.id) AS pending_tasks
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN wf_task wt ON wt.assignee_id = u.id AND wt.status = 0
WHERE u.dept_id = 101
  AND ur.role_code = 'RF_ENGINEER'
  AND u.is_active = 1
GROUP BY u.id
ORDER BY u.z_level DESC, pending_tasks ASC
LIMIT 1;
```

---

## 8. 管理功能设计

### 8.1 组织树管理API

| API端点 | 方法 | 功能 | 权限要求 |
|---------|------|------|---------|
| `/api/dept/tree` | GET | 获取完整组织树 | 所有用户 |
| `/api/dept` | POST | 新增部门 | 省级管理员 |
| `/api/dept/{id}` | PUT | 修改部门 | 省级管理员 |
| `/api/dept/{id}` | DELETE | 删除部门 | 省级管理员 |
| `/api/dept/{id}/move` | PUT | 移动部门 | 省级管理员 |
| `/api/dept/{id}/children` | GET | 获取子部门 | 部门经理以上 |

### 8.2 验证规则

| 规则ID | 验证内容 | 错误提示 |
|--------|---------|---------|
| VALID-001 | 不能创建多个根节点 | "组织树只能有一个根节点" |
| VALID-002 | 不能将部门移动到自己的子节点下 | "不能形成循环引用" |
| VALID-003 | 删除前检查是否有子部门 | "该部门下存在子部门，无法删除" |
| VALID-004 | 删除前检查是否有关联用户 | "该部门下存在用户，请先调整用户归属" |
| VALID-005 | 部门代码唯一性 | "部门代码已存在" |

---

## 9. 使用说明

### 9.1 系统部署流程

1. **执行数据库脚本**：
   ```bash
   mysql -u root -p contract_system < init.sql
   mysql -u root -p contract_system < init_data.sql  # 包含组织树初始化
   ```

2. **管理员首次配置**：
   - 登录系统（省级管理员账号）
   - 进入"组织架构管理"页面
   - 修改根节点名称：`中国移动XX省公司` → `中国移动四川省公司`
   - 调整市级分公司：删除模板中的"A市"、"B市"，新增实际市公司
   - 调整县级分公司：根据实际情况添加各县分公司
   - 设置部门负责人

3. **用户数据导入**：
   - 批量导入用户基础信息
   - 分配用户到对应部门
   - 设置用户角色

### 9.2 日常维护

| 场景 | 操作步骤 |
|------|---------|
| 新增县级分公司 | 组织架构管理 → 选择市级分公司 → 新增子节点 → 选择类型"COUNTY" |
| 部门重组 | 选择部门 → 拖拽到新的上级节点 |
| 人员调动 | 用户管理 → 修改用户的dept_id |
| 部门撤销 | 先转移部门下的用户 → 删除部门（软删除） |

---

## 10. 扩展性设计

### 10.1 支持虚拟组织

如需支持项目组、临时组织等虚拟部门：

```sql
ALTER TABLE sys_dept ADD COLUMN `is_virtual` tinyint DEFAULT 0 COMMENT '是否虚拟组织';
ALTER TABLE sys_dept ADD COLUMN `parent_ids` varchar(500) COMMENT '多父节点ID列表（JSON格式）';
```

### 10.2 支持跨部门兼职

通过 `sys_user_role.effective_dept_id` 实现：

```sql
-- 示例：张三在A市网络部担任部门经理，同时在A市政企部兼任技术顾问
INSERT INTO sys_user_role (user_id, role_code, effective_dept_id) VALUES 
(1001, 'DEPT_MANAGER', 101),      -- A市网络部经理
(1001, 'TECHNICAL_LEAD', 103);    -- A市政企部技术顾问（兼职）
```

---

## 11. 附录

### 11.1 参考文档

- `Master_Workflow_Configuration_Matrix_CN.md` - 审批流程配置矩阵
- `Approval_Workflow_Engine_Design.md` - 审批流程引擎设计
- `中国移动合同审批流程构建_251220_154924.md` - 组织架构调研报告

### 11.2 相关表结构

- `sys_dept` - 组织架构表
- `sys_user` - 用户表
- `sys_role` - 角色定义表
- `sys_user_role` - 用户角色关联表
- `wf_definition` - 工作流定义表
- `wf_task` - 工作流任务表

---

> **文档结束**
