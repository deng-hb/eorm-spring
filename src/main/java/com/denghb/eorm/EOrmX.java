package com.denghb.eorm;

import com.denghb.eorm.support.ESQLWhere;

import java.util.List;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/12/25 20:47
 */
public interface EOrmX extends EOrm {

    <T> int update(T t, ESQLWhere<T> segment);

    <T> int delete(ESQLWhere<T> segment);

    <T> List<T> select(ESQLWhere<T> segment);

    <T> T selectOne(ESQLWhere<T> segment);
}
