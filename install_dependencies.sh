#!/bin/bash
# ============================================================
# Ubuntu 环境依赖安装脚本
# ============================================================

set -e

echo "=========================================="
echo "  安装服务器环境依赖"
echo "=========================================="
echo ""

# 更新包列表
echo "[1/6] 更新包列表..."
apt update

# 安装JDK 21
echo "[2/6] 安装JDK 21..."
apt install -y openjdk-21-jdk
echo "Java版本:"
java -version

# 安装Maven
echo "[3/6] 安装Maven..."
apt install -y maven
echo "Maven版本:"
mvn -version

# 安装Node.js 20
echo "[4/6] 安装Node.js 20..."
curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
apt install -y nodejs
echo "Node.js版本:"
node -v
echo "npm版本:"
npm -v

# 安装pip（如果还没有）
echo "[5/6] 安装pip..."
apt install -y python3-pip
echo "pip版本:"
pip3 --version

# 安装其他工具
echo "[6/6] 安装其他工具..."
apt install -y git curl wget

echo ""
echo "=========================================="
echo "✅ 所有依赖安装完成！"
echo "=========================================="
echo ""
echo "已安装的软件："
echo "  Java: $(java -version 2>&1 | head -n 1)"
echo "  Maven: $(mvn -version | head -n 1)"
echo "  Node.js: $(node -v)"
echo "  npm: $(npm -v)"
echo "  Python: $(python3 --version)"
echo "  pip: $(pip3 --version | head -n 1)"
echo ""


