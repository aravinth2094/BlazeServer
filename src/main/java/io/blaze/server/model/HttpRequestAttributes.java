package io.blaze.server.model;

import java.util.HashMap;
import java.util.Map;

public final class HttpRequestAttributes {

    private final Map<String, Object> attributes;

    public HttpRequestAttributes() {
        attributes = new HashMap<>();
    }

    public <T> T get(final String key) {
        return (T) attributes.get(key);
    }

    public <T> HttpRequestAttributes put(final String key,
                                         final T value) {
        attributes.put(key, value);
        return this;
    }

    public Object remove(final String key) {
        return attributes.remove(key);
    }

    public <T> T getOrDefault(final String key,
                              final T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    public Object putIfAbsent(final String key,
                              final Object value) {
        return attributes.putIfAbsent(key, value);
    }
}
