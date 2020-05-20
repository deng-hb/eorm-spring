package com.denghb.xxlibrary.base;


import com.denghb.eorm.page.EPageReq;
import com.denghb.eorm.page.EPageRes;

/**
 * custom service example
 *
 * @author denghb 2019-05-30 23:50
 */
public interface BaseService<T> {

    public void insert(T object);

    public void save(T object);

    public void updateById(T object);

    public void deleteById(Object id);

    public T selectById(Object id);

    public EPageRes<T> selectPage(EPageReq req);
}