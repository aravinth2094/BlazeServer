package com.example;

import io.blaze.server.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Route("/api/v1")
public final class AppController {

    private final static Logger LOG = LogManager.getLogger(AppController.class);

    @Inject(implementation = AppServiceImpl.class)
    private AppService appService;

    @Get("/welcome")
    public Map<String, String> welcome() {
        LOG.info("Handling request");
        return Map.of("message", "hello from netty");
    }

    @Post("/welcome")
    public Map<String, String> welcomePost(@Body Map<String, String> body,
                                           @Value("key:echo") String key,
                                           @Value("java.runtime.version:unknown") String javaRuntime,
                                           @RequestAttribute("ip") String ipAddress) {
        LOG.info("Handling request");
        return Map.of(
                key, body.get("message"),
                "runtime", javaRuntime,
                "ipAddress", ipAddress
        );
    }

    @Post("/welcome/:key")
    public Map<String, String> welcomePostWithPathVariable(@Body Map<String, String> body,
                                                           @PathVariable("key") String key) {
        LOG.info("Handling request");
        return appService.welcome(key, body);
    }
}
