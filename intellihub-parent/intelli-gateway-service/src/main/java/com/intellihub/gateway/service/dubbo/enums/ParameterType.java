package com.intellihub.gateway.service.dubbo.enums;

/**
 * Dubbo泛化调用参数类型枚举
 * <p>
 * 定义支持的Java参数类型，用于泛化调用时的类型推断
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public enum ParameterType {

    STRING("java.lang.String"),
    INTEGER("java.lang.Integer"),
    LONG("java.lang.Long"),
    DOUBLE("java.lang.Double"),
    FLOAT("java.lang.Float"),
    BOOLEAN("java.lang.Boolean"),
    LIST("java.util.List"),
    MAP("java.util.Map"),
    OBJECT("java.lang.Object");

    private final String javaType;

    ParameterType(String javaType) {
        this.javaType = javaType;
    }

    public String getJavaType() {
        return javaType;
    }

    /**
     * 根据值推断参数类型
     *
     * @param value 参数值
     * @return 对应的参数类型枚举
     */
    public static ParameterType fromValue(Object value) {
        if (value == null) {
            return OBJECT;
        }
        if (value instanceof String) {
            return STRING;
        }
        if (value instanceof Integer) {
            return INTEGER;
        }
        if (value instanceof Long) {
            return LONG;
        }
        if (value instanceof Double) {
            return DOUBLE;
        }
        if (value instanceof Float) {
            return FLOAT;
        }
        if (value instanceof Boolean) {
            return BOOLEAN;
        }
        if (value instanceof java.util.List) {
            return LIST;
        }
        if (value instanceof java.util.Map) {
            return MAP;
        }
        return OBJECT;
    }

    /**
     * 获取值对应的Java类型名称
     *
     * @param value 参数值
     * @return Java类型全限定名
     */
    public static String getJavaTypeFromValue(Object value) {
        ParameterType type = fromValue(value);
        if (type == OBJECT && value != null) {
            return value.getClass().getName();
        }
        return type.getJavaType();
    }
}
