package com.example;

import java.util.Map;

/**
 * @author Aravinth Sundaram on 18/12/23
 */
public interface AppService {

    Map<String, String> welcome(final String key,
                                final Map<String, String> body);
}
