@echo off
chcp 65001 >nul 2>&1
title 电信智慧合同管理系统启动器

:: 调用 PowerShell 脚本
powershell -ExecutionPolicy Bypass -File "%~dp0start.ps1"

