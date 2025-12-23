# 数据库重新初始化脚本
# 用于在数据库被删除后重新创建

$DB_HOST = "118.31.77.102"
$DB_PORT = "3306"
$DB_USER = "root"
$DB_PASSWORD = "Zlw.100525"
$DB_NAME = "contract_system"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "       电信智慧合同管理系统 - 数据库重新初始化" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# 检查mysql命令
$mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlCmd) {
    Write-Host "[错误] 未找到 'mysql' 命令。" -ForegroundColor Red
    Write-Host "请确保已安装 MySQL 并将 bin 目录添加到环境变量 PATH 中。" -ForegroundColor Yellow
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "[1/4] 测试数据库连接..." -ForegroundColor Yellow
$testResult = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -e "SELECT 1;" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[错误] 无法连接到数据库服务器！" -ForegroundColor Red
    Write-Host $testResult -ForegroundColor Red
    Write-Host ""
    Write-Host "可能原因：" -ForegroundColor Yellow
    Write-Host "1. 服务器MySQL未启动" -ForegroundColor Yellow
    Write-Host "2. 安全组未开放3306端口" -ForegroundColor Yellow
    Write-Host "3. MySQL未配置允许远程连接" -ForegroundColor Yellow
    Write-Host "4. 密码错误" -ForegroundColor Yellow
    Read-Host "按回车键退出"
    exit 1
}
Write-Host "[成功] 数据库连接正常" -ForegroundColor Green
Write-Host ""

Write-Host "[2/4] 创建数据库并导入基础表结构 (init.sql)..." -ForegroundColor Yellow
$initSql = Join-Path $PSScriptRoot "database\init.sql"
if (-not (Test-Path $initSql)) {
    Write-Host "[错误] 找不到文件: $initSql" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

$result = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" < $initSql 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[错误] 导入 init.sql 失败！" -ForegroundColor Red
    Write-Host $result -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}
Write-Host "[成功] 基础表结构导入完成" -ForegroundColor Green
Write-Host ""

Write-Host "[3/4] 导入初始数据 (init_data_v2.sql)..." -ForegroundColor Yellow
$initDataSql = Join-Path $PSScriptRoot "database\init_data_v2.sql"
if (-not (Test-Path $initDataSql)) {
    Write-Host "[警告] 找不到文件: $initDataSql，跳过" -ForegroundColor Yellow
} else {
    $result = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" $DB_NAME < $initDataSql 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[错误] 导入 init_data_v2.sql 失败！" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
    } else {
        Write-Host "[成功] 初始数据导入完成" -ForegroundColor Green
    }
}
Write-Host ""

Write-Host "[4/4] 导入审批场景配置 (db_upgrade_v5_complete_scenarios.sql)..." -ForegroundColor Yellow
$upgradeSql = Join-Path $PSScriptRoot "database\db_upgrade_v5_complete_scenarios.sql"
if (-not (Test-Path $upgradeSql)) {
    Write-Host "[警告] 找不到文件: $upgradeSql，跳过" -ForegroundColor Yellow
} else {
    $result = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" $DB_NAME < $upgradeSql 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[错误] 导入 db_upgrade_v5_complete_scenarios.sql 失败！" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
    } else {
        Write-Host "[成功] 审批场景配置导入完成" -ForegroundColor Green
    }
}
Write-Host ""

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "数据库重新初始化完成！" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "现在可以启动后端服务了：" -ForegroundColor Yellow
Write-Host "  cd backend" -ForegroundColor White
Write-Host "  mvn spring-boot:run" -ForegroundColor White
Write-Host ""
Read-Host "按回车键退出"

