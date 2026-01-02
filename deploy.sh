#!/bin/bash
# ============================================================
# 服务器部署脚本
# 用于在Linux服务器上部署合同管理系统
# ============================================================

set -e  # 遇到错误立即退出

PROJECT_DIR="/opt/contract-system"
LOG_DIR="$PROJECT_DIR/logs"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

print_step() {
    echo -e "${CYAN}[步骤]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

print_error() {
    echo -e "${RED}[错误]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[警告]${NC} $1"
}

# 检查命令是否存在
check_command() {
    if ! command -v "$1" &> /dev/null; then
        print_error "$1 未安装，请先安装"
        return 1
    fi
    return 0
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if netstat -tuln 2>/dev/null | grep -q ":$port " || ss -tuln 2>/dev/null | grep -q ":$port "; then
        print_warning "端口 $port 已被占用"
        return 1
    fi
    return 0
}

echo "=========================================="
echo "  合同管理系统 - 服务器部署脚本"
echo "=========================================="
echo ""

# 1. 环境检查
print_step "1. 检查运行环境..."

check_command "java" || exit 1
check_command "mvn" || exit 1
check_command "node" || exit 1
check_command "npm" || exit 1
check_command "python3" || exit 1
check_command "pip3" || exit 1

# 检查版本
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_error "Java版本过低，需要JDK 17+"
    exit 1
fi

print_success "环境检查通过"
echo ""

# 2. 检查端口
print_step "2. 检查端口占用..."
check_port 8080 || print_warning "后端端口8080可能被占用"
check_port 5173 || print_warning "前端端口5173可能被占用"
check_port 8765 || print_warning "AI服务端口8765可能被占用"
echo ""

# 3. 创建日志目录
print_step "3. 创建日志目录..."
mkdir -p "$LOG_DIR"
print_success "日志目录已创建: $LOG_DIR"
echo ""

# 4. 安装依赖
print_step "4. 安装项目依赖..."

# 后端依赖
print_step "4.1 安装后端依赖..."
cd "$PROJECT_DIR/backend"
if [ ! -d "target" ] || [ "target/classes" -ot "pom.xml" ]; then
    mvn clean install -DskipTests
    print_success "后端依赖安装完成"
else
    print_warning "后端已编译，跳过"
fi

# 前端依赖
print_step "4.2 安装前端依赖..."
cd "$PROJECT_DIR/frontend"
if [ ! -d "node_modules" ]; then
    npm install
    print_success "前端依赖安装完成"
else
    print_warning "前端依赖已存在，跳过"
fi

# AI服务依赖
print_step "4.3 安装AI服务依赖..."
cd "$PROJECT_DIR/ai-service"
if ! python3 -c "import fastapi" 2>/dev/null; then
    pip3 install -r requirements.txt
    print_success "AI服务依赖安装完成"
else
    print_warning "AI服务依赖已存在，跳过"
fi

echo ""

# 5. 启动服务
print_step "5. 启动服务..."

# 停止旧服务（如果存在）
print_step "5.0 停止旧服务..."
pkill -f "uvicorn app.main:app" 2>/dev/null || true
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "vite" 2>/dev/null || true
sleep 2

# 启动AI服务
print_step "5.1 启动AI服务 (端口8765)..."
cd "$PROJECT_DIR/ai-service"
nohup python3 -m uvicorn app.main:app --host 0.0.0.0 --port 8765 > "$LOG_DIR/ai-service.log" 2>&1 &
AI_PID=$!
echo $AI_PID > "$LOG_DIR/ai-service.pid"
sleep 3
if ps -p $AI_PID > /dev/null; then
    print_success "AI服务已启动 (PID: $AI_PID)"
else
    print_error "AI服务启动失败，查看日志: tail -f $LOG_DIR/ai-service.log"
    exit 1
fi

# 启动后端服务
print_step "5.2 启动后端服务 (端口8080)..."
cd "$PROJECT_DIR/backend"
nohup mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > "$LOG_DIR/backend.pid"
print_warning "后端服务启动中，请等待30秒..."
sleep 30
if ps -p $BACKEND_PID > /dev/null; then
    print_success "后端服务已启动 (PID: $BACKEND_PID)"
else
    print_error "后端服务启动失败，查看日志: tail -f $LOG_DIR/backend.log"
    exit 1
fi

# 启动前端服务
print_step "5.3 启动前端服务 (端口5173)..."
cd "$PROJECT_DIR/frontend"
nohup npm run dev -- --host 0.0.0.0 > "$LOG_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > "$LOG_DIR/frontend.pid"
sleep 5
if ps -p $FRONTEND_PID > /dev/null; then
    print_success "前端服务已启动 (PID: $FRONTEND_PID)"
else
    print_error "前端服务启动失败，查看日志: tail -f $LOG_DIR/frontend.log"
    exit 1
fi

echo ""

# 6. 验证服务
print_step "6. 验证服务状态..."

sleep 5

# 检查AI服务
if curl -s http://localhost:8765 > /dev/null 2>&1; then
    print_success "AI服务响应正常"
else
    print_warning "AI服务可能未就绪，请稍后检查"
fi

# 检查后端服务
if curl -s http://localhost:8080/doc.html > /dev/null 2>&1; then
    print_success "后端服务响应正常"
else
    print_warning "后端服务可能未就绪，请稍后检查"
fi

# 检查前端服务
if curl -s http://localhost:5173 > /dev/null 2>&1; then
    print_success "前端服务响应正常"
else
    print_warning "前端服务可能未就绪，请稍后检查"
fi

echo ""
echo "=========================================="
print_success "部署完成！"
echo "=========================================="
echo ""
echo "服务访问地址："
echo "  前端页面:  http://$(hostname -I | awk '{print $1}'):5173"
echo "  后端API:   http://$(hostname -I | awk '{print $1}'):8080"
echo "  API文档:   http://$(hostname -I | awk '{print $1}'):8080/doc.html"
echo "  AI服务:    http://$(hostname -I | awk '{print $1}'):8765"
echo ""
echo "查看日志："
echo "  tail -f $LOG_DIR/ai-service.log"
echo "  tail -f $LOG_DIR/backend.log"
echo "  tail -f $LOG_DIR/frontend.log"
echo ""
echo "停止服务："
echo "  ./stop.sh"
echo ""


