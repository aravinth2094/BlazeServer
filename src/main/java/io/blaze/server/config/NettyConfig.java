package io.blaze.server.config;

import io.netty.bootstrap.ServerBootstrap;

public interface NettyConfig {

    ServerBootstrap getBootstrap();

}
