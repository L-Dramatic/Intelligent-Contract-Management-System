#!/bin/bash
# ============================================================
# 电信智慧合同管理系统 - Linux/Mac 启动脚本
# Telecom Intelligent Contract Management System - Start Script
# ============================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

# 获取脚本所在目录
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_ROOT"

# 显示Banner
show_banner() {
    echo ""
    echo -e "${CYAN}  ╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}  ║                                                          ║${NC}"
    echo -e "${CYAN}  ║       ${YELLOW}电信智慧合同管理系统${CYAN}                       ║${NC}"
    echo -e "${CYAN}  ║       ${WHITE}Telecom Intelligent Contract System${CYAN}           ║${NC}"
    echo -e "${CYAN}  ║                                                          ║${NC}"
    echo -e "${CYAN}  ╚══════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

# 打印步骤信息
print_step() {
    local icon=$1
    local message=$2
    local type=${3:-info}
    
    case $type in
        success) color=$GREEN ;;
        warning) color=$YELLOW ;;
        error) color=$RED ;;
        progress) color=$MAGENTA ;;
        *) color=$CYAN ;;
    esac
    
    echo -e "${color}${icon}${NC} ${message}"
}

# 检查命令是否存在
check_command() {
    command -v "$1" >/dev/null 2>&1
}

# 显示加载动画
show_spinner() {
    local pid=$1
    local message=$2
    local spin='⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏'
    local i=0
    
    while kill -0 $pid 2>/dev/null; do
        i=$(( (i+1) % 10 ))
        printf "\r${CYAN}${spin:$i:1}${NC} ${message}"
        sleep 0.1
    done
    printf "\r"
}

# 显示Banner
show_banner

print_step "📋" "项目根目录: $PROJECT_ROOT" "info"
echo ""

# ============================================================
# 环境检查
# ============================================================
print_step "🔍" "正在检查运行环境..." "progress"
echo ""

ENV_CHECK_PASSED=true

# 检查 Node.js
echo -n "  [1/4] 检查 Node.js... "
if check_command node; then
    NODE_VERSION=$(node -v)
    echo -e "${GREEN}✓ 已安装 ($NODE_VERSION)${NC}"
else
    echo -e "${RED}✗ 未安装${NC}"
    echo -e "        ${YELLOW}请访问 https://nodejs.org 下载安装${NC}"
    ENV_CHECK_PASSED=false
fi

# 检查 npm
echo -n "  [2/4] 检查 npm... "
if check_command npm; then
    NPM_VERSION=$(npm -v)
    echo -e "${GREEN}✓ 已安装 (v$NPM_VERSION)${NC}"
else
    echo -e "${RED}✗ 未安装${NC}"
    ENV_CHECK_PASSED=false
fi

# 检查 Java
echo -n "  [3/4] 检查 Java... "
if check_command java; then
    echo -e "${GREEN}✓ 已安装${NC}"
else
    echo -e "${RED}✗ 未安装${NC}"
    echo -e "        ${YELLOW}请安装 JDK 17 或更高版本${NC}"
    ENV_CHECK_PASSED=false
fi

# 检查 Maven
echo -n "  [4/4] 检查 Maven... "
MAVEN_AVAILABLE=false
if check_command mvn; then
    echo -e "${GREEN}✓ 已安装${NC}"
    MAVEN_AVAILABLE=true
elif [ -f "$PROJECT_ROOT/backend/mvnw" ]; then
    echo -e "${YELLOW}○ 将使用 Maven Wrapper${NC}"
    MAVEN_AVAILABLE=true
    chmod +x "$PROJECT_ROOT/backend/mvnw"
else
    echo -e "${RED}✗ 未安装${NC}"
    echo -e "        ${YELLOW}请安装 Maven 或确保项目包含 mvnw${NC}"
    ENV_CHECK_PASSED=false
fi

echo ""

if [ "$ENV_CHECK_PASSED" = false ]; then
    print_step "❌" "环境检查未通过，请安装缺失的软件后重试" "error"
    exit 1
fi

print_step "✅" "环境检查通过!" "success"
echo ""

# ============================================================
# 选择启动模式
# ============================================================
print_step "🚀" "请选择启动模式:" "info"
echo ""
echo "  [1] 启动全部 (后端 + 前端)"
echo "  [2] 仅启动后端"
echo "  [3] 仅启动前端"
echo "  [4] 安装依赖 (首次使用)"
echo -e "  ${WHITE}[0] 退出${NC}"
echo ""
read -p "请输入选项 [1-4]: " choice

case $choice in
    1)
        # 启动全部
        echo ""
        print_step "🔧" "准备启动后端和前端服务..." "progress"
        
        # 启动后端（后台）
        print_step "☕" "启动后端服务 (Spring Boot)..." "progress"
        cd "$PROJECT_ROOT/backend"
        
        if [ -f "mvnw" ]; then
            ./mvnw spring-boot:run &
        else
            mvn spring-boot:run &
        fi
        BACKEND_PID=$!
        
        print_step "⏳" "等待后端启动 (15秒)..." "info"
        sleep 15
        
        # 启动前端（前台）
        print_step "🎨" "启动前端服务 (Vite)..." "progress"
        cd "$PROJECT_ROOT/frontend"
        
        echo ""
        print_step "✅" "服务启动中!" "success"
        echo ""
        echo -e "  📌 后端地址: ${CYAN}http://localhost:8080${NC}"
        echo -e "  📌 API文档:  ${CYAN}http://localhost:8080/doc.html${NC}"
        echo -e "  📌 前端地址: ${GREEN}http://localhost:5173${NC}"
        echo ""
        echo -e "  ${YELLOW}💡 提示: 后端启动需要约30秒，请稍等后访问${NC}"
        echo -e "  ${YELLOW}💡 按 Ctrl+C 停止所有服务${NC}"
        echo ""
        
        # 捕获退出信号
        trap "echo ''; print_step '👋' '正在停止服务...' 'info'; kill $BACKEND_PID 2>/dev/null; exit 0" INT TERM
        
        npm run dev
        ;;
    2)
        # 仅启动后端
        echo ""
        print_step "☕" "启动后端服务 (Spring Boot)..." "progress"
        cd "$PROJECT_ROOT/backend"
        
        if [ -f "mvnw" ]; then
            ./mvnw spring-boot:run
        else
            mvn spring-boot:run
        fi
        ;;
    3)
        # 仅启动前端
        echo ""
        print_step "🎨" "启动前端服务 (Vite)..." "progress"
        cd "$PROJECT_ROOT/frontend"
        npm run dev
        ;;
    4)
        # 安装依赖
        echo ""
        print_step "📦" "开始安装项目依赖..." "progress"
        echo ""
        
        # 安装前端依赖
        print_step "🎨" "[1/2] 安装前端依赖..." "progress"
        cd "$PROJECT_ROOT/frontend"
        npm install
        
        if [ $? -eq 0 ]; then
            print_step "✅" "前端依赖安装完成!" "success"
        else
            print_step "⚠️" "前端依赖安装可能存在问题" "warning"
        fi
        
        # 编译后端
        print_step "☕" "[2/2] 编译后端项目..." "progress"
        cd "$PROJECT_ROOT/backend"
        
        if [ -f "mvnw" ]; then
            ./mvnw compile -DskipTests
        else
            mvn compile -DskipTests
        fi
        
        if [ $? -eq 0 ]; then
            print_step "✅" "后端编译完成!" "success"
        else
            print_step "⚠️" "后端编译可能存在问题" "warning"
        fi
        
        echo ""
        print_step "🎉" "依赖安装完成! 现在可以使用选项 [1] 启动项目" "success"
        ;;
    0)
        print_step "👋" "再见!" "info"
        exit 0
        ;;
    *)
        print_step "❌" "无效选项" "error"
        ;;
esac

