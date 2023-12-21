package io.blaze.server.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

public final class AppContext {

    private static final Logger LOG = LogManager.getLogger(AppContext.class);
    private static final Map<Class<?>, Object> OBJECT_STORE = new Hashtable<>();

    private AppContext() {
        // Utility class
    }

    public static <T> T get(final Class<? extends T> key) {
        return (T) OBJECT_STORE.get(key);
    }

    public static <T> void put(final Class<? extends T> key,
                               final T value) {
        OBJECT_STORE.put(key, value);
    }

    public static Collection<?> getAll() {
        return Collections.unmodifiableCollection(OBJECT_STORE.values());
    }

    public static <T> T getOrDefault(final Class<?> key,
                                     final T defaultValue) {
        return (T) OBJECT_STORE.getOrDefault(key, defaultValue);
    }

    public static <T> T computeIfAbsent(final Class<?> key,
                                        final Function<? super Class<?>, ?> mappingFunction) {
        return (T) OBJECT_STORE.computeIfAbsent(key, mappingFunction);
    }

    public static <T> T createInstance(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                LOG.info("Creating instance for {}", clazz.getCanonicalName());
                return (T) constructor.newInstance();
            }
        }
        throw new NoSuchMethodException(clazz.getCanonicalName() + " must have a default constructor");
    }
}
