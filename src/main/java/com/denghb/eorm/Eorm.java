package com.denghb.eorm;

import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;

import java.util.List;

/**
 * Easy ROM
 *
 * @author denghb
 */
public interface Eorm {

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
     * @return List
     */
    <T> List<T> select(Class<T> clazz, String sql, Object... args);

    /**
     * 插入一个对象
     *
     * @param domain
     * @param <T>
     * @return int
     */
    <T> boolean insert(T domain);

    /**
     * 修改一个对象
     *
     * @param domain
     * @param <T>
     * @return int
     */
    <T> int update(T domain);

    /**
     * 删除一个对象
     *
     * @param domain
     * @param <T>
     * @return int
     */
    <T> int delete(T domain);


    /**
     * 删除多个主键的对象
     *
     * @param clazz
     * @param ids
     * @param <T>
     * @return int
     */
    <T> int delete(Class<T> clazz, Object... ids);

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
     * @param args
     * @param <T>
     * @return T
     */
    <T> T selectByPrimaryKey(Class<T> clazz, Object... args);

    /**
     * 批量插入
     *
     * @param list
     * @param <T>
     * @return int
     */
    <T> int batchInsert(List<T> list);

    /**
     * 分页查询
     *
     * @param clazz
     * @param sql
     * @param paging
     * @param <T>
     * @return PagingResult
     */
    <T> PagingResult<T> page(Class<T> clazz, StringBuffer sql, Paging paging);

}
