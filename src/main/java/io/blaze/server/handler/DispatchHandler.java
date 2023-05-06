package io.blaze.server.handler;

import io.blaze.server.model.HttpRequest;
import io.blaze.server.model.HttpResponse;
import io.blaze.server.model.ResolvedRoute;
import io.blaze.server.resolver.RouteResolver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DispatchHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private static final Logger LOG = LogManager.getLogger(DispatchHandler.class);
    private static final RouteResolver ROUTE_RESOLVER = new RouteResolver();

    private final List<?> controllers;
    private final ExecutorService handlerThreadPool;

    public DispatchHandler(final List<?> controllers) {
        this.controllers = controllers;
        this.handlerThreadPool = Executors.newFixedThreadPool(10, runnable -> new Thread(runnable, String.format("dispatch-handler-%d", Thread.currentThread().getId())));
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx,
                                final HttpRequest request) throws Exception {
        LOG.info("{} {}", request.method(), request.uri());
        final ResolvedRoute resolvedRoute = ROUTE_RESOLVER.resolve(request.method(), request.uri(), controllers);
        if (resolvedRoute == null) {
            ctx.writeAndFlush(new HttpResponse(HttpResponseStatus.NOT_FOUND));
            return;
        }
        handlerThreadPool.submit(() -> {
            Object result;
            try {
                result = resolvedRoute.invoke(request);
            } catch (final Exception e) {
                LOG.error("Dispatch invocation error", e);
                result = new HttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
            ctx.writeAndFlush(result);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.writeAndFlush(new HttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR));
    }
}
