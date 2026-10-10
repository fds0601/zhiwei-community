-- ============================================
-- 升级SQL：新增标签、收藏、AI工具模块
-- 在MySQL中执行：USE community; source sql/upgrade_v2.sql
-- ============================================

-- 1. article表加tags字段
ALTER TABLE article ADD COLUMN tags VARCHAR(200) COMMENT '标签，逗号分隔：RAG,Agent,LLM' AFTER category;

-- 2. 收藏表
CREATE TABLE IF NOT EXISTS article_favorite (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id  BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_article_user (article_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章收藏表';

-- 3. AI工具表
CREATE TABLE IF NOT EXISTS tool (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '工具名称',
    description VARCHAR(500) COMMENT '工具描述',
    url         VARCHAR(255) NOT NULL COMMENT '工具链接',
    category    VARCHAR(50) COMMENT '分类：LLM/RAG/Agent/绘图/办公',
    cover_url   VARCHAR(255) COMMENT '截图URL',
    user_id     BIGINT NOT NULL COMMENT '提交人',
    like_count  INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    status      TINYINT NOT NULL DEFAULT 1 COMMENT '1-上架 0-下架',
    deleted     TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI工具表';
