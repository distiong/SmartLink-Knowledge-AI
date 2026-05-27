Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI智能知识库问答系统 - 一键启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 设置JDK 17
$env:JAVA_HOME = "D:\softtware\java17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Write-Host "[1/4] 使用JDK: $env:JAVA_HOME" -ForegroundColor Green

# 检查MySQL
Write-Host "[2/4] 检查MySQL连接..." -ForegroundColor Yellow
$mysqlTest = mysql -u root -p123456 -e "SELECT 1" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: MySQL连接失败，请确保MySQL已启动" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}
Write-Host "MySQL连接成功!" -ForegroundColor Green

# 初始化数据库
Write-Host "初始化数据库..." -ForegroundColor Yellow
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS knowledge_base DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>&1 | Out-Null
Write-Host "数据库初始化完成!" -ForegroundColor Green

# 启动Milvus服务
Write-Host "[3/4] 启动Milvus向量服务 (端口 8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'E:\HuaYuWork\AIdemo\milvus-service'; python main.py"

Write-Host "等待Milvus服务启动 (10秒)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 启动后端
Write-Host "[4/4] 启动后端服务 (端口 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:JAVA_HOME='D:\softtware\java17'; `$env:Path=`$env:JAVA_HOME+'\bin;'+`$env:Path; cd 'E:\HuaYuWork\AIdemo\backend'; mvn spring-boot:run"

Write-Host "等待后端服务启动 (30秒)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 启动前端
Write-Host "启动前端服务 (端口 5173)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'E:\HuaYuWork\AIdemo\frontend'; npm run dev"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "系统启动完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "访问地址:" -ForegroundColor White
Write-Host "  前端: http://localhost:5173" -ForegroundColor White
Write-Host "  后端: http://localhost:8080" -ForegroundColor White
Write-Host "  Milvus: http://localhost:8081" -ForegroundColor White
Write-Host ""
Write-Host "登录账号:" -ForegroundColor White
Write-Host "  管理员: admin / admin123" -ForegroundColor White
Write-Host ""
Write-Host "关闭系统请运行: stop.ps1" -ForegroundColor Yellow
Write-Host ""
Write-Host "按任意键退出此窗口..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
