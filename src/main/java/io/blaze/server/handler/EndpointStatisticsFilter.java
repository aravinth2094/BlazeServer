package io.blaze.server.handler;

import io.blaze.server.model.EndpointStatistics;
import io.blaze.server.model.HttpRequest;
import io.blaze.server.model.HttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EndpointStatisticsFilter extends HttpResponseFilter {

    private static final AttributeKey<HttpRequest> ATTRIBUTE_KEY_HTTP_REQUEST = AttributeKey.valueOf("request");
    private static EndpointStatisticsFilter INSTANCE;
    private final Map<String, EndpointStatistics> endpointStatisticsMap = new HashMap<>();

    private EndpointStatisticsFilter() {
        // Singleton
    }

    public Map<String, EndpointStatistics> getEndpointsStatisticsMap() {
        return Collections.unmodifiableMap(endpointStatisticsMap);
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx,
                          final HttpResponse response,
                          final List<Object> out) throws Exception {
        final HttpRequest request = ctx.channel().attr(ATTRIBUTE_KEY_HTTP_REQUEST).get();
        final LocalDateTime now = LocalDateTime.now();
        final long responseTimeInMilliseconds = Duration.between(request.getStartTime(), now).toMillis();
        final String endpoint = String.format("%s %s", request.method(), request.uri());
        synchronized (endpointStatisticsMap) {
            endpointStatisticsMap.putIfAbsent(endpoint, new EndpointStatistics());
            final EndpointStatistics endpointStatistics = endpointStatisticsMap.get(endpoint)
                    .addResponseTime(responseTimeInMilliseconds);
            if (response.status().code() >= 400) {
                endpointStatistics.addNumberOfFailures(1);
            } else {
                endpointStatistics.addNumberOfSuccess(1);
            }
        }
        super.encode(ctx, response, out);
    }

    @Override
    protected void filter(final HttpResponse response) {
        // Do nothing
    }
}
