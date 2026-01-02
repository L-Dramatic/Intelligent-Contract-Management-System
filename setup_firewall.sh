#!/bin/bash
# ============================================================
# 配置防火墙脚本
# ============================================================

echo "=========================================="
echo "  配置防火墙规则"
echo "=========================================="
echo ""

# 检查ufw是否安装
if ! command -v ufw &> /dev/null; then
    echo "安装ufw..."
    apt update
    apt install -y ufw
fi

# 重要：先开放SSH端口22，防止启用防火墙后无法连接
echo "开放端口 22 (SSH)..."
ufw allow 22/tcp

# 开放应用端口
echo "开放端口 8080 (后端服务)..."
ufw allow 8080/tcp

echo "开放端口 5173 (前端服务)..."
ufw allow 5173/tcp

echo "开放端口 8765 (AI服务)..."
ufw allow 8765/tcp

# 如果ufw未启用，则启用它
if ufw status | grep -q "Status: inactive"; then
    echo "启用防火墙..."
    ufw --force enable
fi

echo ""
echo "防火墙状态:"
ufw status

echo ""
echo "=========================================="
echo "✅ 防火墙配置完成！"
echo "=========================================="
echo ""
echo "注意：如果是云服务器（阿里云/腾讯云等），"
echo "还需要在云控制台的安全组中开放这些端口！"
echo ""


