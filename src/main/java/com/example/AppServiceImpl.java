package com.example;

import io.blaze.server.annotation.Init;
import io.blaze.server.annotation.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public final class AppServiceImpl implements AppService {

    private final static Logger LOG = LogManager.getLogger(AppServiceImpl.class);

    @Value("server.port:80")
    private int port;

    @Init
    private void init() {
        LOG.info("AppService initialized");
        LOG.info("Server port: {}", port);
    }

    @Override
    public Map<String, String> welcome(final String key,
                                       final Map<String, String> body) {
        return Map.of(key, body.get("message"));
    }

}
