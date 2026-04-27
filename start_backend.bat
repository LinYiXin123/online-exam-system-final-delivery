@echo off
chcp 65001 >nul
setlocal

echo 启动Java后端API服务...
cd /d "%~dp0OnlineExamSystemApi"

if not exist "target\OnlineExamSystemApi-0.0.1-SNAPSHOT.jar" (
  echo 未找到已打包的JAR，尝试使用项目自带Maven进行打包...
  call "%~dp0apache-maven-3.8.8\bin\mvn.cmd" clean package -DskipTests
  if errorlevel 1 (
    echo Maven打包失败，请检查环境后重试。
    pause
    exit /b 1
  )
)

java -jar target\OnlineExamSystemApi-0.0.1-SNAPSHOT.jar
pause
