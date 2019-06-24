package com.denghb.xxlibrary.service;


/**
 * custom service example
 *
 * @author  denghb
 * @date  2019-05-30 23:50
 */
public interface BaseService<T> {

    public void save(T object);

    public void del(Object id);

}
