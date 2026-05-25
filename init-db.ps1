Write-Host "========================================" -ForegroundColor Cyan
Write-Host "初始化MySQL数据库" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$sqlFile = "E:\HuaYuWork\AIdemo\init-database.sql"

if (!(Test-Path $sqlFile)) {
    Write-Host "错误: 未找到SQL文件" -ForegroundColor Red
    exit 1
}

Write-Host "正在初始化数据库..." -ForegroundColor Yellow
mysql -u root -p123456 < $sqlFile

if ($LASTEXITCODE -eq 0) {
    Write-Host "数据库初始化成功!" -ForegroundColor Green
} else {
    Write-Host "数据库初始化失败，请检查MySQL连接" -ForegroundColor Red
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")