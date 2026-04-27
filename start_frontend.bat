@echo off
chcp 65001 >nul
setlocal

echo 启动Vue前端开发服务器...
cd /d "%~dp0OnlineExamSystemVue"

set "NODE_EXE=node"
if defined NVM_HOME if exist "%NVM_HOME%\v16.13.2\node.exe" set "NODE_EXE=%NVM_HOME%\v16.13.2\node.exe"

for /f "delims=" %%v in ('"%NODE_EXE%" -v 2^>nul') do set "NODE_VERSION=%%v"
if not defined NODE_VERSION (
  echo 未检测到Node.js，请先安装Node 16.13.2后再启动前端。
  pause
  exit /b 1
)

echo 当前Node版本: %NODE_VERSION%
if /I not "%NODE_VERSION:~0,4%"=="v16." (
  echo.
  echo 警告: 当前前端依赖更适合Node 16.13.2。
  echo 如果启动失败，请先切换到Node 16.13.2后重试。
  echo.
)

"%NODE_EXE%" node_modules\webpack-dev-server\bin\webpack-dev-server.js --inline --progress --config build\webpack.dev.conf.js

pause
