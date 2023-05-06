package io.blaze.server.resolver;

import io.blaze.server.annotation.Get;
import io.blaze.server.annotation.Post;
import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum PathAnnotationEnum {

    GET(HttpMethod.GET,
            method -> {
                final Get annotation = method.getAnnotation(Get.class);
                if (annotation == null) {
                    return null;
                }
                return annotation.value();
            }),
    POST(HttpMethod.POST,
            method -> {
                final Post annotation = method.getAnnotation(Post.class);
                if (annotation == null) {
                    return null;
                }
                return annotation.value();
            });

    private static final Map<HttpMethod, PathAnnotationEnum> cache;

    static {
        Map<HttpMethod, PathAnnotationEnum> _cache = new HashMap<>();
        for (final PathAnnotationEnum pathEnum : values()) {
            _cache.put(pathEnum.method, pathEnum);
        }
        cache = Collections.unmodifiableMap(_cache);
    }

    private final HttpMethod method;
    private final Function<Method, String[]> valueAccessor;

    PathAnnotationEnum(final HttpMethod method,
                       final Function<Method, String[]> valueAccessor) {
        this.method = method;
        this.valueAccessor = valueAccessor;
    }

    public static PathAnnotationEnum getForMethod(final HttpMethod method) {
        if (!cache.containsKey(method)) {
            throw new UnsupportedOperationException(method + " method not supported");
        }
        return cache.get(method);
    }

    public String[] getValue(final Method method) {
        return valueAccessor.apply(method);
    }

    public HttpMethod getMethod() {
        return method;
    }

}
