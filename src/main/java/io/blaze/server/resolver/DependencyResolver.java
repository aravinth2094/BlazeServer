package io.blaze.server.resolver;

import io.blaze.server.annotation.Inject;
import io.blaze.server.context.AppContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class DependencyResolver {

    private DependencyResolver() {
        // Utility class
    }

    public static <T> T resolve(final T obj) {
        try {
            for (final Field field : obj.getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(Inject.class)) {
                    continue;
                }
                field.setAccessible(true);
                if (field.get(obj) != null) {
                    continue;
                }
                final Inject annotation = field.getAnnotation(Inject.class);
                final Object instance = switch (annotation.scope()) {
                    case Singleton -> getOrCreate(field);
                    case Prototype -> createInstance(field);
                };
                field.set(obj, instance);
                resolve(instance);
            }
            return obj;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getOrCreate(final Field field) {
        return AppContext.computeIfAbsent(field.getType(), _clazz -> createInstance(field));
    }

    private static Object createInstance(final Field field) {
        Class<?> clazz = field.getType();
        if (Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers())) {
            clazz = field.getAnnotation(Inject.class).implementation();
            if (clazz == Object.class) {
                throw new IllegalStateException("implementation attribute must be specified");
            }
        }
        try {
            return AppContext.createInstance(clazz);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
