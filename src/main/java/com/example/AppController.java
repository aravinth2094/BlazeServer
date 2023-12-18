package com.example;

import io.blaze.server.annotation.Get;
import io.blaze.server.annotation.Inject;
import io.blaze.server.annotation.Post;
import io.blaze.server.annotation.Route;
import io.blaze.server.model.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Route("/api/v1")
public final class AppController {

    private final static Logger LOG = LogManager.getLogger(AppController.class);

    @Inject(implementation = AppServiceImpl.class)
    private AppService appService;

    @Get("/welcome")
    public Map<String, String> welcome(final HttpRequest request) {
        LOG.info("Handling request");
        return Map.of("message", "hello from netty");
    }

    @Post("/welcome")
    public Map<String, String> welcomePost(final HttpRequest request) throws Exception {
        LOG.info("Handling request");
        final Map<?, ?> body = request.body(Map.class);
        return Map.of("echo", (String) body.get("message"));
    }

    @Post("/welcome/:key")
    public Map<String, String> welcomePostWithPathVariable(final HttpRequest request) throws Exception {
        LOG.info("Handling request");
        final Map<?, ?> body = request.body(Map.class);
        return appService.welcome(request.getPathVariable("key"), (Map<String, String>) body);
    }
}
