package io.blaze.server.handler;

import io.blaze.server.model.HttpResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

@ChannelHandler.Sharable
public abstract class HttpResponseFilter extends MessageToMessageEncoder<HttpResponse> {

    @Override
    protected void encode(final ChannelHandlerContext ctx,
                          final HttpResponse response,
                          final List<Object> out) throws Exception {
        filter(response);
        out.add(response);
    }

    protected abstract void filter(final HttpResponse response);

}
