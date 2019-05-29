package com.denghb.eorm;

import com.denghb.eorm.page.EPageRes;

import java.util.List;

/**
 * Easy ROM
 *
 * @author denghb
 */
public interface EOrm {

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
     * 删除多个主键的对象
     *
     * @param clazz
     * @param ids
     * @param <T>
     */
    <T> void deleteById(Class<T> clazz, Object... ids);

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
     * @param ids
     * @param <T>
     * @return T
     */
    <T> T selectById(Class<T> clazz, Object... ids);

    /**
     * 分页查询
     *
     * @param clazz
     * @param sql
     * @param pageReq
     * @param <T>
     * @return EPageRes
     */
    <T> EPageRes<T> selectPage(Class<T> clazz, String sql, Object pageReq);

}
