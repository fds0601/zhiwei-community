-- upgrade_v4.sql: 完善社区功能
USE community;

-- 1. 草稿箱表
CREATE TABLE IF NOT EXISTS draft (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) DEFAULT '',
  summary VARCHAR(500) DEFAULT '',
  content MEDIUMTEXT,
  category VARCHAR(50) DEFAULT '',
  tags VARCHAR(200) DEFAULT '',
  cover_url VARCHAR(500) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 文章表情回应表(点赞/哇/棒)
CREATE TABLE IF NOT EXISTS article_reaction (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  article_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL COMMENT 'LIKE/AWESOME/GOOD',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_article_user_type (article_id, user_id, type)
);

-- 3. 用户屏蔽表(屏蔽作者/标签)
CREATE TABLE IF NOT EXISTS user_block (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '谁屏蔽的',
  block_type VARCHAR(20) NOT NULL COMMENT 'USER/TAG',
  block_value VARCHAR(200) NOT NULL COMMENT '被屏蔽的用户ID或标签名',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_type_value (user_id, block_type, block_value)
);

-- 4. 举报表(补充字段)
ALTER TABLE report ADD COLUMN IF NOT EXISTS reason VARCHAR(500) DEFAULT '';
ALTER TABLE report ADD COLUMN IF NOT EXISTS article_id BIGINT DEFAULT NULL;
ALTER TABLE report ADD COLUMN IF NOT EXISTS comment_id BIGINT DEFAULT NULL;
ALTER TABLE report ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/RESOLVED/REJECTED';

-- 5. 用户表补充字段
ALTER TABLE user ADD COLUMN IF NOT EXISTS banner VARCHAR(500) DEFAULT '';
ALTER TABLE user ADD COLUMN IF NOT EXISTS phone VARCHAR(20) DEFAULT '';

-- 6. 搜索热词表
CREATE TABLE IF NOT EXISTS search_hotword (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  keyword VARCHAR(100) NOT NULL,
  count INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_keyword (keyword)
);

-- 7. 文章浏览记录去重表(Redis也可,这里用MySQL记录最近浏览)
-- 用Redis去重即可,不需要建表

-- 8. 通知表补充category字段(按类型分类已在type字段体现)
-- 已有type字段,不需要加
