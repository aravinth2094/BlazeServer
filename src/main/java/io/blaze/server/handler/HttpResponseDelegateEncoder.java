package io.blaze.server.handler;

import io.blaze.server.model.HttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

public final class HttpResponseDelegateEncoder extends MessageToMessageEncoder<Object> {
    @Override
    protected void encode(final ChannelHandlerContext ctx,
                          final Object msg,
                          final List<Object> out) throws Exception {
        if (msg instanceof HttpResponse response) {
            out.add(response);
            return;
        }
        out.add(new HttpResponse(HttpResponseStatus.OK, msg));
    }
}
