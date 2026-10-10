-- 知微社区 v3 升级脚本：关注/通知/评论嵌套/评论点赞/举报/用户简介
USE community;

-- 关注表
CREATE TABLE IF NOT EXISTS `follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '关注者',
  `follow_id` BIGINT NOT NULL COMMENT '被关注者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`user_id`,`follow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消息通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '接收者',
  `from_user_id` BIGINT NOT NULL,
  `type` VARCHAR(20) COMMENT 'LIKE/COMMENT/FOLLOW',
  `article_id` BIGINT DEFAULT NULL,
  `content` VARCHAR(255),
  `is_read` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论点赞表
CREATE TABLE IF NOT EXISTS `comment_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `comment_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk` (`comment_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 举报表
CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_type` VARCHAR(20) COMMENT 'ARTICLE/COMMENT/TOOL',
  `target_id` BIGINT NOT NULL,
  `reason` VARCHAR(255),
  `reporter_id` BIGINT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- comment表加parent_id和reply_to_user_id
ALTER TABLE `comment` ADD COLUMN `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID，0=一级评论' AFTER `content`;
ALTER TABLE `comment` ADD COLUMN `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复给谁';
ALTER TABLE `comment` ADD COLUMN `like_count` INT DEFAULT 0;

-- user表加简介字段
ALTER TABLE `user` ADD COLUMN `bio` VARCHAR(255) DEFAULT NULL COMMENT '个人简介' AFTER `nickname`;

-- article表加cover_url如果没有
ALTER TABLE `article` ADD COLUMN `cover_url` VARCHAR(500) DEFAULT NULL AFTER `tags`;
