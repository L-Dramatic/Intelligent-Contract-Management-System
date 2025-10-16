# 智能工程合同管理系统 (Intelligent Contract Management System)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

这是一个为软件工程课程设计而开发的智能合同管理平台。系统旨在通过自动化和智能化手段，解决传统工程合同管理中流程繁琐、风险高、监控难的核心痛点。

## ✨ 项目亮点 (Features)

*   **动态工作流引擎**: 可根据合同金额、类型等条件，自动匹配并执行多级审批流程。
*   **AI 赋能**: 集成大语言模型（LLM），实现合同草稿的智能生成与关键条款的风险审查。
*   **全周期监控**: 对工程进度款的支付计划进行自动化跟踪、提醒与超额预警。
*   **现代化技术栈**: 采用业界主流的前后端分离架构，兼具高性能与良好的开发体验。
*   **权限管理**: 基于角色的访问控制（RBAC），确保数据安全与操作合规。

## 🚀 技术栈 (Tech Stack)

*   **前端**: Vue 3 + Vite + Element Plus + Pinia + Axios
*   **后端**: Spring Boot 3 + Spring Security + MyBatis-Plus
*   **数据库**: MySQL 8.0
*   **AI 集成**: 调用 [文心一言/通义千问] API
*   **协作与部署**: Git, GitHub, Docker
## 📦 快速开始 (Getting Started)

> 此部分内容将在项目开发过程中逐步完善。

### 环境要求

*   JDK 17+
*   Maven 3.6+
*   Node.js 18+
*   MySQL 8.0+

### 安装与运行

1.  **克隆仓库**
    ```bash
    git clone https://github.com/[你的用户名]/[你的仓库名].git
    cd [你的仓库名]
    ```

2.  **后端启动**
    ```bash
    # 进入后端项目目录
    cd backend
    # 配置 application.yml 中的数据库连接信息
    # ...
    # 启动项目
    mvn spring-boot:run
    ```

3.  **前端启动**
    ```bash
    # 进入前端项目目录
    cd frontend
    # 安装依赖
    npm install
    # 启动开发服务器
    npm run dev
    ```

## 🧑‍💻 团队成员 (Team Members)

*   **[你的名字]** - 组长 & 后端开发
*   **[成员2名字]** - 前端开发
*   **[成员3名字]** - 测试 & 文档
*   ...

## ©️ 许可证 (License)

本项目采用 [MIT License](LICENSE) 开源许可证。
