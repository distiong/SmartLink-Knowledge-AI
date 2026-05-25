# 快速启动指南

## 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Maven 3.6+

## 快速启动步骤

### 1. 初始化数据库

```bash
mysql -u root -p123456 < init-database.sql
```

### 2. 配置AI API

复制 `.env.example` 为 `.env`，填入你的AI API Key：

```bash
cp .env.example .env
# 编辑 .env 文件，填入正确的API Key
```

支持的AI服务：
- OpenAI: https://api.openai.com
- 通义千问: https://dashscope.aliyuncs.com
- 文心一言: https://aip.baidubce.com

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 5. 访问系统

打开浏览器访问: http://localhost:3000

## 可选服务

### Milvus向量数据库

用于RAG知识库问答和向量语义检索功能。

```bash
# 安装
install-milvus.bat

# 启动
start-milvus.bat
```

### Neo4j图数据库

用于知识图谱功能。

```bash
# 安装
install-neo4j.bat

# 启动
start-neo4j.bat
```

## 功能说明

1. **通用AI对话** - 直接与AI大模型对话
2. **RAG知识库问答** - 上传文档后基于文档内容问答
3. **向量语义检索** - 搜索相似文档片段
4. **知识图谱** - 创建和查询实体关系
5. **文档管理** - 管理已上传的文档

## 常见问题

### Q: 启动报错连接数据库失败？

A: 检查MySQL服务是否启动，用户名密码是否正确。

### Q: RAG功能不可用？

A: 需要先启动Milvus服务，并上传文档到知识库。

### Q: 知识图谱功能不可用？

A: 需要先启动Neo4j服务。

### Q: AI对话没有响应？

A: 检查AI API Key是否配置正确，网络是否可访问API服务。