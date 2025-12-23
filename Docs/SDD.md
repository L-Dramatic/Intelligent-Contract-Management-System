# 软件设计说明书 (SDD)

## Software Design Specification

---

**项目名称：中国移动智慧合同管理系统**

**版本**：v1.0

**编写日期**：2025年12月

---

## 目录

## 1. 引言 (Introduction)

### 1.1 编写目的

本软件设计说明书（Software Design Specification, SDS）旨在详细描述**中国移动智慧合同管理系统**的系统架构、模块设计、接口定义及数据库设计。本文档基于《软件需求规格说明书（SRS）v2.0》编写，是将业务需求转化为技术实现的蓝图。

本文档的主要预期读者包括：
- **系统架构师**：用于指导系统整体架构搭建与技术选型。
- **后端开发人员**：用于理解业务逻辑实现、数据库结构及接口规范。
- **前端开发人员**：用于理解页面交互逻辑及API调用方式。
- **AI算法工程师**：用于理解AI服务与业务系统的集成方式及数据流转。
- **测试人员**：用于编写集成测试用例和系统测试方案。

### 1.2 项目背景与范围

本项目旨在为中国移动开发一套智能化的合同管理系统，解决传统合同管理中起草效率低、审查依赖人工、审批流程僵化等痛点。系统聚焦于**Type A（工程施工）、Type B（运维服务）、Type C（IT服务）**三类核心合同，利用大语言模型（LLM）和检索增强生成（RAG）技术，实现合同的智能起草与风险审查。

**系统范围包括**：
- **前端子系统**：提供用户交互界面，包括合同管理、审批工作台、仪表盘等。
- **后端子系统**：处理核心业务逻辑，包括用户权限、工作流引擎、合同全生命周期管理。
- **AI服务子系统**：提供基于向量数据库的知识检索、合同生成及风险审查API。
- **数据库**：存储结构化业务数据（MySQL）及非结构化向量数据（ChromaDB）。

### 1.3 参考资料

1. 《中国移动智慧合同管理系统 - 软件需求规格说明书 (SRS) v2.0》
2. 《阿里巴巴Java开发手册（泰山版）》
3. Spring Boot 3.x 官方文档
4. Vue.js 3.x 官方文档
5. FastAPI 官方文档

### 1.4 术语与缩略语定义

| 术语/缩略语 | 全称 | 说明 |
| :--- | :--- | :--- |
| **SDD** | Software Design Specification | 软件设计说明书 |
| **SRS** | Software Requirements Specification | 软件需求规格说明书 |
| **RAG** | Retrieval-Augmented Generation | 检索增强生成，结合外部知识库增强LLM能力的技术 |
| **LLM** | Large Language Model | 大语言模型（如通义千问 Qwen） |
| **RBAC** | Role-Based Access Control | 基于角色的访问控制 |
| **DTO** | Data Transfer Object | 数据传输对象，用于层间数据传输 |
| **VO** | View Object | 视图对象，用于展示层数据封装 |
| **PO** | Persistent Object | 持久化对象，与数据库表一一对应 |
| **Pre-Flight Check** | - | 提交前审查规则引擎，用于硬性规则校验 |

---

## 2. 系统总体设计 (System Architecture)

### 2.1 设计原则

1.  **高内聚低耦合**：各子系统（前端、后端、AI服务）独立部署，通过标准RESTful API通信。
2.  **安全性优先**：鉴于电信行业数据的敏感性，系统采用JWT认证、RBAC权限控制及数据加密传输。
3.  **可扩展性**：工作流引擎采用配置化设计，支持通过修改配置适配不同类型的审批流程；AI服务采用插件式架构，便于更换底层模型。
4.  **领域驱动设计 (DDD)**：核心业务逻辑围绕“合同”这一聚合根展开，确保业务逻辑的纯净性。

### 2.2 系统逻辑架构 (Logical Architecture)

系统采用经典的分层架构，自下而上分为数据层、基础设施层、核心服务层、应用层及展示层。

![系统逻辑架构](SDD图片/2.1-系统逻辑架构.png)


### 2.3 技术架构选型 (Technology Stack)

#### 2.3.1 前端技术栈
-   **框架**: Vue.js 3.x (Composition API)
-   **构建工具**: Vite
-   **语言**: TypeScript
-   **UI 组件库**: Element Plus
-   **状态管理**: Pinia
-   **路由管理**: Vue Router
-   **HTTP 客户端**: Axios

#### 2.3.2 后端技术栈
-   **开发语言**: Java 21
-   **核心框架**: Spring Boot 3.5.6
-   **ORM 框架**: MyBatis Plus
-   **安全框架**: Spring Security + JWT
-   **API 文档**: Knife4j / Swagger
-   **工具库**: Hutool, Lombok

#### 2.3.3 AI 服务技术栈
-   **框架**: FastAPI (Python 3.10+)
-   **服务器**: Uvicorn
-   **大模型接口**: Aliyun Dashscope (通义千问)
-   **向量数据库**: ChromaDB
-   **Embedding 模型**: Sentence-Transformers
-   **文档处理**: PyPDF2, python-docx

#### 2.3.4 数据存储与环境
-   **关系型数据库**: MySQL 8.0+
-   **运行环境**: JDK 21, Python 3.10+, Node.js 18+

### 2.4 物理部署架构 (Deployment Architecture)

系统支持容器化部署，建议采用 Docker Compose 或 K8s 进行编排。

-   **Web Server**: Nginx 作为统一入口，负责静态资源托管及 API 请求转发。
-   **App Server**: Spring Boot 应用容器，运行核心业务逻辑。
-   **AI Server**: Python 应用容器，运行 AI 推理与检索服务，需配置 GPU 或高性能 CPU 资源。
-   **Database**: MySQL 容器与 ChromaDB 容器，挂载持久化存储卷。

各服务间通过内部虚拟网络通信，仅 Nginx 端口对外暴露（如 80/443），保障内部服务安全。

## 3. 系统功能模块设计 (Module Design)

### 3.1 模块划分概述

系统功能模块依据“高内聚、低耦合”原则进行划分，整体分为基础设施层、核心业务层和智能增强层。各层之间通过定义良好的接口进行交互。

![系统模块划分图](SDD图片/3.1-系统模块划分图.png)



### 3.2 基础设施层设计

#### 3.2.1 用户认证与权限管理 (RBAC)
采用基于角色的访问控制（RBAC）模型。
- **用户 (SysUser)**: 系统的登录主体，关联部门和角色。
- **角色 (SysRole)**: 权限的集合，如“普通员工”、“部门经理”、“法务专员”。
- **权限 (SysPermission)**: 具体的资源操作许可，如“合同:查看”、“合同:审批”。
- **认证机制**: 使用 Spring Security + JWT 实现无状态认证。登录成功后颁发 Token，后续请求需在 Header 中携带。

#### 3.2.2 组织架构管理 (Organization Tree)
针对中国移动的三级架构（省公司-地市-区县）设计树状组织结构。
- **部门 (SysDept)**: 具有层级关系（parentId），支持无限层级。
- **功能**: 提供部门树查询、部门人员关联查询，为工作流引擎的“部门经理审批”策略提供数据支撑。

### 3.3 核心业务层设计

#### 3.3.1 合同全生命周期管理 (CLM)
覆盖合同从起草、审批、签署到归档的全过程。核心实体为 `Contract`。

![合同状态机图](SDD图片/3.2-合同状态机图.png)


#### 3.3.2 审查规则引擎 (Pre-Flight Check)
在合同提交审批前进行的硬性规则检查，防止低级错误流入审批环节。
- **设计模式**: 策略模式 (Strategy Pattern)。
- **检查项**:
    - **必填项检查**: 关键字段是否缺失。
    - **附件完整性**: 根据合同类型（Type A/B/C）检查是否上传了强制附件（如安全协议）。
    - **数值逻辑**: 检查金额计算是否正确（如总价=单价*数量）。
    - **关键字黑名单**: 检查是否包含敏感词汇。

#### 3.3.3 审批流程引擎 (Workflow Engine)
自定义轻量级工作流引擎，支持动态流程配置。
- **流程定义**: 描述流程的结构（节点、连线）。
- **流程实例**: 具体的审批过程记录。

![审批流程活动图](SDD图片/3.3-审批流程活动图.png)


### 3.4 智能增强层设计

#### 3.4.1 知识库管理 (Knowledge Base)
- **数据源**: 法律法规、内部管理办法、历史合同模板。
- **处理流程**: 文本提取 -> 分块 (Chunking) -> 向量化 (Embedding) -> 存入 ChromaDB。
- **索引策略**: 混合索引（关键词 + 向量），提高检索准确率。

#### 3.4.2 AI 合同生成服务 (RAG)
利用 RAG 技术辅助用户快速起草合同。
- **输入**: 用户意图（如“起草一份基站建设合同，金额50万”）。
- **检索**: 从知识库中检索相关的模板片段和法律条款。
- **生成**: 组装 Prompt，调用 LLM 生成合同初稿。

![AI合同生成时序图](SDD图片/3.4-AI合同生成时序图.png)


#### 3.4.3 AI 风险审查服务 (Risk Review)
- **触发时机**: 合同提交审批后异步触发。
- **处理逻辑**:
    1.  将合同文本分段。
    2.  针对每一段，结合审查规则（Prompt）调用 LLM 进行风险识别。
    3.  汇总风险点，计算风险等级（高/中/低）。
    4.  生成审查报告，存入数据库供审批人查看。

## 4. 子系统详细设计 (Subsystem Detailed Design)

本章将系统划分为四个核心功能子系统进行详细设计：合同管理子系统、审批流程子系统、智能服务子系统和系统管理子系统。这种划分方式更贴合业务领域，便于理解各模块的职责边界。

### 4.1 合同管理子系统 (Contract Management Subsystem)

#### 4.1.1 功能描述
负责合同全生命周期的管理，包括合同起草、模板管理、合同详情查看、附件管理及归档。该子系统是业务的核心，与其他子系统交互频繁。

#### 4.1.2 核心类设计 (Class Design)
- **ContractController**: 接收前端请求，处理合同相关的 CRUD 操作。
- **ContractService**: 封装合同业务逻辑，如状态流转、金额校验。
- **ContractTemplate**: 合同模板实体，用于快速起草。
- **Attachment**: 附件实体，关联合同文件。

![合同管理子系统类图](SDD图片/4.1-合同管理子系统类图.png)



### 4.2 审批流程子系统 (Workflow Subsystem)

#### 4.2.1 功能描述
提供通用的审批流引擎能力，支持流程定义、流程实例启动、任务分配及审批操作。该子系统与合同状态紧密耦合，驱动合同状态的变更。

#### 4.2.2 核心类设计 (Class Design)
- **WorkflowEngine**: 流程引擎核心，负责解析流程定义和流转控制。
- **WfDefinition**: 流程定义实体，存储流程图结构（JSON）。
- **WfInstance**: 流程实例，代表一次具体的审批过程。
- **WfTask**: 待办任务，分配给具体审批人。

![审批流程子系统类图](SDD图片/4.2-审批流程子系统类图.png)



### 4.3 智能服务子系统 (Intelligent Service Subsystem)

#### 4.3.1 功能描述
封装所有 AI 相关能力，包括基于 RAG 的合同生成、智能风险审查及知识库管理。该子系统作为独立服务运行（Python），通过 REST API 为主系统提供支持。

#### 4.3.2 交互时序设计 (Sequence Design)
以“智能风险审查”为例，展示合同子系统与智能服务子系统的交互。

![智能审查时序图](SDD图片/4.3-智能审查时序图.png)



### 4.4 系统管理子系统 (System Management Subsystem)

#### 4.4.1 功能描述
负责系统的基础数据管理，包括组织架构（部门）、用户身份（账号）、权限控制（角色）及系统日志。

#### 4.4.2 核心类设计 (Class Design)
基于 RBAC 模型的设计。

![系统管理子系统类图](SDD图片/4.4-系统管理子系统类图.png)


## 5. 数据库设计 (Database Design)

### 5.1 数据库设计原则
1.  **规范化**: 遵循第三范式 (3NF)，减少数据冗余。
2.  **命名规范**: 表名使用 `小写_下划线` 格式（如 `sys_user`），主键统一为 `id` (BIGINT)。
3.  **通用字段**: 所有业务表包含 `created_at`, `updated_at`, `is_deleted` (逻辑删除)。
4.  **索引优化**: 针对高频查询字段（如 `contract_code`, `status`, `user_id`）建立索引。

### 5.2 概念模型设计 (ER Diagram)
![系统ER图](SDD图片/5.1-数据库ER图.png)


### 5.3 物理数据模型 (Data Schema)

#### 5.3.1 系统管理表

**用户表 (`sys_user`)**
| 字段名 | 类型 | 长度 | 说明 | 约束 |
| :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | 主键ID | PK, Auto Inc |
| username | VARCHAR | 50 | 用户名 | Unique, Not Null |
| password | VARCHAR | 100 | 密码 (BCrypt) | Not Null |
| real_name | VARCHAR | 50 | 真实姓名 | |
| dept_id | BIGINT | 20 | 部门ID | FK -> sys_dept.id |
| email | VARCHAR | 100 | 邮箱 | |
| phone | VARCHAR | 20 | 手机号 | |
| status | TINYINT | 1 | 状态 (1:正常, 0:禁用) | Default 1 |

**部门表 (`sys_dept`)**
| 字段名 | 类型 | 长度 | 说明 | 约束 |
| :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | 主键ID | PK |
| parent_id | BIGINT | 20 | 父部门ID | 0为根节点 |
| dept_name | VARCHAR | 50 | 部门名称 | Not Null |
| order_num | INT | 11 | 显示顺序 | |
| leader | VARCHAR | 50 | 负责人 | |

#### 5.3.2 合同业务表

**合同主表 (`contract`)**
| 字段名 | 类型 | 长度 | 说明 | 约束 |
| :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | 主键ID | PK |
| contract_code | VARCHAR | 64 | 合同编号 | Unique |
| title | VARCHAR | 200 | 合同标题 | Not Null |
| type | VARCHAR | 20 | 类型 (Type A/B/C) | |
| amount | DECIMAL | 18,2 | 合同金额 | |
| status | VARCHAR | 20 | 状态 (DRAFT, REVIEWING...) | |
| initiator_id | BIGINT | 20 | 发起人ID | FK -> sys_user.id |
| content | LONGTEXT | - | 合同正文 (HTML/Markdown) | |
| attachment_ids | VARCHAR | 500 | 附件ID列表 (逗号分隔) | |

**合同审查记录表 (`contract_review`)**
| 字段名 | 类型 | 长度 | 说明 | 约束 |
| :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | 主键ID | PK |
| contract_id | BIGINT | 20 | 合同ID | FK -> contract.id |
| risk_level | VARCHAR | 20 | 风险等级 (HIGH/MEDIUM/LOW) | |
| score | INT | 11 | 合规评分 (0-100) | |
| ai_report | JSON | - | AI审查报告详情 | |
| manual_comments | TEXT | - | 人工审核意见 | |

#### 5.3.3 工作流引擎表

**流程定义表 (`wf_definition`)**
| 字段名 | 类型 | 长度 | 说明 | 约束 |
| :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | 主键ID | PK |
| name | VARCHAR | 100 | 流程名称 | |
| version | INT | 11 | 版本号 | |
| json_content | JSON | - | 流程图结构定义 | Not Null |
| status | TINYINT | 1 | 状态 (1:激活, 0:挂起) | |

**流程实例表 (`wf_instance`)**
| 字段名 | 类型 | 长度 | 说明 | 约束 |
| :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | 主键ID | PK |
| definition_id | BIGINT | 20 | 流程定义ID | FK |
| business_id | BIGINT | 20 | 业务ID (合同ID) | |
| current_node_id | VARCHAR | 50 | 当前节点ID | |
| status | VARCHAR | 20 | 状态 (RUNNING, FINISHED...) | |

**流程任务表 (`wf_task`)**
| 字段名 | 类型 | 长度 | 说明 | 约束 |
| :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | 主键ID | PK |
| instance_id | BIGINT | 20 | 流程实例ID | FK |
| node_id | VARCHAR | 50 | 节点ID | |
| assignee_id | BIGINT | 20 | 处理人ID | FK -> sys_user.id |
| outcome | VARCHAR | 20 | 处理结果 (PASS, REJECT) | |
| comment | VARCHAR | 500 | 审批意见 | |

#### 5.3.4 知识库与AI表

**知识库文档表 (`knowledge_document`)**
| 字段名 | 类型 | 长度 | 说明 | 约束 |
| :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | 主键ID | PK |
| title | VARCHAR | 200 | 文档标题 | |
| category | VARCHAR | 50 | 分类 (LAW, TEMPLATE...) | |
| file_path | VARCHAR | 255 | 文件存储路径 | |
| vector_ids | JSON | - | 关联的向量ID列表 | 对应 ChromaDB |
| status | TINYINT | 1 | 状态 (0:处理中, 1:已索引) | |

## 6. 接口设计 (Interface Design)

### 6.1 接口设计规范 (RESTful API)

系统接口遵循 RESTful 风格，使用 HTTP 方法表达操作意图，使用 JSON 作为数据交换格式。

-   **URL 规范**: `/api/{version}/{resource}/{action}`，例如 `/api/v1/contracts/create`。
-   **HTTP 方法**:
    -   `GET`: 获取资源
    -   `POST`: 创建资源或执行复杂操作
    -   `PUT`: 全量更新资源
    -   `DELETE`: 删除资源
-   **统一响应格式**:
    ```json
    {
      "code": 200,
      "message": "操作成功",
      "data": { ... }
    }
    ```
-   **状态码定义**:
    -   `200`: 成功
    -   `401`: 未授权 (Token 无效/过期)
    -   `403`: 禁止访问 (权限不足)
    -   `404`: 资源不存在
    -   `500`: 服务器内部错误

### 6.2 内部子系统接口

#### 6.2.1 后端与AI服务交互接口

后端服务 (Java) 作为客户端，调用 AI 服务 (Python) 提供的能力。

**1. 合同生成接口**
-   **URL**: `POST /api/rag/generate`
-   **调用方**: 后端服务 -> AI 服务
-   **请求体**:
    ```json
    {
      "contract_type": "Type A",
      "requirements": "基站建设，金额50万，工期3个月",
      "user_id": 1001
    }
    ```
-   **响应体**:
    ```json
    {
      "content": "根据您的需求，已生成合同草稿...",
      "references": ["CMCC_ZJ_Construction_Master_Contract_1.md"]
    }
    ```

**2. 风险审查接口**
-   **URL**: `POST /api/rag/review`
-   **调用方**: 后端服务 -> AI 服务
-   **请求体**:
    ```json
    {
      "contract_id": 2023001,
      "content": "合同正文内容...",
      "rules": ["必须包含安全生产协议", "质保金比例不低于5%"]
    }
    ```
-   **响应体**:
    ```json
    {
      "risk_level": "MEDIUM",
      "score": 85,
      "issues": [
        { "severity": "HIGH", "description": "缺少安全生产协议附件", "suggestion": "请补充附件" }
      ]
    }
    ```

### 6.3 前后端交互接口

#### 6.3.1 合同管理模块

| 接口名称 | URL | 方法 | 说明 |
| :--- | :--- | :--- | :--- |
| 分页查询合同 | `/api/v1/contracts` | GET | 支持按状态、类型、时间筛选 |
| 获取合同详情 | `/api/v1/contracts/{id}` | GET | 返回合同基本信息及正文 |
| 创建合同草稿 | `/api/v1/contracts` | POST | 保存合同基本信息 |
| 更新合同内容 | `/api/v1/contracts/{id}` | PUT | 更新正文或附件 |
| 提交审批 | `/api/v1/contracts/{id}/submit` | POST | 触发 Pre-Flight Check 及工作流 |
| 删除合同 | `/api/v1/contracts/{id}` | DELETE | 仅限草稿状态 |

#### 6.3.2 工作流模块

| 接口名称 | URL | 方法 | 说明 |
| :--- | :--- | :--- | :--- |
| 获取待办任务 | `/api/v1/workflow/tasks/todo` | GET | 查询当前用户的待办 |
| 获取已办任务 | `/api/v1/workflow/tasks/done` | GET | 查询当前用户的已办 |
| 审批任务 | `/api/v1/workflow/tasks/{taskId}/complete` | POST | 提交审批结果 (PASS/REJECT) |
| 获取审批历史 | `/api/v1/workflow/instance/{instanceId}/history` | GET | 获取流程流转记录 |

#### 6.3.3 知识库模块

| 接口名称 | URL | 方法 | 说明 |
| :--- | :--- | :--- | :--- |
| 上传文档 | `/api/v1/knowledge/upload` | POST | 支持 PDF/Word/Markdown |
| 查询文档列表 | `/api/v1/knowledge/list` | GET | 分页查询 |
| 重建索引 | `/api/v1/knowledge/reindex` | POST | 触发向量库重建 |

![接口调用时序图](SDD图片/6.1-接口调用时序图.png)


## 7. 安全设计 (Security Design)

### 7.1 身份认证与会话管理 (Authentication & Session)

系统采用 **JWT (JSON Web Token)** 进行无状态身份认证，结合 Spring Security 框架实现。

-   **认证流程**:
    1.  用户提交用户名/密码。
    2.  后端校验通过后，生成包含用户ID、角色、过期时间的 JWT。
    3.  前端将 JWT 存储在 LocalStorage/SessionStorage。
    4.  后续请求在 HTTP Header `Authorization: Bearer <token>` 中携带。
-   **Token 策略**:
    -   **Access Token**: 有效期短（如 2 小时），用于访问资源。
    -   **Refresh Token**: 有效期长（如 7 天），用于刷新 Access Token，减少频繁登录。
-   **密码存储**: 使用 BCrypt 强哈希算法加盐存储，禁止明文存储。

![认证流程图](SDD图片/7.1-认证流程图.png)


### 7.2 访问控制策略 (Access Control)

采用 **RBAC (Role-Based Access Control)** 模型，实现细粒度的权限控制。

-   **URL 级控制**: 通过 Spring Security 的过滤器链配置，限制特定 URL 模式仅允许特定角色访问。
    -   `/api/admin/**` -> 仅限 `ADMIN` 角色
    -   `/api/contract/approve` -> 仅限 `MANAGER` 或 `LEADER` 角色
-   **方法级控制**: 使用 `@PreAuthorize` 注解控制 Service 方法的调用权限。
    ```java
    @PreAuthorize("hasAuthority('contract:audit')")
    public void auditContract(ApproveDTO dto) { ... }
    ```
-   **数据级控制**: 通过 MyBatis Plus 的拦截器实现数据权限过滤（Data Scope）。
    -   普通员工：只能查看自己创建的合同。
    -   部门经理：可以查看本部门及下属部门的合同。

### 7.3 数据安全与隐私保护 (Data Security)

鉴于电信行业数据的敏感性，采取多重措施保障数据安全。

-   **传输加密**: 全站强制启用 HTTPS (TLS 1.2+)，防止中间人攻击和数据窃听。
-   **敏感数据脱敏**:
    -   在日志打印和前端展示时，对手机号、身份证号等个人隐私信息进行掩码处理（如 `138****1234`）。
    -   使用 Jackson 序列化器自动处理脱敏逻辑。
-   **SQL 注入防御**:
    -   全面使用 MyBatis 的 `#{}` 预编译参数，严禁使用 `${}` 拼接 SQL。
    -   配置 Web 防火墙 (WAF) 拦截恶意 SQL 特征请求。
-   **文件安全**:
    -   上传文件进行类型校验（白名单机制）和病毒扫描。
    -   合同附件存储在非 Web 根目录下，通过流式接口下载，防止直接链接访问。
-   **审计日志**:
    -   记录所有关键操作（登录、审批、导出、删除）的日志，包含操作人、IP、时间、操作内容。
    -   审计日志单独存储，禁止篡改。

## 8. 非功能性设计 (Non-functional Design)

### 8.1 性能设计 (Performance)

-   **响应时间指标**:
    -   普通页面加载时间 < 1s。
    -   简单业务操作（如查询、保存草稿）响应时间 < 500ms。
    -   复杂业务操作（如提交审批、生成报表）响应时间 < 3s。
    -   AI 合同生成响应时间 < 30s（流式输出首字响应 < 3s）。
-   **并发能力**:
    -   支持 500+ 在线用户。
    -   支持 50+ 并发审批操作。
-   **优化策略**:
    -   **前端**: 启用 Gzip 压缩，静态资源 CDN 加速，路由懒加载。
    -   **后端**: 数据库读写分离，热点数据（如数据字典、组织架构）使用 Redis 缓存。
    -   **AI服务**: 使用异步队列（Celery/RabbitMQ）处理耗时任务，避免阻塞主线程。

### 8.2 可靠性与可用性 (Reliability & Availability)

-   **可用性目标**: 系统可用性达到 99.9%（全年停机时间 < 8.76小时）。
-   **容错机制**:
    -   **服务降级**: 当 AI 服务不可用时，系统自动切换为人工起草模式，不影响核心审批流程。
    -   **断点续传**: 大文件上传支持断点续传，防止网络波动导致上传失败。
    -   **数据备份**: 数据库每日全量备份，每小时增量备份，保留最近 30 天数据。
-   **监控告警**:
    -   集成 Prometheus + Grafana 监控系统资源（CPU/内存/磁盘）及应用指标（QPS/错误率）。
    -   关键服务宕机或错误率飙升时，通过邮件/短信发送告警。

### 8.3 可扩展性 (Scalability)

-   **水平扩展**: 后端服务和 AI 服务均为无状态设计，支持通过增加节点（Pod）实现线性扩展。
-   **模块化设计**: 业务模块（合同、工作流、知识库）之间低耦合，未来可拆分为微服务独立部署。
-   **配置化**: 审批流程、审查规则、数据字典等均支持后台配置，无需修改代码即可适应业务变化。

## 9. 附录 (Appendix)

### 9.1 第三方库清单

| 组件名称 | 版本 | 许可证 | 用途 |
| :--- | :--- | :--- | :--- |
| Spring Boot | 3.5.6 | Apache 2.0 | 后端核心框架 |
| MyBatis Plus | 3.5.x | Apache 2.0 | ORM 框架 |
| Vue.js | 3.x | MIT | 前端核心框架 |
| Element Plus | 2.x | MIT | UI 组件库 |
| FastAPI | 0.104+ | MIT | AI 服务框架 |
| LangChain | 0.1.x | MIT | LLM 应用开发框架 |
| ChromaDB | 0.4.x | Apache 2.0 | 向量数据库 |

### 9.2 修订历史

| 版本 | 日期 | 修改人 | 说明 |
| :--- | :--- | :--- | :--- |
| v1.0 | 2025-12-22 | 架构组 | 初始版本发布 |
