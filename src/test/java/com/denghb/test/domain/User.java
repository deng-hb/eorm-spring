package com.denghb.test.domain;

import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;


/**
 * 用户表
 * DDL
 * 
 <pre>
CREATE TABLE `tb_user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `open_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'OPEN ID',
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '手机号',
  `nick_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `gender` int(11) DEFAULT NULL COMMENT '性别 0：未知、1：男、2：女',
  `city` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `province` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
  `country` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `union_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'UNION ID',
  `avatar_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `session_key` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'session_key',
  `operator` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_updated_time` (`updated_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表'
 <pre> 
 * @author denghb
 * @generateTime 2019-07-14 01:40
 */
@Etable(name="tb_user", database="test")
public class User implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	/** ID */
	@Ecolumn(name = "id", primaryKey = true)
	private Integer id;
	
	/** OPEN ID */
	@Ecolumn(name = "open_id")
	private String openId;
	
	/** 手机号 */
	@Ecolumn(name = "mobile")
	private String mobile;
	
	/** 昵称 */
	@Ecolumn(name = "nick_name")
	private String nickName;
	
	/** 性别 0：未知、1：男、2：女 */
	@Ecolumn(name = "gender")
	private Integer gender;
	
	/** 城市 */
	@Ecolumn(name = "city")
	private String city;
	
	/** 省份 */
	@Ecolumn(name = "province")
	private String province;
	
	/** 国家 */
	@Ecolumn(name = "country")
	private String country;
	
	/** UNION ID */
	@Ecolumn(name = "union_id")
	private String unionId;
	
	/** 头像 */
	@Ecolumn(name = "avatar_url")
	private String avatarUrl;
	
	/** session_key */
	@Ecolumn(name = "session_key")
	private String sessionKey;
	
	/** 操作人 */
	@Ecolumn(name = "operator")
	private String operator;
	
	/** 插入时间 */
	@Ecolumn(name = "created_time")
	private java.util.Date createdTime;
	
	/** 更新时间 */
	@Ecolumn(name = "updated_time")
	private java.util.Date updatedTime;
	
	/** 逻辑删除 */
	@Ecolumn(name = "deleted")
	private Integer deleted;
	
	/** 版本号 */
	@Ecolumn(name = "version")
	private Integer version;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Integer getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
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
		StringBuilder str = new StringBuilder("User {");
		str.append("\"id\":\"");
		str.append(id);
		str.append("\"");
		str.append(",");
		str.append("\"openId\":\"");
		str.append(openId);
		str.append("\"");
		str.append(",");
		str.append("\"mobile\":\"");
		str.append(mobile);
		str.append("\"");
		str.append(",");
		str.append("\"nickName\":\"");
		str.append(nickName);
		str.append("\"");
		str.append(",");
		str.append("\"gender\":\"");
		str.append(gender);
		str.append("\"");
		str.append(",");
		str.append("\"city\":\"");
		str.append(city);
		str.append("\"");
		str.append(",");
		str.append("\"province\":\"");
		str.append(province);
		str.append("\"");
		str.append(",");
		str.append("\"country\":\"");
		str.append(country);
		str.append("\"");
		str.append(",");
		str.append("\"unionId\":\"");
		str.append(unionId);
		str.append("\"");
		str.append(",");
		str.append("\"avatarUrl\":\"");
		str.append(avatarUrl);
		str.append("\"");
		str.append(",");
		str.append("\"sessionKey\":\"");
		str.append(sessionKey);
		str.append("\"");
		str.append(",");
		str.append("\"operator\":\"");
		str.append(operator);
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