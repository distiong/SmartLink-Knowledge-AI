-- 创建数据库
CREATE DATABASE IF NOT EXISTS knowledge_base DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE knowledge_base;

-- 创建文档表
CREATE TABLE IF NOT EXISTS ai_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL COMMENT '文档名称',
    file_type VARCHAR(50) NOT NULL COMMENT '文档类型',
    content TEXT COMMENT '文档纯文本内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档信息表';

-- 插入示例数据（可选）
-- INSERT INTO ai_document (file_name, file_type, content) VALUES 
-- ('示例文档.txt', 'txt', '这是一份示例文档的内容，用于测试RAG知识库问答系统。');