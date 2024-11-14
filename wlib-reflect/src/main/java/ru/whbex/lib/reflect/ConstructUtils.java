package ru.whbex.lib.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ConstructUtils {

    public static <T> T newInstance(Class<T> clazz, Object... constructorArgs) throws InvocationTargetException {
        try {
            return clazz.getConstructor(Arrays.stream(constructorArgs).map(Object::getClass).toArray(Class[]::new)).newInstance(constructorArgs);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
