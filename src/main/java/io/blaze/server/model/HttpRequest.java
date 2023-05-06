package io.blaze.server.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public final class HttpRequest {

    private final FullHttpRequest fullHttpRequest;
    private final ObjectMapper mapper;
    private final byte[] content;
    private final HttpRequestAttributes attributes;
    private final ChannelHandlerContext ctx;
    private final Map<String, Object> pathVariables;
    private final LocalDateTime startTime;

    public HttpRequest(final FullHttpRequest fullHttpRequest,
                       final ChannelHandlerContext ctx) {
        this.fullHttpRequest = fullHttpRequest;
        this.ctx = ctx;
        this.attributes = new HttpRequestAttributes();
        this.pathVariables = new HashMap<>();
        this.startTime = LocalDateTime.now();

        mapper = new ObjectMapper();
        if (!(HttpMethod.GET.equals(method()) || HttpMethod.DELETE.equals(method()))) {
            final ByteBuf buffer = fullHttpRequest.content();
            content = new byte[buffer.readableBytes()];
            buffer.readBytes(content);
        } else {
            content = null;
        }
    }

    public HttpMethod method() {
        return fullHttpRequest.method();
    }

    public String uri() {
        return fullHttpRequest.uri();
    }

    public HttpHeaders headers() {
        return fullHttpRequest.headers();
    }

    public <T> T body(final Class<T> clazz) throws Exception {
        return mapper.readValue(content, clazz);
    }

    public <T> T getAttribute(final String key) {
        return attributes.get(key);
    }

    public <T> HttpRequest putAttribute(final String key,
                                        final T value) {
        attributes.put(key, value);
        return this;
    }

    public HttpRequestAttributes getAttributes() {
        return attributes;
    }

    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) ctx.channel().remoteAddress();
    }

    void setPathVariables(final Map<String, Object> pathVariables) {
        this.pathVariables.putAll(pathVariables);
    }

    public <T> T getPathVariable(final String name) {
        return (T) this.pathVariables.get(name);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getContentLength() {
        return content.length;
    }
}
