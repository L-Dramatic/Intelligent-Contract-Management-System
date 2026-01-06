# 服务器部署指南

## 📋 部署前准备

### 1. 服务器要求

- **操作系统**: Linux (CentOS 7+/Ubuntu 18+)
- **内存**: 至少 2GB
- **磁盘**: 至少 10GB 可用空间
- **网络**: 可访问互联网（用于下载依赖）

### 2. 需要安装的软件

```bash
# JDK 21
sudo yum install java-21-openjdk java-21-openjdk-devel -y
# 或 Ubuntu: sudo apt install openjdk-21-jdk -y

# Maven
sudo yum install maven -y
# 或 Ubuntu: sudo apt install maven -y

# Node.js 20+
curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
sudo yum install -y nodejs
# 或 Ubuntu: curl -fsSL https://deb.nodesource.com/setup_20.x | sudo bash - && sudo apt install -y nodejs

# Python 3.10+
sudo yum install python3 python3-pip -y
# 或 Ubuntu: sudo apt install python3 python3-pip -y
```

### 3. 检查端口

确保以下端口未被占用：
- **8080**: 后端服务
- **5173**: 前端服务
- **8765**: AI服务

```bash
sudo netstat -tulpn | grep -E '8080|5173|8765'
```

## 🚀 部署步骤

### 步骤1: 上传项目代码到服务器

**方式A: 使用Git（推荐）**

```bash
# 在服务器上
cd /opt
sudo git clone <你的Git仓库地址> contract-system
cd contract-system
```

**方式B: 使用SCP上传**

```bash
# 在本地执行
scp -r /path/to/your/project root@你的服务器IP:/opt/contract-system
```

### 步骤2: 配置数据库连接

数据库已经在远程服务器 `118.31.77.102`，配置文件已经正确，**一般无需修改**。

如果需要修改，编辑：
```bash
vim /opt/contract-system/backend/src/main/resources/application.properties
```

### 步骤3: 配置AI服务

确认AI服务的API Key已配置：
```bash
vim /opt/contract-system/ai-service/config.ini
```

确认 `tongyi_api_key` 已设置。

### 步骤4: 配置防火墙

**CentOS/RHEL (firewalld):**
```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=5173/tcp
sudo firewall-cmd --permanent --add-port=8765/tcp
sudo firewall-cmd --reload
```

**Ubuntu (ufw):**
```bash
# 重要：先开放SSH端口22，防止启用防火墙后无法连接
sudo ufw allow 22/tcp
sudo ufw allow 8080/tcp
sudo ufw allow 5173/tcp
sudo ufw allow 8765/tcp
sudo ufw reload
```

**云服务器**: 在云控制台的安全组中开放这些端口。

### 步骤5: 执行部署脚本

```bash
cd /opt/contract-system

# 给脚本添加执行权限
chmod +x deploy.sh stop.sh restart.sh update.sh

# 执行部署
./deploy.sh
```

部署脚本会自动：
1. 检查运行环境
2. 检查端口占用
3. 安装项目依赖
4. 启动三个服务（AI、后端、前端）
5. 验证服务状态

### 步骤6: 验证部署

```bash
# 检查服务是否运行
ps aux | grep -E 'uvicorn|spring-boot|vite'

# 检查端口监听
sudo netstat -tulpn | grep -E '8080|5173|8765'

# 测试服务响应
curl http://localhost:8080/doc.html
curl http://localhost:5173
curl http://localhost:8765
```

### 步骤7: 从浏览器访问

在浏览器中输入：
```
http://你的服务器公网IP:5173
```

应该能看到登录页面。

## 📝 常用操作

### 查看日志

```bash
# 查看所有日志
tail -f /opt/contract-system/logs/ai-service.log
tail -f /opt/contract-system/logs/backend.log
tail -f /opt/contract-system/logs/frontend.log
```

### 停止服务

```bash
cd /opt/contract-system
./stop.sh
```

### 重启服务

```bash
cd /opt/contract-system
./restart.sh
```

### 更新代码

```bash
cd /opt/contract-system
./update.sh
```

## 🔧 故障排查

### 问题1: 服务启动失败

**检查日志:**
```bash
tail -50 /opt/contract-system/logs/backend.log
tail -50 /opt/contract-system/logs/ai-service.log
tail -50 /opt/contract-system/logs/frontend.log
```

**常见原因:**
- 端口被占用
- 依赖未安装
- 配置文件错误
- 数据库连接失败

### 问题2: 无法从浏览器访问

**检查:**
1. 防火墙是否开放端口
2. 云服务器安全组是否配置
3. 服务是否正常运行: `ps aux | grep -E 'uvicorn|spring-boot|vite'`

### 问题3: 前端无法连接后端

**检查:**
1. 后端是否正常运行: `curl http://localhost:8080/doc.html`
2. 前端代理配置: `frontend/vite.config.ts`
3. 浏览器控制台错误（F12）

### 问题4: AI功能不可用

**检查:**
1. AI服务是否运行: `curl http://localhost:8765`
2. API Key是否正确: `cat ai-service/config.ini`
3. 后端配置: `backend/src/main/resources/application.properties` 中的 `ai.service.base-url`

## 📞 获取帮助

如果遇到问题，请：
1. 查看日志文件
2. 检查服务状态
3. 确认配置文件正确

---

**部署完成后，系统可以通过浏览器访问！**


