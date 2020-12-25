package com.denghb.eorm;

import com.denghb.eorm.support.ESQLSegment;

import java.util.List;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/12/25 20:47
 */
public interface EOrmX extends EOrm {

    <T> int update(T t, ESQLSegment<T> segment);

    <T> int delete(T t, ESQLSegment<T> segment);

    <T> List<T> select(ESQLSegment<T> segment);

    <T> T selectOne(ESQLSegment<T> segment);
}
