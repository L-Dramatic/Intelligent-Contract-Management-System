#!/bin/bash
# ============================================================
# 更新代码并重启服务脚本
# ============================================================

set -e

PROJECT_DIR="/opt/contract-system"

echo "=========================================="
echo "  更新代码并重启服务"
echo "=========================================="
echo ""

cd "$PROJECT_DIR"

# 1. 备份当前代码
BACKUP_DIR="backup-$(date +%Y%m%d-%H%M%S)"
echo "[1] 备份当前代码到 $BACKUP_DIR..."
mkdir -p "$BACKUP_DIR"
cp -r backend/src "$BACKUP_DIR/backend-src" 2>/dev/null || true
cp -r frontend/src "$BACKUP_DIR/frontend-src" 2>/dev/null || true
cp -r ai-service/app "$BACKUP_DIR/ai-service-app" 2>/dev/null || true
echo "备份完成"
echo ""

# 2. 拉取最新代码（如果使用Git）
if [ -d ".git" ]; then
    echo "[2] 从Git拉取最新代码..."
    git pull
    echo "代码更新完成"
else
    echo "[2] 未检测到Git仓库，跳过代码拉取"
    echo "请手动上传新代码到服务器"
fi
echo ""

# 3. 更新依赖（如果需要）
read -p "是否更新依赖? (y/N): " update_deps
if [ "$update_deps" = "y" ] || [ "$update_deps" = "Y" ]; then
    echo "[3] 更新依赖..."
    
    cd "$PROJECT_DIR/backend"
    mvn clean install -DskipTests
    
    cd "$PROJECT_DIR/frontend"
    npm install
    
    cd "$PROJECT_DIR/ai-service"
    pip3 install -r requirements.txt
    
    echo "依赖更新完成"
else
    echo "[3] 跳过依赖更新"
fi
echo ""

# 4. 重启服务
echo "[4] 重启服务..."
cd "$PROJECT_DIR"
./restart.sh

echo ""
echo "更新完成！"


