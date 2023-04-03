package com.yarg.robotpiserver.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.yarg.gen.models.ConfigurationModel;
import com.yarg.robotpiserver.config.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ConnectionServer {

    private ConnectionServiceInterface connectionService;
    private HttpServer server;

    public ConnectionServer(ConnectionServiceInterface connectionService) throws IOException {
        this.connectionService = connectionService;
        init();
    }

    private void init() throws IOException {
        ConfigurationModel model = Configuration.getInstance().getConfigurationModel();

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        server = HttpServer.create(new InetSocketAddress(model.getServerIpAddress(), model.getServerPort()), model.getServerBackLogging());
        server.createContext("/connect", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                connectionService.handleConnect(exchange);
            }
        });
        server.createContext("/disconnect", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                connectionService.handleDisconnect(exchange);
            }
        });
        server.setExecutor(threadPoolExecutor);
        server.start();
        System.out.println("Server started: " + model.toString());
    }

    public void stopServer() {
        server.stop(1);
    }
}
