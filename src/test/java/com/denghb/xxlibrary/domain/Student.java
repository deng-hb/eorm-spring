package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;


/**
 * 学生信息表
 * DDL
 * 
 <pre>
CREATE TABLE `student` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '姓名',
  `gender` tinyint(2) NOT NULL COMMENT '0:女，1:男',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_updated_time` (`updated_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=100001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息表'
 <pre>
 * @author denghb
 * @generateTime Tue May 28 00:56:26 CST 2019
 */
@ETable(name="student", database="xxlibrary")
public class Student implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	/** ID */
	@EColumn(name="id", primaryKey = true)
	private Long id;
	
	/** 姓名 */
	@EColumn(name="name")
	private String name;
	
	/** 0:女，1:男 */
	@EColumn(name="gender")
	private Integer gender;
	
	/** 生日 */
	@EColumn(name="birthday")
	private java.util.Date birthday;
	
	/** 插入时间 */
	@EColumn(name="created_time")
	private java.util.Date createdTime;
	
	/** 更新时间 */
	@EColumn(name="updated_time")
	private java.util.Date updatedTime;
	
	/** 逻辑删除 */
	@EColumn(name="deleted")
	private Integer deleted;
	
	/** 版本号 */
	@EColumn(name="version")
	private Integer version;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
	}

	public java.util.Date getBirthday() {
		return birthday;
	}

	public void setBirthday(java.util.Date birthday) {
		this.birthday = birthday;
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
		str.append("\"name\":\"");
		str.append(name);
		str.append("\"");
		str.append(",");
		str.append("\"gender\":\"");
		str.append(gender);
		str.append("\"");
		str.append(",");
		str.append("\"birthday\":\"");
		str.append(birthday);
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