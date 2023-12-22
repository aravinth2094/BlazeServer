package io.blaze.server.config;

import io.blaze.server.handler.HttpRequestFilter;
import io.blaze.server.handler.HttpResponseFilter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
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
                .channel(getServerSocketChannelClass())
                .childHandler(new AppChannelInitializer(
                        getControllers(),
                        getRequestFilters(),
                        getResponseFilters()
                ))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    private Class<? extends ServerSocketChannel> getServerSocketChannelClass() {
        if (Epoll.isAvailable()) return EpollServerSocketChannel.class;
        if (KQueue.isAvailable()) return KQueueServerSocketChannel.class;
        return NioServerSocketChannel.class;
    }

}
