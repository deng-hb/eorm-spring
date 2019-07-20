## eorm-spring

这是一个基于Spring `JdbcTemplate` 和 `NamedParameterJdbcTemplate` 的ORM框架。


### Maven
```xml
<dependency>
    <groupId>com.denghb</groupId>
    <artifactId>eorm-spring</artifactId>
    <version>1.0.9</version>
</dependency>
```

### Example
```java
String sql = ""/*{
    select count(*) from tb_user u where u.deleted = 0
    #if (null != #nickName)
        and u.nick_name like concat('%', :nickName, '%')
    #elseIf (null != #openId)
        and u.openId = :openId
    #end 
}*/;
Integer count = db.selectOne(Integer.class, sql, new HashMap<String, String>() {{
    put("nickName", "张三");
}});

System.out.println(count);
```


### Java Entity 生成工具
[eorm-mysql-support.jar](./eorm-mysql-support.jar)


配合`IntelliJ IDEA` `Eorm` 插件食用更佳。


