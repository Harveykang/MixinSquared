package com.bawnorton.mixinsquared.reflection;

import java.lang.reflect.Constructor;

public class ConstructorReference<T> {
    private final Constructor<?> constructor;

    public ConstructorReference(Class<?> clazz, Class<?>... parameterTypes) {
        Constructor<?> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        constructor.setAccessible(true);
        this.constructor = constructor;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(Object... args) {
        try {
            return (T) constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
