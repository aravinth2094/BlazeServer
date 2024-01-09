package io.blaze.server.context;

import io.blaze.server.resolver.MethodInvoker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

public final class AppContext {

    private static final Logger LOG = LogManager.getLogger(AppContext.class);
    private static final Map<Class<?>, Object> OBJECT_STORE = new Hashtable<>();
    private static final MethodInvoker INVOKER = new MethodInvoker();

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

    public static <T> T getOrDefault(final Class<? extends T> key,
                                     final T defaultValue) {
        return (T) OBJECT_STORE.getOrDefault(key, defaultValue);
    }

    public static <T> T computeIfAbsent(final Class<? extends T> key,
                                        final Function<? super Class<?>, ?> mappingFunction) {
        return (T) OBJECT_STORE.computeIfAbsent(key, mappingFunction);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createInstance(Class<? extends T> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return (T) INVOKER.construct(clazz.getDeclaredConstructors()[0]);
    }
}
