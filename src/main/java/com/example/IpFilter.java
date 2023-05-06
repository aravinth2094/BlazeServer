package com.example;

import io.blaze.server.handler.HttpRequestFilter;
import io.blaze.server.model.HttpRequest;
import io.blaze.server.model.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IpFilter extends HttpRequestFilter {

    private static final Logger LOG = LogManager.getLogger(IpFilter.class);

    @Override
    protected HttpResponse filter(final HttpRequest request) {
        LOG.info("Request from " + request.remoteAddress().getAddress().getHostAddress());
        if ("127.0.0.1".equals(request.remoteAddress().getAddress().getHostAddress())) {
            return new HttpResponse(HttpResponseStatus.FORBIDDEN);
        }
        return null;
    }
}
