@echo off
echo Fixing database schema...
echo Please enter your MySQL root password if prompted.
mysql -u root -p contract_system < backend/sql/fix_wf_instance_remark.sql
if %errorlevel% neq 0 (
    echo Error: Failed to execute SQL script. Please check your password and database name.
    pause
    exit /b %errorlevel%
)
echo Database fixed successfully!
pause

