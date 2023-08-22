package com.yarg.robotpiserver.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.yarg.gen.models.ConfigurationModel;
import com.yarg.robotpiserver.config.Configuration;
import com.yarg.robotpiserver.server.handler.HandlerBase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Uses com.sun.net.HttpServer
 * https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html
 */
public class ConnectionServer {

    private final HandlerBase<?> connectHandler;
    private final HandlerBase<?> disconnectHandler;
    private HttpServer server;

    public ConnectionServer(HandlerBase<?> connectHandler, HandlerBase<?> disconnectHandler) throws IOException {
        Objects.requireNonNull(connectHandler, "Connect handler MUST NOT be null.");
        Objects.requireNonNull(disconnectHandler, "Disconnect handler MUST NOT be null.");
        this.connectHandler = connectHandler;
        this.disconnectHandler = disconnectHandler;
        init();
    }

    private void init() throws IOException {

        // Get the configuration model that defines how we are to setup the server.
        ConfigurationModel model = Configuration.getInstance().getConfigurationModel();

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        if (model.getServerIpAddress() == null) {
            server = HttpServer.create(new InetSocketAddress(model.getServerPort()), model.getServerBackLogging());
        } else {
            server = HttpServer.create(new InetSocketAddress(model.getServerIpAddress(), model.getServerPort()), model.getServerBackLogging());
        }

        server.createContext("/connect", connectHandler);
        server.createContext("/disconnect", disconnectHandler);
        server.setExecutor(threadPoolExecutor);
    }

    public void startServer() {
        server.start();
        System.out.println("Server started: " + Configuration.getInstance().getConfigurationModel().toString());
    }

    public InetSocketAddress getServerAddress() {
        return server.getAddress();
    }

    public void stopServer() {
        server.stop(1);
    }
}
