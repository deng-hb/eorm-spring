package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;
import io.swagger.annotations.ApiModelProperty;

/**
 * 书籍信息
 * DDL
 * 
 <pre>
CREATE TABLE `book` (
  `id` int(50) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '书名',
  `cover_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面',
  `author` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者',
  `intro` text COLLATE utf8mb4_unicode_ci COMMENT '简介',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(2) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_updated_time` (`updated_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍信息'
 <pre>
 * @author denghb
 * @generateTime Sat Jun 29 22:13:33 CST 2019
 */
@lombok.Data()
@ETable(name="book", database="xxlibrary")
public class Book implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "编号")
	@EColumn(name="id", primaryKey = true, comment="编号")
	private Integer id;
	
	@ApiModelProperty(value = "书名")
	@EColumn(name="title", comment="书名")
	private String title;
	
	@ApiModelProperty(value = "封面")
	@EColumn(name="cover_url", comment="封面")
	private String coverUrl;
	
	@ApiModelProperty(value = "作者")
	@EColumn(name="author", comment="作者")
	private String author;
	
	@ApiModelProperty(value = "简介")
	@EColumn(name="intro", comment="简介")
	private String intro;
	
	@ApiModelProperty(value = "插入时间")
	@EColumn(name="created_time", comment="插入时间")
	private java.util.Date createdTime;
	
	@ApiModelProperty(value = "更新时间")
	@EColumn(name="updated_time", comment="更新时间")
	private java.util.Date updatedTime;
	
	@ApiModelProperty(value = "逻辑删除")
	@EColumn(name="deleted", comment="逻辑删除")
	private Integer deleted;
	
	@ApiModelProperty(value = "版本号")
	@EColumn(name="version", comment="版本号")
	private Integer version;
	

}