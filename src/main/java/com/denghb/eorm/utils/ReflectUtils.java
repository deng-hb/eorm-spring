package com.denghb.eorm.utils;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 反射工具类
 */
public class ReflectUtils {

    private static Map<String, Set<Field>> FIELD_CACHE = new ConcurrentHashMap<String, Set<Field>>();

    /**
     * 获得实体类的所有属性（该方法递归的获取当前类及父类中声明的字段。最终结果以list形式返回）
     */
    public static Set<Field> getFields(Class<?> clazz) {
        Set<Field> fields = FIELD_CACHE.get(clazz.getName());
        if (null != fields) {
            return fields;
        }
        fields = new HashSet<>();
        Field[] classFields = clazz.getDeclaredFields();
        for (Field field : classFields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            fields.add(field);
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != Object.class) {
            Set<Field> superClassFields = getFields(superclass);
            fields.addAll(superClassFields);
        }
        return fields;
    }

    /**
     * 通过属性对象和实体对象获取字段的值
     */
    public static Object getFieldValue(Field field, Object object) {
        if (field == null || object == null) {
            return null;
        }

        try {
            field.setAccessible(true);
            return field.get(object);// 获取字段的值
        } catch (Exception e) {
            throw new RuntimeException("Can't get field (" + field.getName() + ") value from object " + object, e);
        }
    }

    /**
     * 将值保存到实体对象的指定属性中
     */
    public static void setFieldValue(Field field, Object object, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Can't set value（" + value + "）to instance " + object.getClass().getName() + "." + field.getName(), e);
        }
    }

    /**
     * Map -> Object
     *
     * @param map
     * @param clazz
     * @return
     */
    public static Object mapToObject(Map<String, Object> map, Class<?> clazz) {
        if (map == null)
            return null;

        try {
            Object obj = clazz.newInstance();

            Set<Field> fields = getFields(obj.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(obj, map.get(field.getName()));
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Can't mapToObject", e);
        }
    }

    /**
     * Object -> Map
     *
     * @param obj
     * @return
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return (Map) obj;
        }

        try {
            Map<String, Object> map = new HashMap<String, Object>();

            Set<Field> fields = getFields(obj.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }

            return map;
        } catch (Exception e) {
            throw new RuntimeException("Can't objectToMap", e);
        }
    }

    /**
     * 单类型
     * TODO
     *
     * @param clazz
     * @return
     */
    public static boolean isSingleClass(Class clazz) {
        return clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || CharSequence.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz);
    }
}
