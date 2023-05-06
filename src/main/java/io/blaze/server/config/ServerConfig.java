package io.blaze.server.config;

public final class ServerConfig {

    private int port;

    public ServerConfig() {
    }

    public ServerConfig(final int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
