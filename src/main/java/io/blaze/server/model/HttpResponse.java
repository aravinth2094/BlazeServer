package io.blaze.server.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.*;

public final class HttpResponse {

    private final FullHttpResponse fullHttpResponse;
    private final int contentLength;

    public HttpResponse(final HttpResponseStatus status) {
        this(status, null);
    }

    public HttpResponse(final HttpResponseStatus status,
                        final Object body) {
        this(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status), body);
    }

    private HttpResponse(final FullHttpResponse fullHttpResponse,
                         final Object body) {
        this.fullHttpResponse = fullHttpResponse;
        headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        if (body == null) {
            headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        }
        try {
            final byte[] content = new ObjectMapper().writeValueAsBytes(body);
            contentLength = content.length;
            this.fullHttpResponse.content().writeBytes(content);
            headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FullHttpResponse setStatus(final HttpResponseStatus status) {
        return fullHttpResponse.setStatus(status);
    }

    public HttpResponseStatus status() {
        return fullHttpResponse.status();
    }

    public HttpHeaders headers() {
        return fullHttpResponse.headers();
    }

    public FullHttpResponse underlyingResponse() {
        return fullHttpResponse;
    }

    public int getContentLength() {
        return contentLength;
    }
}
