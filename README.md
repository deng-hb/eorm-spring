## EOrm-spring

Easy ORM with Spring framework


### Warning
+ Method `insert(obj)`,`updateById(obj)` ignore `null` value 
+ Only a single primary key is supported

### Core
```java

/**
 * 执行一条SQL
 *
 * @param sql
 * @param args
 * @return int
 */
int execute(String sql, Object... args);

/**
 * 执行一条查询
 *
 * @param clazz
 * @param sql
 * @param args
 * @param <T>
 * @return List<T>
 */
<T> List<T> select(Class<T> clazz, String sql, Object... args);

/**
 * 插入一个对象
 *
 * @param domain
 * @param <T>
 */
<T> void insert(T domain);

/**
 * 修改一个对象
 *
 * @param domain
 * @param <T>
 */
<T> void updateById(T domain);

/**
 * 删除一个对象
 *
 * @param domain
 * @param <T>
 */
<T> void deleteById(T domain);

/**
 * 删除多个主键的类型
 *
 * @param clazz
 * @param id
 * @param <T>
 */
<T> void deleteById(Class<T> clazz, Object id);

/**
 * 查询一个对象
 *
 * @param clazz
 * @param sql
 * @param args
 * @param <T>
 * @return T
 */
<T> T selectOne(Class<T> clazz, String sql, Object... args);

/**
 * 按主键查询一条记录
 *
 * @param clazz
 * @param id
 * @param <T>
 * @return T
 */
<T> T selectById(Class<T> clazz, Object id);

/**
 * 分页查询
 *
 * @param clazz
 * @param sql
 * @param pageReq
 * @param <T>
 * @return EPageRes<T>
 */
<T> EPageRes<T> selectPage(Class<T> clazz, String sql, EPageReq pageReq);
```