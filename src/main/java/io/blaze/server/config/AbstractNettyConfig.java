package io.blaze.server.config;

import io.blaze.server.handler.HttpRequestFilter;
import io.blaze.server.handler.HttpResponseFilter;

import java.util.Collections;
import java.util.List;

abstract class AbstractNettyConfig implements NettyConfig {

    private final List<?> controllers;
    private final List<HttpRequestFilter> requestFilters;
    private final List<HttpResponseFilter> responseFilters;

    protected AbstractNettyConfig(final List<?> controllers,
                                  final List<HttpRequestFilter> requestFilters,
                                  final List<HttpResponseFilter> responseFilters) {
        this.controllers = Collections.unmodifiableList(controllers);
        this.requestFilters = Collections.unmodifiableList(requestFilters);
        this.responseFilters = Collections.unmodifiableList(responseFilters);
    }

    public final List<?> getControllers() {
        return controllers;
    }

    public final List<HttpRequestFilter> getRequestFilters() {
        return requestFilters;
    }

    public final List<HttpResponseFilter> getResponseFilters() {
        return responseFilters;
    }

}
