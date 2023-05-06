package io.blaze.server.config;

import io.blaze.server.handler.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.List;

final class AppChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final List<?> controllers;
    private final List<HttpRequestFilter> requestFilters;
    private final List<HttpResponseFilter> responseFilters;

    public AppChannelInitializer(final List<?> controllers,
                                 final List<HttpRequestFilter> requestFilters,
                                 final List<HttpResponseFilter> responseFilters) {
        this.controllers = controllers;
        this.requestFilters = requestFilters;
        this.responseFilters = responseFilters;
    }

    @Override
    protected void initChannel(final SocketChannel ch) {
        ch.pipeline()
                // Encoders
                .addLast(new HttpResponseEncoder())
                .addLast(new HttpResponseObjectEncoder());

        // Response Filters
        for (final HttpResponseFilter responseFilter : responseFilters) {
            ch.pipeline().addLast(responseFilter);
        }

        ch.pipeline()
                // Encoders
                .addLast(new HttpResponseDelegateEncoder());

        ch.pipeline()
                // Decoders
                .addLast(new HttpRequestDecoder())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                .addLast(new HttpRequestDelegateDecoder());

        // Request Filters
        for (final HttpRequestFilter requestFilter : requestFilters) {
            ch.pipeline().addLast(requestFilter);
        }


        ch.pipeline()
                // Handlers
                .addLast(new DispatchHandler(controllers));
    }
}
