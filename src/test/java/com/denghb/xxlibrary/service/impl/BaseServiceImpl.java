package com.denghb.xxlibrary.service.impl;

import com.denghb.eorm.EOrm;
import com.denghb.eorm.support.EOrmTableParser;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.ReflectUtils;
import com.denghb.xxlibrary.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author denghb 2019-06-24 23:59
 */
public class BaseServiceImpl<T> implements BaseService<T> {

    @Autowired
    private EOrm db;

    private Class getTypeClass() {
        // get domain class
        Type genType = getClass().getGenericSuperclass();
        if (genType instanceof ParameterizedType) {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            return (Class) params[0];
        }
        throw new RuntimeException("Type error");
    }

    @Override
    public void save(T object) {
        Table table = EOrmTableParser.load(getTypeClass());
        Field field = table.getPrimaryKeyColumn().getField();

        Object idValue = ReflectUtils.getFieldValue(field, object);
        if (null == idValue) {
            db.insert(object);
        } else {
            db.updateById(object);
        }

    }

    @Override
    public void del(Object id) {
        Table table = EOrmTableParser.load(getTypeClass());
        String sql = "update " + table.getName() + " set deleted = 1 where id = ? and deleted = 0";
        int res = db.execute(sql, id);
        if (1 != res) {
            throw new RuntimeException("change zero");
        }
    }
}
