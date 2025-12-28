@echo off
chcp 65001 >nul
echo ============================================================
echo 数据库修复脚本：为 t_contract 表添加 updated_at 字段
echo ============================================================
echo.

echo 请选择数据库连接方式：
echo [1] 本地数据库 (localhost)
echo [2] 远程数据库 (118.31.77.102)
echo.

set /p choice="请输入选项 [1/2]: "

if "%choice%"=="1" (
    set DB_HOST=localhost
    set DB_USER=root
    echo 请输入数据库密码：
    set /p DB_PASS=
) else if "%choice%"=="2" (
    set DB_HOST=118.31.77.102
    set DB_USER=root
    echo 请输入数据库密码：
    set /p DB_PASS=
) else (
    echo 无效选项
    pause
    exit /b
)

echo.
echo 正在连接数据库并执行修复...
echo.

mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASS% contract_system -e "ALTER TABLE t_contract ADD COLUMN updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER created_at;" 2>nul

if %errorlevel%==0 (
    echo.
    echo [成功] updated_at 字段已添加！
    echo.
) else (
    echo.
    echo [检查] 字段可能已存在，正在验证...
    mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASS% contract_system -e "SHOW COLUMNS FROM t_contract LIKE 'updated_at';" 2>nul
    if %errorlevel%==0 (
        echo [成功] updated_at 字段已存在！
    ) else (
        echo [错误] 请检查数据库连接和权限
    )
    echo.
)

pause

