# AI智能知识库问答系统 - 启动说明

## 一键启动

```powershell
.\start.ps1
```

## 一键关闭

```powershell
.\stop.ps1
```

## 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 | http://localhost:8080 |
| Milvus | http://localhost:8081 |

## 登录账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 超级管理员 | admin | admin123 |

## 手动启动

### 1. 启动Milvus服务
```powershell
cd milvus-service
python main.py
```

### 2. 启动后端服务
```powershell
$env:JAVA_HOME = "D:\softtware\java17"
cd backend
mvn spring-boot:run
```

### 3. 启动前端服务
```powershell
cd frontend
npm run dev
```

## 手动关闭

在对应的PowerShell窗口中按 `Ctrl + C` 即可停止服务。

## 常见问题

### 1. 端口被占用
运行 `stop.ps1` 清理端口，或手动关闭占用端口的进程。

### 2. 数据库连接失败
确保MySQL服务已启动，用户名: root，密码: 123456

### 3. Java版本错误
确保 `D:\softtware\java17` 目录下安装了JDK 17
