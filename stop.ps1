Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI智能知识库问答系统 - 一键关闭" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 通过端口关闭进程
function Stop-ProcessByPort {
    param([int]$Port, [string]$Name)
    
    Write-Host "关闭 $Name (端口: $Port)..." -ForegroundColor Yellow
    try {
        $connection = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
        if ($connection) {
            $processId = $connection.OwningProcess
            $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
            if ($process) {
                Stop-Process -Id $processId -Force
                Write-Host "$Name 已关闭 (PID: $processId)" -ForegroundColor Green
            }
        } else {
            Write-Host "$Name 未运行" -ForegroundColor Gray
        }
    } catch {
        Write-Host "关闭 $Name 时出错: $_" -ForegroundColor Red
    }
}

# 关闭前端服务 (端口 5173)
Stop-ProcessByPort -Port 5173 -Name "前端服务"

# 关闭后端服务 (端口 8080)
Stop-ProcessByPort -Port 8080 -Name "后端服务"

# 关闭Milvus服务 (端口 8081)
Stop-ProcessByPort -Port 8081 -Name "Milvus服务"

# 额外关闭所有java和python进程（可选）
Write-Host ""
Write-Host "清理相关进程..." -ForegroundColor Yellow

Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*knowledge*"
} | Stop-Process -Force -ErrorAction SilentlyContinue

Get-Process -Name "python" -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*main.py*"
} | Stop-Process -Force -ErrorAction SilentlyContinue

# 关闭所有相关的PowerShell窗口（除了当前窗口）
$currentPid = $PID
Get-Process -Name "powershell" -ErrorAction SilentlyContinue | Where-Object {
    $_.Id -ne $currentPid -and (
        $_.MainWindowTitle -like "*mvn*" -or 
        $_.MainWindowTitle -like "*vite*" -or 
        $_.MainWindowTitle -like "*python*" -or
        $_.MainWindowTitle -like "*npm*"
    )
} | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "系统已关闭!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
