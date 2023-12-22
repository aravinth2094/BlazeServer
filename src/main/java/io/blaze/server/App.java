package io.blaze.server;

import io.blaze.server.annotation.Route;
import io.blaze.server.config.AppConfig;
import io.blaze.server.config.DefaultNettyConfig;
import io.blaze.server.config.NettyConfig;
import io.blaze.server.config.ServerConfig;
import io.blaze.server.context.AppContext;
import io.blaze.server.handler.HttpRequestFilter;
import io.blaze.server.handler.HttpResponseFilter;
import io.blaze.server.resolver.DependencyResolver;
import io.blaze.server.resolver.PathAnnotationEnum;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
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

    public App() {
        this.controllers = new LinkedList<>();
        this.requestFilters = new LinkedList<>();
        this.responseFilters = new LinkedList<>();
        this.appConfig = loadAppConfig();
        AppContext.put(AppConfig.class, this.appConfig);
    }

    private AppConfig loadAppConfig() {
        final String profile = System.getenv("profile");
        LOG.info("Loading {} profile", profile != null ? profile : "default");
        final String fileName = getFileNameForProfile(profile);
        if (this.getClass().getClassLoader().getResource(fileName) == null) {
            LOG.info("{}.yaml not found. Loading default configuration", APP_CONFIG_FILE_NAME);
            return DEFAULT_APP_CONFIG;
        }
        final AppConfig appConfig;
        try (final InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            final Yaml yaml = new Yaml();
            appConfig = yaml.loadAs(in, AppConfig.class);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        Properties properties = flattenYaml(fileName);
        System.getProperties().forEach((key, value) -> {
            properties.setProperty(String.valueOf(key), String.valueOf(value));
        });
        appConfig.setProperties(properties);
        return appConfig;
    }

    private Properties flattenYaml(final String yamlFileName) {
        try (final InputStream in = this.getClass().getClassLoader().getResourceAsStream(yamlFileName)) {
            return flattenYaml("", new Yaml().load(in), new Properties());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Properties flattenYaml(String currentKey, Map<String, Object> yamlData, Properties properties) {
        for (Map.Entry<String, Object> entry : yamlData.entrySet()) {
            final String key = currentKey.isEmpty() ? entry.getKey() : currentKey + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenYaml(key, (Map<String, Object>) entry.getValue(), properties);
            } else {
                properties.setProperty(key, entry.getValue().toString());
            }
        }
        return properties;
    }

    private String getFileNameForProfile(final String profile) {
        if (profile == null) return APP_CONFIG_FILE_NAME + ".yaml";
        final String fileNameWithProfile = APP_CONFIG_FILE_NAME + "-" + profile + ".yaml";
        final URL fileUrl = this.getClass().getClassLoader().getResource(fileNameWithProfile);
        return fileUrl != null ? fileNameWithProfile : APP_CONFIG_FILE_NAME + ".yaml";
    }

    public void start() {
        DependencyResolver.resolve(this);
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
        if (KQueue.isAvailable()) return new KQueueEventLoopGroup(threads);
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
