package io.blaze.server.handler;

import io.blaze.server.model.HttpRequest;
import io.blaze.server.model.HttpResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public abstract class HttpRequestFilter extends SimpleChannelInboundHandler<HttpRequest> {

    private static final Logger LOG = LogManager.getLogger(HttpRequestFilter.class);

    @Override
    protected final void channelRead0(final ChannelHandlerContext ctx,
                                      final HttpRequest request) {
        HttpResponse response;
        try {
            response = filter(request);
        } catch (final Exception e) {
            LOG.error("Filter error", e);
            response = new HttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
        if (response != null) {
            ctx.writeAndFlush(response);
            return;
        }
        ctx.fireChannelRead(request);
    }

    protected abstract HttpResponse filter(final HttpRequest request);
}
