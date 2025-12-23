#!/bin/bash
# MySQL检查脚本 - 在服务器上运行

echo "=========================================="
echo "MySQL服务状态检查"
echo "=========================================="

# 1. 检查MySQL服务状态
echo -e "\n[1] 检查MySQL服务状态:"
sudo systemctl status mysql --no-pager -l || sudo systemctl status mysqld --no-pager -l

# 2. 检查MySQL进程
echo -e "\n[2] 检查MySQL进程:"
ps aux | grep mysql | grep -v grep

# 3. 检查3306端口监听
echo -e "\n[3] 检查3306端口监听:"
sudo netstat -tlnp | grep 3306 || sudo ss -tlnp | grep 3306

# 4. 检查MySQL配置文件
echo -e "\n[4] 检查MySQL绑定地址配置:"
if [ -f /etc/mysql/mysql.conf.d/mysqld.cnf ]; then
    echo "配置文件: /etc/mysql/mysql.conf.d/mysqld.cnf"
    sudo grep -E "bind-address|port" /etc/mysql/mysql.conf.d/mysqld.cnf
elif [ -f /etc/my.cnf ]; then
    echo "配置文件: /etc/my.cnf"
    sudo grep -E "bind-address|port" /etc/my.cnf
else
    echo "未找到MySQL配置文件"
fi

# 5. 检查防火墙状态
echo -e "\n[5] 检查防火墙状态:"
if command -v ufw &> /dev/null; then
    sudo ufw status | grep 3306
elif command -v firewall-cmd &> /dev/null; then
    sudo firewall-cmd --list-ports | grep 3306
else
    echo "未检测到常见防火墙工具"
fi

echo -e "\n=========================================="
echo "检查完成"
echo "=========================================="

