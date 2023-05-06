package io.blaze.server.handler;

import io.blaze.server.model.HttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public final class HttpResponseObjectEncoder extends MessageToMessageEncoder<HttpResponse> {
    @Override
    protected void encode(final ChannelHandlerContext ctx,
                          final HttpResponse msg,
                          final List<Object> out) throws Exception {
        out.add(msg.underlyingResponse());
    }
}
