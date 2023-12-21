package io.blaze.server.resolver;

/**
 * @author Aravinth Sundaram on 21/12/23
 */
public class ObjectUtils {

    private ObjectUtils() {
        // utility class
    }

    @SafeVarargs
    public static <T> T firstNonNull(T... args) {
        if (args == null) return null;
        for (final T arg : args) {
            if (arg != null) {
                return arg;
            }
        }
        return null;
    }
}
