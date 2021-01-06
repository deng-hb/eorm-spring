package com.denghb.eorm.utils;


import com.denghb.eorm.annotation.ETable;
import com.denghb.eorm.support.domain.EClassRef;
import org.springframework.core.convert.support.DefaultConversionService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 反射工具类
 */
public class EReflectUtils {

    private final static Map<String, List<Field>> FIELD_CACHE = new ConcurrentHashMap<String, List<Field>>(200);
    private final static Map<String, EClassRef> CLASS_REF_CACHE = new ConcurrentHashMap<String, EClassRef>(200);

    /**
     * 获得实体类的所有属性（该方法递归的获取当前类及父类中声明的字段。最终结果以list形式返回）
     */
    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = FIELD_CACHE.get(clazz.getName());
        if (null != fields) {
            return fields;
        }
        fields = new ArrayList<Field>();
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
            List<Field> superClassFields = getFields(superclass);
            fields.addAll(superClassFields);
        }
        FIELD_CACHE.put(clazz.getName(), fields);
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

    public static Field getField(Class<?> clazz, String fieldName) {
        List<Field> fields = getFields(clazz);
        for (Field field : fields) {
            if (fieldName.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    /**
     * 将值保存到实体对象的指定属性中
     */
    public static void setFieldValue(Field field, Object object, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, convert(value, field.getType()));
        } catch (Exception e) {
            throw new RuntimeException("Can't set value（" + value + "）to instance " + object.getClass().getName() + "." + field.getName(), e);
        }
    }

    /**
     * 设置值
     *
     * @param object    对象
     * @param fieldName 字段名
     * @param value     值
     */
    public static void setValue(Object object, String fieldName, Object value) {

        List<Field> fields = EReflectUtils.getFields(object.getClass());
        for (Field field : fields) {
            if (fieldName.equals(field.getName())) {
                EReflectUtils.setFieldValue(field, object, value);
                break;
            }
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

            List<Field> fields = getFields(obj.getClass());
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

            List<Field> fields = getFields(obj.getClass());
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
     * 基本单类型
     *
     * @param clazz
     * @return
     */
    public static boolean isSingleClass(Class<?> clazz) {
        return Boolean.class == clazz || clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || CharSequence.class.isAssignableFrom(clazz)
                || Date.class.isAssignableFrom(clazz) || Blob.class.isAssignableFrom(clazz) || Clob.class.isAssignableFrom(clazz);
    }

    /**
     * 根据实体类创建实体对象
     */
    public static Object constructorInstance(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance（" + clazz.getName() + "）  by reflect!", e);
        }
    }

    /**
     * 根据实体类创建实体对象
     */
    public static Object constructorInstance(String className) {
        if (className == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }

        try {
            return constructorInstance(Class.forName(className));
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance（" + className + "）  by reflect!", e);
        }
    }

    /**
     * 有参构造创建对象
     */
    public static Object constructorInstance(Class<?> clazz, Class<?> type, Object value) {
        return constructorInstance(clazz, new Class[]{type}, new Object[]{value});
    }

    public static Object constructorInstance(Class<?> clazz, Class<?>[] types, Object[] values) {
        if (clazz == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }
        try {
            return clazz.getConstructor(types).newInstance(values);
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance（" + clazz.getName() + "）  by reflect!", e);
        }
    }


    /**
     * 驼峰格式转换为下划线格式
     */
    public static String humpToUnderline(String name) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (i > 0 && Character.isUpperCase(ch)) {// 首字母是大写不需要添加下划线
                builder.append('_');
            }
            builder.append(ch);
        }

        int startIndex = 0;
        if (builder.charAt(0) == '_') {//如果以下划线开头则忽略第一个下划线
            startIndex = 1;
        }
        return builder.substring(startIndex).toLowerCase();
    }


    /**
     * 下划线格式转换为驼峰格式
     */
    public static String underlineToHump(String name, boolean firstCharToUpper) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (i == 0 && firstCharToUpper) {
                builder.append(Character.toUpperCase(ch));
            } else {
                if (i > 0 && ch == '_') {// 首字母是大写不需要添加下划线
                    i++;
                    ch = name.charAt(i);
                    builder.append(Character.toUpperCase(ch));
                } else {
                    builder.append(ch);
                }
            }
        }
        return builder.toString();
    }

    public static EClassRef getClassRef(Class<?> clazz) {
        String className = clazz.getName();
        EClassRef ref = CLASS_REF_CACHE.get(className);
        if (null != ref) {
            return ref;
        }
        ref = new EClassRef();
        CLASS_REF_CACHE.put(className, ref);

        List<Field> fields = EReflectUtils.getFields(clazz);
        for (Field field : fields) {
            String fieldName = convertName(field.getName());
            if (!EReflectUtils.isSingleClass(field.getType())) {
                EClassRef.ESubClassRef subClassRef = new EClassRef.ESubClassRef();
                subClassRef.setField(field);

                Class<?> clazz2 = field.getType();
                // 放置
                ETable etable = clazz2.getAnnotation(ETable.class);
                if (null != etable) {
                    ref.getTableMap().put(etable.name(), subClassRef);
                }
                ref.getTableMap().put(fieldName, subClassRef);
                //
                List<Field> field2s = EReflectUtils.getFields(clazz2);
                for (Field field2 : field2s) {
                    String fieldName2 = convertName(field2.getName());
                    subClassRef.getColumnMap().put(fieldName2, field2);
                }
            } else {
                ref.getColumnMap().put(fieldName, field);
            }
        }
        return ref;
    }

    /**
     * 优化 驼峰下划线命名格式，需要避免名称冲突
     * 名称统一转换为小写且无下划线格式
     * nick_name -> nickname
     * nickName -> nickname
     */
    public static String convertName(String name) {
        return name.toLowerCase().replaceAll("_", "");
    }

    /**
     * 类型转换
     */
    public static <T> T convert(Object source, Class<T> targetType) {
        return DefaultConversionService.getSharedInstance().convert(source, targetType);
    }
}
