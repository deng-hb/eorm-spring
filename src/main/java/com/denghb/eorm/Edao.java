package com.denghb.eorm;

import java.io.Serializable;

public interface Edao<T> {

    void insert(T t);

    void deleteById(Serializable id);
}
