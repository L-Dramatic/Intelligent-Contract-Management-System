#!/bin/bash
# ============================================================
# 停止服务脚本
# ============================================================

PROJECT_DIR="/opt/contract-system"
LOG_DIR="$PROJECT_DIR/logs"

echo "正在停止服务..."

# 从PID文件读取并停止
if [ -f "$LOG_DIR/ai-service.pid" ]; then
    PID=$(cat "$LOG_DIR/ai-service.pid")
    if ps -p $PID > /dev/null 2>&1; then
        kill $PID
        echo "AI服务已停止 (PID: $PID)"
    fi
    rm -f "$LOG_DIR/ai-service.pid"
fi

if [ -f "$LOG_DIR/backend.pid" ]; then
    PID=$(cat "$LOG_DIR/backend.pid")
    if ps -p $PID > /dev/null 2>&1; then
        kill $PID
        echo "后端服务已停止 (PID: $PID)"
    fi
    rm -f "$LOG_DIR/backend.pid"
fi

if [ -f "$LOG_DIR/frontend.pid" ]; then
    PID=$(cat "$LOG_DIR/frontend.pid")
    if ps -p $PID > /dev/null 2>&1; then
        kill $PID
        echo "前端服务已停止 (PID: $PID)"
    fi
    rm -f "$LOG_DIR/frontend.pid"
fi

# 强制停止（如果PID文件不存在）
pkill -f "uvicorn app.main:app" 2>/dev/null && echo "AI服务进程已终止"
pkill -f "spring-boot:run" 2>/dev/null && echo "后端服务进程已终止"
pkill -f "vite" 2>/dev/null && echo "前端服务进程已终止"

echo "所有服务已停止"


