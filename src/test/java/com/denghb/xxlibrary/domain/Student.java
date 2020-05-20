package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;
import io.swagger.annotations.ApiModelProperty;

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
) ENGINE=InnoDB AUTO_INCREMENT=100008 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息表'
 <pre>
 * @author denghb
 * @generateTime Sat Jun 29 21:15:58 CST 2019
 */
@Etable(name="student", database="xxlibrary")
public class Student implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "ID")
	@Ecolumn(name="id", primaryKey = true)
	private Long id;
	
	@ApiModelProperty(value = "姓名")
	@Ecolumn(name="name")
	private String name;
	
	@ApiModelProperty(value = "0:女，1:男")
	@Ecolumn(name="gender")
	private Integer gender;
	
	@ApiModelProperty(value = "生日")
	@Ecolumn(name="birthday")
	private java.util.Date birthday;
	
	@ApiModelProperty(value = "插入时间")
	@Ecolumn(name="created_time")
	private java.util.Date createdTime;
	
	@ApiModelProperty(value = "更新时间")
	@Ecolumn(name="updated_time")
	private java.util.Date updatedTime;
	
	@ApiModelProperty(value = "逻辑删除")
	@Ecolumn(name="deleted")
	private Integer deleted;
	
	@ApiModelProperty(value = "版本号")
	@Ecolumn(name="version")
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