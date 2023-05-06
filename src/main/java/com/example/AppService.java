package com.example;

import java.util.Map;

public final class AppService {

    public Map<String, String> welcome(final String key,
                                       final Map<String, String> body) {
        return Map.of(key, body.get("message"));
    }

}
