package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;
import io.swagger.annotations.ApiModelProperty;

/**
 * 阅读记录
 * DDL
 * 
 <pre>
CREATE TABLE `read_record` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `student_id` int(11) DEFAULT NULL COMMENT '学生ID',
  `book_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '书籍ID',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(2) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_updated_time` (`updated_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅读记录'
 <pre>
 * @author denghb
 * @generateTime Sat Jun 29 21:15:58 CST 2019
 */
@ETable(name="read_record", database="xxlibrary")
public class ReadRecord implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "ID")
	@EColumn(name="id", primaryKey = true)
	private Integer id;
	
	@ApiModelProperty(value = "学生ID")
	@EColumn(name="student_id")
	private Integer studentId;
	
	@ApiModelProperty(value = "书籍ID")
	@EColumn(name="book_id")
	private String bookId;
	
	@ApiModelProperty(value = "插入时间")
	@EColumn(name="created_time")
	private java.util.Date createdTime;
	
	@ApiModelProperty(value = "更新时间")
	@EColumn(name="updated_time")
	private java.util.Date updatedTime;
	
	@ApiModelProperty(value = "逻辑删除")
	@EColumn(name="deleted")
	private Integer deleted;
	
	@ApiModelProperty(value = "版本号")
	@EColumn(name="version")
	private Integer version;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public java.util.Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(java.util.Date createdTime) {
		this.createdTime = createdTime;
	}

	public java.util.Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(java.util.Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	public Integer getDeleted() {
		return deleted;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{");
		str.append("\"id\":\"");
		str.append(id);
		str.append("\"");
		str.append(",");
		str.append("\"studentId\":\"");
		str.append(studentId);
		str.append("\"");
		str.append(",");
		str.append("\"bookId\":\"");
		str.append(bookId);
		str.append("\"");
		str.append(",");
		str.append("\"createdTime\":\"");
		str.append(createdTime);
		str.append("\"");
		str.append(",");
		str.append("\"updatedTime\":\"");
		str.append(updatedTime);
		str.append("\"");
		str.append(",");
		str.append("\"deleted\":\"");
		str.append(deleted);
		str.append("\"");
		str.append(",");
		str.append("\"version\":\"");
		str.append(version);
		str.append("\"");
		
		return str.append("}").toString();
	}
}