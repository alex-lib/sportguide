package com.sport.service.utils;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

public final class BeanUtils {

    @SneakyThrows
    public static void copyNonNullProperties(Object source, Object destination) {
        Class<?> sourceClass = source.getClass();
        Field[] sourceFields = sourceClass.getDeclaredFields();
        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);
            Object value = sourceField.get(source);
            if (value != null) {
                sourceField.set(destination, value);
            }
        }
    }
}