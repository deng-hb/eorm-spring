
-- DROP TABLE IF EXISTS student;
CREATE TABLE `student` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(100) COMMENT '姓名',
  `gender` int(11) COMMENT '0:女，1:男',
  `age` int(11) COMMENT '年龄',
  `birthday` date COMMENT '生日',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_updated_time` (`updated_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息';

-- DROP TABLE IF EXISTS book;
CREATE TABLE `book` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(200) COMMENT '书名',
  `cover_url` varchar(200) COMMENT '封面',
  `author` varchar(100) COMMENT '作者',
  `intro` varchar(300) COMMENT '简介',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_updated_time` (`updated_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍信息';


-- DROP TABLE IF EXISTS read_record;
CREATE TABLE `read_record` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `student_id` int(11) COMMENT '学生ID',
  `book_id` int(11) COMMENT '书籍ID',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_updated_time` (`updated_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅读记录';



