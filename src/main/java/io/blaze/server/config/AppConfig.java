package io.blaze.server.config;

public final class AppConfig {

    private ServerConfig server;

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
}
