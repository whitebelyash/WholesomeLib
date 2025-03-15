package ru.whbex.lib.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ReflectUtils {

    // This is actually useless
    public static <T> T newInstance(Class<T> clazz, Object... constructorArgs) throws InvocationTargetException {
        try {
            return clazz.getConstructor(Arrays.stream(constructorArgs).map(Object::getClass).toArray(Class[]::new)).newInstance(constructorArgs);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object target, String name) throws NoSuchFieldException, IllegalAccessException {
        Field f = target.getClass().getField(name);
        f.setAccessible(true);
        return (T) f.get(target);
    }
    @SuppressWarnings("unchecked")
    public static <T> T getDeclField(Object target, String name) throws NoSuchFieldException, IllegalAccessException {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(target);
    }
}
