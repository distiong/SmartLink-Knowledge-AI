-- 权限体系数据库初始化脚本

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(200) NOT NULL COMMENT '加密密码',
    nickname VARCHAR(50) COMMENT '用户昵称',
    role_id BIGINT COMMENT '关联角色ID',
    user_type TINYINT DEFAULT 2 COMMENT '账号类型(1-超级主账号 2-子账号)',
    status TINYINT DEFAULT 1 COMMENT '账号状态(0-禁用 1-正常)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT COMMENT '创建人ID',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_path VARCHAR(200) COMMENT '前端路由地址',
    menu_component VARCHAR(200) COMMENT '前端组件路径',
    menu_icon VARCHAR(50) COMMENT '菜单图标',
    menu_perm VARCHAR(100) COMMENT '权限标识',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型(1-目录 2-菜单 3-按钮)',
    sort INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态(0-禁用 1-正常)',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单权限ID',
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- AI对话记录表
CREATE TABLE IF NOT EXISTS ai_chat_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question TEXT COMMENT '用户提问',
    answer TEXT COMMENT 'AI回答',
    chat_type VARCHAR(20) DEFAULT 'chat' COMMENT '对话类型(chat/rag)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';

-- 修改文档表，添加user_id字段
ALTER TABLE ai_document ADD COLUMN IF NOT EXISTS user_id BIGINT COMMENT '创建用户ID';
ALTER TABLE ai_document ADD INDEX IF NOT EXISTS idx_user_id (user_id);

-- 初始化角色数据
INSERT INTO sys_role (role_name, role_code, description) VALUES
('超级管理员', 'admin', '系统最高权限，拥有所有功能操作权限'),
('知识库运营', 'operator', '拥有文档上传、RAG问答、向量检索权限'),
('普通访客', 'visitor', '仅拥有AI通用对话、公开知识库问答权限');

-- 初始化菜单数据
INSERT INTO sys_menu (parent_id, menu_name, menu_path, menu_component, menu_icon, menu_perm, menu_type, sort) VALUES
-- 一级目录
(0, 'AI对话', '/chat', 'ChatView', 'ChatLineRound', 'ai:chat', 2, 1),
(0, '知识库', '/rag', 'Layout', 'Document', 'rag:manage', 1, 2),
(0, '向量检索', '/search', 'SearchView', 'Search', 'search:query', 2, 3),
(0, '知识图谱', '/graph', 'GraphView', 'Share', 'graph:manage', 2, 4),
(0, '系统管理', '/system', 'Layout', 'Setting', 'system:manage', 1, 5),

-- 知识库子菜单
(2, 'RAG问答', '/rag/chat', 'RagView', 'ChatDotRound', 'rag:chat', 2, 1),
(2, '文档管理', '/rag/document', 'DocumentView', 'Document', 'rag:document', 2, 2),

-- 系统管理子菜单
(5, '用户管理', '/system/user', 'UserView', 'User', 'system:user', 2, 1),
(5, '角色管理', '/system/role', 'RoleView', 'UserFilled', 'system:role', 2, 2),

-- 按钮权限
(7, '文档上传', NULL, NULL, NULL, 'rag:document:upload', 3, 1),
(7, '文档删除', NULL, NULL, NULL, 'rag:document:delete', 3, 2),
(8, '用户新增', NULL, NULL, NULL, 'system:user:add', 3, 1),
(8, '用户编辑', NULL, NULL, NULL, 'system:user:edit', 3, 2),
(8, '用户删除', NULL, NULL, NULL, 'system:user:delete', 3, 3),
(9, '角色新增', NULL, NULL, NULL, 'system:role:add', 3, 1),
(9, '角色编辑', NULL, NULL, NULL, 'system:role:edit', 3, 2),
(9, '角色删除', NULL, NULL, NULL, 'system:role:delete', 3, 3);

-- 初始化超级管理员用户 (密码: admin123)
INSERT INTO sys_user (username, password, nickname, role_id, user_type, status, create_user) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, 1, 1, 1);

-- 超级管理员拥有所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 知识库运营角色权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu WHERE menu_perm IN ('ai:chat', 'rag:manage', 'rag:chat', 'rag:document', 'rag:document:upload', 'search:query');

-- 普通访客角色权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu WHERE menu_perm IN ('ai:chat');
