## eorm-spring

这是一个基于Spring `JdbcTemplate` 和 `NamedParameterJdbcTemplate` 的ORM框架。


### Maven依赖
```xml
<dependency>
    <groupId>com.denghb</groupId>
    <artifactId>eorm-spring</artifactId>
    <version>1.1.1</version>
</dependency>
```

### 配置
```java
@Autowired
private JdbcTemplate jdbcTemplate;

@Bean
public Eorm eorm() {
    return new EormMySQLImpl(jdbcTemplate);
}
```

### 基本操作

```java
@Autowired
private Eorm db;

伪代码{
    // 执行一条SQL
    int r = db.execute(String sql, Object... args);
    
    // 执行一条SQL查询
    List<T> list = db.select(Class<T> clazz, String sql, Object... args);
    
    // 插入一个对象
    db.insert(T domain);
    
    // 修改一个对象
    db.update(T domain);
    
    // 删除一个对象
    db.delete(T domain);
    
    
    // 按主键删除
    db.delete(Class<T> clazz, Object... ids);
    
    // 查询返回一个对象
    T db.selectOne(Class<T> clazz, String sql, Object... args);
    
    // 按主键查询一条记录
    T db.selectByPrimaryKey(Class<T> clazz, Object... args);
}
```


### 模版SQL
```
SQL 片段1
#if (表达式)
  SQL 片段2
#elseIf (表达式)
  SQL 片段3
#else
  SQL 片段4
#end
SQL 片段5
```

#### 模版SQL示例
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


