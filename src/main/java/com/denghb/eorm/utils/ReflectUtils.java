package com.denghb.eorm.utils;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;


/**
 * 反射工具类
 */
public class ReflectUtils {

    /**
     * 获得实体类的所有属性（该方法递归的获取当前类及父类中声明的字段。最终结果以list形式返回）
     */
    public static List<Field> getFields(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        List<Field> fields = new ArrayList<Field>();
        Field[] classFields = clazz.getDeclaredFields();
        fields.addAll(Arrays.asList(classFields));

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != Object.class) {
            List<Field> superClassFields = getFields(superclass);
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
     * @param beanClass
     * @return
     * @throws Exception
     */
    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) {
        if (map == null)
            return null;

        try {
            Object obj = beanClass.newInstance();

            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }

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

        try {
            Map<String, Object> map = new HashMap<String, Object>();

            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }

            return map;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't objectToMap", e);
        }
    }

}
