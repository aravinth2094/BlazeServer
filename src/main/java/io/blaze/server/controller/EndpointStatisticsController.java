package io.blaze.server.controller;

import io.blaze.server.annotation.Get;
import io.blaze.server.annotation.Inject;
import io.blaze.server.annotation.Route;
import io.blaze.server.handler.EndpointStatisticsFilter;
import io.blaze.server.model.EndpointStatistics;
import io.blaze.server.model.HttpRequest;

import java.util.Map;

@Route("/stats/endpoints")
public final class EndpointStatisticsController {

    @Inject
    private EndpointStatisticsFilter endpointStatisticsFilter;

    @Get("/all")
    public Map<String, EndpointStatistics> getAll(final HttpRequest request) {
        return endpointStatisticsFilter.getEndpointsStatisticsMap();
    }

}
