@echo off
chcp 65001 >nul
echo ============================================================
echo AI合同助手服务启动脚本
echo ============================================================

cd /d "%~dp0"

echo.
echo [1] 检查Python环境...
python --version

echo.
echo [2] 启动服务...
echo     访问地址: http://localhost:8000
echo     测试页面: test_chat.html
echo     按 Ctrl+C 停止服务
echo.

cd app
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload

pause

