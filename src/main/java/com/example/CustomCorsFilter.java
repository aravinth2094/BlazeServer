package com.example;

import io.blaze.server.handler.HttpResponseFilter;
import io.blaze.server.model.HttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CustomCorsFilter extends HttpResponseFilter {

    private static final Logger LOG = LogManager.getLogger(CustomCorsFilter.class);

    @Override
    protected void filter(final HttpResponse response) {
        LOG.debug("Applying CORS...");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "https://localhost:8080");
    }
}
