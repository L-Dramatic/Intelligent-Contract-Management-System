@echo off
chcp 65001 >nul
title 数据库初始化工具

echo ============================================================
echo       电信智慧合同管理系统 - 数据库初始化工具
echo ============================================================
echo.

:: 1. 检查 MySQL 命令是否存在
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 未找到 'mysql' 命令。
    echo 请确保已安装 MySQL 并将 bin 目录添加到环境变量 PATH 中。
    echo.
    pause
    exit /b
)

:: 2. 设置数据库连接信息
echo 请输入本地 MySQL 的 root 密码：
set /p DB_PASSWORD=

set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=contract_system
set SQL_FILE=database\init.sql

echo.
echo [1/3] 正在连接 MySQL...

:: 3. 尝试连接并创建数据库
mysql -h %DB_HOST% -P %DB_PORT% -u root -p"%DB_PASSWORD%" -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul

if %errorlevel% neq 0 (
    echo [ERROR] 连接失败或创建数据库失败！
    echo 可能原因：
    echo 1. 密码错误
    echo 2. MySQL 服务未启动
    echo.
    echo 正在尝试启动 MySQL 服务 (需要管理员权限)...
    net start mysql 2>nul
    if %errorlevel% neq 0 (
        echo [ERROR] 无法启动 MySQL 服务。请手动检查。
    ) else (
        echo [INFO] MySQL 服务已启动，请重新运行此脚本。
    )
    pause
    exit /b
)

echo [INFO] 数据库 '%DB_NAME%' 准备就绪。
echo.

:: 4. 导入表结构
echo [2/3] 正在导入表结构 (%SQL_FILE%)...
if not exist "%SQL_FILE%" (
    echo [ERROR] 找不到 SQL 文件: %SQL_FILE%
    pause
    exit /b
)

mysql -h %DB_HOST% -P %DB_PORT% -u root -p"%DB_PASSWORD%" %DB_NAME% < "%SQL_FILE%"

if %errorlevel% neq 0 (
    echo [ERROR] 导入失败！
    pause
    exit /b
)

echo [INFO] 表结构导入成功！
echo.

:: 5. 提示修改配置文件
echo [3/3] 检查配置文件...
echo.
echo [IMPORTANT] 请确保后端配置文件中的密码与您刚才输入的密码一致！
echo 文件路径: backend\src\main\resources\application.properties
echo.
echo spring.datasource.password=%DB_PASSWORD%
echo.

echo ============================================================
echo       数据库初始化完成！现在可以运行 start.bat 启动项目了。
echo ============================================================
echo.
pause

