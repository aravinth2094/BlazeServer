package io.blaze.server.resolver;

import io.blaze.server.annotation.Init;
import io.blaze.server.annotation.Inject;
import io.blaze.server.annotation.Value;
import io.blaze.server.context.AppContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;

public final class DependencyResolver {

    private DependencyResolver() {
        // Utility class
    }

    public static <T> T resolve(final T obj) {
        try {
            for (final Field field : obj.getClass().getDeclaredFields()) {
                if (!field.trySetAccessible()) {
                    continue;
                }
                setProperty(obj, field);
                if (!field.isAnnotationPresent(Inject.class)) {
                    continue;
                }
                if (field.get(obj) != null) {
                    continue;
                }
                final Inject annotation = field.getAnnotation(Inject.class);
                final Object instance = switch (annotation.scope()) {
                    case Singleton -> getOrCreate(field);
                    case Prototype -> createInstance(field);
                };
                field.set(obj, resolve(instance));
            }
            init(obj);
            return obj;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void init(final T obj) {
        try {
            int count = 0;
            for (final Method method : obj.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Init.class)) {
                    if (++count > 1) {
                        throw new IllegalStateException(obj.getClass().getCanonicalName() + " cannot have multiple @Init annotations");
                    }
                    if (Modifier.isStatic(method.getModifiers())) {
                        throw new IllegalStateException("@Init method " + method.getName() + " cannot be static");
                    }
                    method.setAccessible(true);
                    method.invoke(obj);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void setProperty(final T obj, final Field field) {
        try {
            if (!field.isAnnotationPresent(Value.class)) {
                return;
            }
            final Value value = field.getAnnotation(Value.class);
            final Object fieldValue = ValueUtils.extractValue(value.value(), field.getType());
            if (fieldValue == null) {
                throw new IllegalStateException(
                        MessageFormat.format(
                                "Cannot set property \"{0}\" to field \"{1}\". " +
                                        "Property \"{0}\" not found. Assign a default value to avoid exception.",
                                value.value().split(":")[0], field)
                );
            }
            field.set(obj, fieldValue);
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
