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

### 环境要求

*   JDK 17+ (推荐21)
*   Maven 3.6+
*   Node.js 20+
*   MySQL 8.0+

### ⚡ 一键启动（推荐）

项目提供了跨平台的一键启动脚本，自动检查环境并启动服务：

**Windows 用户：** 双击 `start.bat` 或运行 `.\start.ps1`

**Linux/Mac 用户：** 运行 `chmod +x start.sh && ./start.sh`

首次使用请选择 `[4] 安装依赖`，然后选择 `[1] 启动全部`

### 手动启动

1.  **克隆仓库**
    ```bash
    git clone https://github.com/[你的用户名]/[你的仓库名].git
    cd [你的仓库名]
    ```

2.  **初始化数据库**
    ```bash
    mysql -u root -p < database/init.sql
    ```

3.  **配置数据库密码**
    
    编辑 `backend/src/main/resources/application.properties`，修改 `spring.datasource.password`

4.  **后端启动**
    ```bash
    cd backend
    mvn spring-boot:run
    ```

5.  **前端启动**
    ```bash
    cd frontend
    npm install
    npm run dev
    ```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:5173 |
| 后端API | http://localhost:8080 |
| API文档 | http://localhost:8080/doc.html |

详细说明请查看 [启动指南](./Docs/启动指南.md)

## 🧑‍💻 团队成员 (Team Members)

*   **[李星烁]**
*   **[许奕]**
*   **[张洛梧]**
*   **[肖相宇]**

## ©️ 许可证 (License)

本项目采用 [MIT License](LICENSE) 开源许可证。
