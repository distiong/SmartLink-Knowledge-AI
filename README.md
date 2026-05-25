# AI智能知识库问答系统

基于 SpringBoot3 + Vue3 的**企业级权限管控型AI智能问答系统**，采用第三方大模型API实现AI核心能力。

## ✨ 功能特性

### 🔐 权限体系
- RBAC权限模型，支持主账号/子账号分级管理
- 菜单权限、按钮权限、接口权限、数据权限四级管控
- 动态路由、动态菜单、按钮级权限控制
- JWT令牌认证，支持Redis缓存

### 🤖 AI对话
- 通用大模型对话（支持智谱AI、OpenAI、通义千问等）
- 多轮对话上下文记忆
- 对话历史记录管理
- 消息长度限制（业内标准4000字符）

### 📚 RAG知识库
- 支持PDF、Word、Excel、TXT文档上传
- 文档自动解析、分块、向量化
- 基于Milvus的语义相似度检索
- 带溯源引用的智能问答

### 🔍 向量检索
- 独立的向量语义检索演示
- 支持TopN相似文档返回
- 余弦相似度计算

### 🕸️ 知识图谱
- 实体关系创建与管理
- ECharts可视化图谱展示
- 图谱关系查询

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 核心框架 |
| Spring Security | 6.x | 安全框架 |
| Spring Data JPA | - | ORM框架 |
| JWT | 0.12.x | 令牌认证 |
| MySQL | 8.0 | 关系数据库 |
| Redis | 7.x | 缓存（可选） |
| Milvus Lite | - | 向量数据库 |
| Neo4j | 5.x | 图数据库（可选） |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Vite | 5.x | 构建工具 |
| Element Plus | 2.6.x | UI组件库 |
| ECharts | 5.5.x | 图表可视化 |
| Axios | 1.6.x | HTTP客户端 |
| Vue Router | 4.3.x | 路由管理 |
| Pinia | 2.1.x | 状态管理 |

## 📁 项目结构

```
AIdemo/
├── backend/                    # 后端SpringBoot项目
│   ├── src/
│   │   └── main/
│   │       ├── java/com/ai/knowledge/
│   │       │   ├── config/         # 配置类
│   │       │   ├── controller/     # 控制器
│   │       │   ├── dto/            # 数据传输对象
│   │       │   ├── entity/         # 实体类
│   │       │   ├── repository/     # 数据访问层
│   │       │   ├── security/       # 安全相关
│   │       │   ├── service/        # 业务逻辑层
│   │       │   └── util/           # 工具类
│   │       └── resources/
│   │           └── application.yml # 配置文件
│   └── pom.xml
├── frontend/                   # 前端Vue3项目
│   ├── src/
│   │   ├── components/         # 组件
│   │   ├── router/             # 路由
│   │   ├── stores/             # 状态管理
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面
│   └── package.json
├── milvus-service/             # Milvus向量服务（Python）
│   ├── main.py
│   └── requirements.txt
├── init-permission.sql         # 权限数据库初始化脚本
├── .env.example                # 环境变量模板
├── .gitignore
├── LICENSE
└── README.md
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Python 3.10+（用于Milvus服务）
- Maven 3.6+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/ai-knowledge-base.git
cd ai-knowledge-base
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填入你的配置
# 主要配置：
# - DB_USERNAME: MySQL用户名
# - DB_PASSWORD: MySQL密码
# - AI_API_KEY: AI大模型API密钥
```

### 3. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE knowledge_base DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 初始化权限表和数据
mysql -u root -p knowledge_base < init-permission.sql
```

### 4. 启动Milvus向量服务

```bash
cd milvus-service
pip install -r requirements.txt
python main.py
```

### 5. 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 7. 访问系统

- 前端地址: http://localhost:3000
- 后端API: http://localhost:8080
- Milvus服务: http://localhost:8081

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |

## 📝 API文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| GET | /api/auth/info | 获取用户信息 |
| GET | /api/auth/menu | 获取用户菜单 |
| GET | /api/auth/permissions | 获取用户权限 |

### AI对话接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/ai/chat | 发送消息 |
| GET | /api/ai/sessions | 获取会话列表 |
| GET | /api/ai/sessions/{id}/messages | 获取会话消息 |
| POST | /api/ai/sessions | 创建新会话 |
| DELETE | /api/ai/sessions/{id} | 删除会话 |

### RAG知识库接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/rag/upload | 上传文档 |
| GET | /api/rag/chat | RAG问答 |
| GET | /api/rag/search | 向量检索 |

### 知识图谱接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/graph/create | 创建关系 |
| GET | /api/graph/query | 查询关系 |

### 用户管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/user/list | 用户列表 |
| POST | /api/user | 创建用户 |
| PUT | /api/user/{id} | 更新用户 |
| DELETE | /api/user/{id} | 删除用户 |
| POST | /api/user/{id}/permissions | 配置权限 |

## 🔧 配置说明

### AI大模型配置

支持兼容OpenAI API格式的大模型服务：

```yaml
ai:
  api-key: your_api_key
  base-url: https://api.openai.com  # 或其他兼容API地址
  model: gpt-3.5-turbo
```

支持的AI服务：
- 智谱AI: https://open.bigmodel.cn/api/paas/v4
- OpenAI: https://api.openai.com
- 通义千问: https://dashscope.aliyuncs.com
- 文心一言: https://aip.baidubce.com

### Neo4j配置（可选）

如需使用知识图谱功能，安装Neo4j并配置：

```yaml
neo4j:
  enabled: true
  uri: bolt://localhost:7687
  username: neo4j
  password: your_password
```

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 📄 许可证

本项目基于 MIT 许可证开源 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Milvus](https://milvus.io/)
- [ECharts](https://echarts.apache.org/)

## 📧 联系方式

如有问题，请提交Issue或联系项目维护者。
