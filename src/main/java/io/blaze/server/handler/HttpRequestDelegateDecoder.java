package io.blaze.server.handler;

import io.blaze.server.model.HttpRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;

import java.util.List;

public final class HttpRequestDelegateDecoder extends MessageToMessageDecoder<FullHttpRequest> {

    private static final AttributeKey<HttpRequest> ATTRIBUTE_KEY_HTTP_REQUEST = AttributeKey.valueOf("request");

    @Override
    protected void decode(final ChannelHandlerContext ctx,
                          final FullHttpRequest msg,
                          final List<Object> out) throws Exception {
        final HttpRequest request = new HttpRequest(msg, ctx);
        ctx.channel().attr(ATTRIBUTE_KEY_HTTP_REQUEST).set(request);
        out.add(request);
    }
}
