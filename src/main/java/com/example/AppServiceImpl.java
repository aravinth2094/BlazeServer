package com.example;

import io.blaze.server.annotation.Init;
import io.blaze.server.annotation.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public final class AppServiceImpl implements AppService {

    private final static Logger LOG = LogManager.getLogger(AppServiceImpl.class);

    @Value("app.worker.threads:5")
    private int workerThreads;

    @Init
    private void init() {
        LOG.info("AppService initialized");
        LOG.info("Worker threads: {}", workerThreads);
    }

    @Override
    public Map<String, String> welcome(final String key,
                                       final Map<String, String> body) {
        return Map.of(key, body.get("message"));
    }

}
