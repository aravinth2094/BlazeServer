package io.blaze.server;

import io.blaze.server.annotation.Inject;
import io.blaze.server.annotation.Route;
import io.blaze.server.config.AppConfig;
import io.blaze.server.config.DefaultNettyConfig;
import io.blaze.server.config.NettyConfig;
import io.blaze.server.config.ServerConfig;
import io.blaze.server.controller.EndpointStatisticsController;
import io.blaze.server.handler.EndpointStatisticsFilter;
import io.blaze.server.handler.HttpRequestFilter;
import io.blaze.server.handler.HttpResponseFilter;
import io.blaze.server.resolver.DependencyResolver;
import io.blaze.server.resolver.PathAnnotationEnum;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public final class App {

    private static final Logger LOG = LogManager.getLogger(App.class);
    private static final AppConfig DEFAULT_APP_CONFIG = new AppConfig(new ServerConfig(8080));
    private static final String APP_CONFIG_FILE_NAME = "application";

    private final List<Object> controllers;
    private final List<HttpRequestFilter> requestFilters;
    private final List<HttpResponseFilter> responseFilters;
    private final AppConfig appConfig;
    private boolean endpointStatisticsEnabled;
    @Inject
    private EndpointStatisticsFilter endpointStatisticsFilter;
    @Inject
    private EndpointStatisticsController endpointStatisticsController;

    public App() {
        this.controllers = new LinkedList<>();
        this.requestFilters = new LinkedList<>();
        this.responseFilters = new LinkedList<>();
        this.appConfig = loadAppConfig();
    }

    private AppConfig loadAppConfig() {
        final String profile = System.getenv("profile");
        LOG.info("Loading {} profile", profile != null ? profile : "default");
        final String fileName = getFileNameForProfile(profile);
        if (this.getClass().getClassLoader().getResource(fileName) == null) {
            LOG.info("{}.yaml not found. Loading default configuration", APP_CONFIG_FILE_NAME);
            return DEFAULT_APP_CONFIG;
        }
        try (final InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            final Yaml yaml = new Yaml();
            return yaml.loadAs(in, AppConfig.class);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileNameForProfile(final String profile) {
        if (profile == null) return APP_CONFIG_FILE_NAME + ".yaml";
        final String fileNameWithProfile = APP_CONFIG_FILE_NAME + "-" + profile + ".yaml";
        final URL fileUrl = this.getClass().getClassLoader().getResource(fileNameWithProfile);
        return fileUrl != null ? fileNameWithProfile : APP_CONFIG_FILE_NAME + ".yaml";
    }

    public void start() {
        DependencyResolver.resolve(this);
        if (endpointStatisticsEnabled) {
            addFilter(endpointStatisticsFilter);
            addController(endpointStatisticsController);
        }
        LOG.info("Application starting...");
        final LocalDateTime start = LocalDateTime.now();
        validatePaths();
        final EventLoopGroup parentGroup = getEventLoopGroup(2);
        final EventLoopGroup childGroup = getEventLoopGroup(3);
        try {
            final NettyConfig nettyConfig = new DefaultNettyConfig(
                    parentGroup,
                    childGroup,
                    Collections.unmodifiableList(controllers),
                    Collections.unmodifiableList(requestFilters),
                    Collections.unmodifiableList(responseFilters));
            final ChannelFuture channelFuture = nettyConfig.getBootstrap().bind(appConfig.getServer().getPort()).sync();
            LOG.info("Application started on port {}", appConfig.getServer().getPort());
            LOG.info("Server started in {} ms", Duration.between(start, LocalDateTime.now()).toMillis());
            channelFuture.channel().closeFuture().sync();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }

    private EventLoopGroup getEventLoopGroup(final int threads) {
        if (Epoll.isAvailable()) return new EpollEventLoopGroup(threads);
        return new NioEventLoopGroup();
    }

    public App addController(final Object controller) {
        if (controller == null) {
            throw new IllegalStateException("Controller cannot be null");
        }
        if (!controller.getClass().isAnnotationPresent(Route.class)) {
            throw new IllegalStateException(controller.getClass().getCanonicalName() + " is not a controller. Must have @Route annotation");
        }
        controllers.add(DependencyResolver.resolve(controller));
        LOG.info("Added controller " + controller.getClass().getCanonicalName());
        return this;
    }

    public App addFilter(final HttpRequestFilter requestFilter) {
        if (requestFilter == null) {
            throw new IllegalStateException("Request filter cannot be null");
        }
        requestFilters.add(DependencyResolver.resolve(requestFilter));
        return this;
    }

    public App addFilter(final HttpResponseFilter responseFilter) {
        if (responseFilter == null) {
            throw new IllegalStateException("Response filter cannot be null");
        }
        responseFilters.add(DependencyResolver.resolve(responseFilter));
        return this;
    }

    public App enableEndpointStatistics() {
        endpointStatisticsEnabled = true;
        return this;
    }

    private void validatePaths() {
        if (controllers.isEmpty()) {
            throw new IllegalStateException("No controllers found");
        }
        final Set<String> paths = new HashSet<>();
        for (final Object controller : controllers) {
            final Route routeAnnotation = controller.getClass().getAnnotation(Route.class);
            for (final String route : routeAnnotation.value()) {
                for (final Method method : controller.getClass().getDeclaredMethods()) {
                    for (final PathAnnotationEnum pathAnnotationEnum : PathAnnotationEnum.values()) {
                        final String[] pathValues = pathAnnotationEnum.getValue(method);
                        if (pathValues == null) {
                            continue;
                        }
                        for (final String pathValue : pathValues) {
                            final String fullPath = pathAnnotationEnum.getMethod() + " " + route + pathValue;
                            if (!paths.add(fullPath)) {
                                throw new IllegalStateException(fullPath + " already configured");
                            }
                            LOG.info("Found {}", fullPath);
                        }
                    }
                }
            }
        }
    }

}
