@echo off
chcp 65001 >nul 2>&1
title 合同管理系统启动器（跳过AI服务）

echo ============================================================
echo   合同管理系统 - 快速启动（仅后端+前端）
echo ============================================================
echo.
echo [提示] 此脚本跳过AI服务，避免conda环境问题
echo.

cd /d "%~dp0"

echo [1/3] 启动后端服务...
start "Backend Service" powershell -NoExit -Command "cd '%~dp0backend'; if (Test-Path 'mvnw.cmd') { .\mvnw.cmd spring-boot:run } else { mvn spring-boot:run }"

echo [等待] 等待后端启动（15秒）...
timeout /t 15 /nobreak >nul

echo [2/3] 启动前端服务...
cd frontend
if not exist "node_modules" (
    echo [提示] 首次运行，安装依赖...
    call npm install
)

echo.
echo ============================================================
echo [完成] 服务启动中...
echo ============================================================
echo   前端: http://localhost:5173
echo   后端: http://localhost:8080
echo   文档: http://localhost:8080/doc.html
echo ============================================================
echo.

npm run dev
