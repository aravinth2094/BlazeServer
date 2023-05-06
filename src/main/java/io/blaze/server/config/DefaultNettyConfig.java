package io.blaze.server.config;

import io.blaze.server.handler.HttpRequestFilter;
import io.blaze.server.handler.HttpResponseFilter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.List;

public class DefaultNettyConfig extends AbstractNettyConfig {

    private final EventLoopGroup parentGroup;
    private final EventLoopGroup childGroup;

    public DefaultNettyConfig(final EventLoopGroup parentGroup,
                              final EventLoopGroup childGroup,
                              final List<?> controllers,
                              final List<HttpRequestFilter> requestFilters,
                              final List<HttpResponseFilter> responseFilters) {
        super(
                controllers,
                requestFilters,
                responseFilters
        );
        this.parentGroup = parentGroup;
        this.childGroup = childGroup;
    }

    @Override
    public ServerBootstrap getBootstrap() {
        return new ServerBootstrap()
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new AppChannelInitializer(
                        getControllers(),
                        getRequestFilters(),
                        getResponseFilters()
                ))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

}
