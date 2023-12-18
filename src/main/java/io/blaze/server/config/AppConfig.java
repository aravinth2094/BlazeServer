package io.blaze.server.config;

import java.util.Properties;

public final class AppConfig {

    private ServerConfig server;
    private Properties properties;

    public AppConfig() {
    }

    public AppConfig(final ServerConfig server) {
        this.server = server;
    }

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
