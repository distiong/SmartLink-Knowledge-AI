-- 版本3新增功能数据库初始化脚本

USE knowledge_base;

-- 文档分类表
CREATE TABLE IF NOT EXISTS ai_document_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    user_id BIGINT COMMENT '创建用户ID',
    sort INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分类表';

-- 更新文档表，添加分类、标签、版本、文件路径字段
ALTER TABLE ai_document ADD COLUMN IF NOT EXISTS category_id BIGINT COMMENT '文档分类ID';
ALTER TABLE ai_document ADD COLUMN IF NOT EXISTS tags VARCHAR(500) COMMENT '文档标签(逗号分隔)';
ALTER TABLE ai_document ADD COLUMN IF NOT EXISTS version INT DEFAULT 1 COMMENT '文档版本号';
ALTER TABLE ai_document ADD COLUMN IF NOT EXISTS file_path VARCHAR(500) COMMENT '文档存储路径';
ALTER TABLE ai_document ADD INDEX IF NOT EXISTS idx_category_id (category_id);

-- 文档版本表
CREATE TABLE IF NOT EXISTS ai_document_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL COMMENT '文档ID',
    version INT NOT NULL COMMENT '版本号',
    file_path VARCHAR(500) COMMENT '版本文件路径',
    content TEXT COMMENT '版本内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本表';

-- AI会话表
CREATE TABLE IF NOT EXISTS ai_chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_name VARCHAR(200) COMMENT '会话名称',
    model_name VARCHAR(50) COMMENT '使用的模型名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话表';

-- 更新AI对话记录表，添加会话ID、模型名称、反馈字段
ALTER TABLE ai_chat_record ADD COLUMN IF NOT EXISTS session_id BIGINT COMMENT '会话ID';
ALTER TABLE ai_chat_record ADD COLUMN IF NOT EXISTS model_name VARCHAR(50) COMMENT '模型名称';
ALTER TABLE ai_chat_record ADD COLUMN IF NOT EXISTS feedback TINYINT DEFAULT 0 COMMENT '问答反馈(1-点赞 2-点踩 0-无)';
ALTER TABLE ai_chat_record ADD INDEX IF NOT EXISTS idx_session_id (session_id);

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '登录账号',
    operation VARCHAR(500) COMMENT '操作内容',
    operation_type VARCHAR(50) COMMENT '操作类型',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(500) COMMENT '配置值',
    config_desc VARCHAR(200) COMMENT '配置描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 模型配置表
CREATE TABLE IF NOT EXISTS sys_model_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(50) NOT NULL COMMENT '模型名称',
    api_key VARCHAR(500) COMMENT 'API Key',
    api_url VARCHAR(500) COMMENT 'API地址',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用(0-禁用 1-启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置表';

-- 用户权限表
CREATE TABLE IF NOT EXISTS sys_user_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    perm_code VARCHAR(100) NOT NULL COMMENT '权限标识',
    perm_name VARCHAR(100) COMMENT '权限名称',
    perm_type VARCHAR(20) COMMENT '权限类型',
    status TINYINT DEFAULT 1 COMMENT '状态(0-禁用 1-启用)',
    INDEX idx_user_id (user_id),
    INDEX idx_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限表';

-- 聊天会话表（如果不存在则创建）
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) COMMENT '会话标题',
    chat_type VARCHAR(20) DEFAULT 'chat' COMMENT '对话类型',
    message_count INT DEFAULT 0 COMMENT '消息数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 插入默认文档分类
INSERT INTO ai_document_category (category_name, parent_id, user_id, sort) VALUES
('技术文档', 0, 1, 1),
('规章制度', 0, 1, 2),
('产品手册', 0, 1, 3),
('培训资料', 0, 1, 4);

-- 插入系统默认配置
INSERT INTO sys_config (config_key, config_value, config_desc) VALUES
('temperature', '0.7', '大模型温度参数'),
('max_tokens', '2000', '大模型最大输出token数'),
('top_k', '5', '向量检索返回TopK数量'),
('similarity_threshold', '0.7', '相似度阈值'),
('chunk_size', '500', '文档分块大小'),
('chunk_overlap', '50', '文档分块重叠长度');

-- 添加分类管理菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_path, menu_component, menu_icon, menu_perm, menu_type, sort) VALUES
(2, '分类管理', '/rag/category', 'CategoryView', 'Folder', 'rag:category', 2, 3);

-- 添加分类管理按钮权限
INSERT INTO sys_menu (parent_id, menu_name, menu_path, menu_component, menu_icon, menu_perm, menu_type, sort) VALUES
(11, '分类新增', NULL, NULL, NULL, 'rag:category:add', 3, 1),
(11, '分类编辑', NULL, NULL, NULL, 'rag:category:edit', 3, 2),
(11, '分类删除', NULL, NULL, NULL, 'rag:category:delete', 3, 3);

-- 为超级管理员添加分类管理权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_perm IN ('rag:category', 'rag:category:add', 'rag:category:edit', 'rag:category:delete');

-- 为知识库运营添加分类管理权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu WHERE menu_perm IN ('rag:category', 'rag:category:add', 'rag:category:edit', 'rag:category:delete');

-- 添加操作日志菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_path, menu_component, menu_icon, menu_perm, menu_type, sort) VALUES
(5, '操作日志', '/system/log', 'LogView', 'Document', 'system:log', 2, 3);

-- 为超级管理员添加操作日志权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_perm = 'system:log';
