Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI智能知识库问答系统启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "请确保以下服务已启动:" -ForegroundColor Yellow
Write-Host "1. MySQL (端口 3306)" -ForegroundColor White
Write-Host ""

Write-Host "正在启动Milvus向量服务..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'E:\HuaYuWork\AIdemo\milvus-service'; pip install -r requirements.txt; python main.py"

Write-Host "等待Milvus服务启动 (10秒)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "正在启动后端服务..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'E:\HuaYuWork\AIdemo\backend'; mvn spring-boot:run"

Write-Host "等待后端服务启动 (30秒)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "正在启动前端服务..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'E:\HuaYuWork\AIdemo\frontend'; npm install; npm run dev"

Write-Host ""
Write-Host "系统启动中..." -ForegroundColor Cyan
Write-Host "前端地址: http://localhost:3000" -ForegroundColor White
Write-Host "后端地址: http://localhost:8080" -ForegroundColor White
Write-Host "Milvus服务: http://localhost:8081" -ForegroundColor White
Write-Host ""
Write-Host "按任意键退出此窗口..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")