package com.denghb.eorm;

import java.util.List;
import java.util.Map;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/11/29 23:45
 */
public interface Core {

    /**
     * 执行一条SQL
     *
     * @param sql
     * @param args
     * @return int
     */
    int execute(String sql, Object... args);

    /**
     * @param clazz
     * @param sql
     * @param args
     * @return
     */
    <T> List<T> select(Class<T> clazz, String sql, Object... args);

}
