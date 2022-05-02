package com.denghb.eorm.edao;

import com.denghb.eorm.EOrm;
import com.denghb.eorm.support.ETraceHolder;
import com.denghb.eorm.support.domain.ETrace;
import com.denghb.eorm.utils.EReflectUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.*;

public class EdaoProxyFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    private Class<T> clazz;
    private ApplicationContext applicationContext;

    public EdaoProxyFactoryBean(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {

                String methodName = method.getName();
                ETrace trace = new ETrace();
                trace.setLogName(clazz.getName());
                trace.setLogMethod(String.format("%s(%s.java:%d)", method.getName(), clazz.getSimpleName(), 10));
                ETraceHolder.setTrace(trace);


                EOrm db = applicationContext.getBean(EOrm.class);
                switch (methodName) {
                    case "insert":
                        db.insert(objects[0]);
                        break;
                    case "updateById":
                        db.updateById(objects[0]);
                        break;
                    case "deleteById":
                        db.deleteById(objects[0]);
                        break;
                    default:
                        String sql = (String) EReflectUtils.getStaticFieldValue(clazz, methodName);
                        System.out.println(sql);

                        // 获取方法返回值范型
                        Type genericReturnType = method.getGenericReturnType();
                        Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                        Class type = (Class)actualTypeArguments[0];
                        return db.select(type, sql, objects);
                }

                return null;
            }
        });
    }

    // 获取父类范型
    public Class getActualType(Object o,int index) {
        Type clazz = o.getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType)clazz;
        return (Class)pt.getActualTypeArguments()[index];
    }

    // 获取父接口范型
    public Class<?> getDomainType() {
        Type t = clazz.getGenericInterfaces()[0];
        Type[] p = ((ParameterizedType) t).getActualTypeArguments(); //取得所有泛型
        return (Class)p[0];
    }

    @Override
    public Class<?> getObjectType() {
        return clazz;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
