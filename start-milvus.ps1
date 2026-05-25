Write-Host "========================================" -ForegroundColor Cyan
Write-Host "启动 Milvus 向量服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$milvusDir = "E:\HuaYuWork\AIdemo\milvus-service"

if (!(Test-Path $milvusDir)) {
    Write-Host "错误: 未找到Milvus服务目录" -ForegroundColor Red
    exit 1
}

Set-Location $milvusDir

Write-Host "正在安装依赖..." -ForegroundColor Yellow
pip install -r requirements.txt

Write-Host "正在启动Milvus服务 (端口 8081)..." -ForegroundColor Green
python main.py