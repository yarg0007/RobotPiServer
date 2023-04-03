package com.yarg.robotpiserver.server;

import com.sun.net.httpserver.HttpServer;

public class ConnectionServer {

    private ConnectionServiceInterface connectionService;

    public ConnectionServer(ConnectionServiceInterface connectionService) {
        this.connectionService = connectionService;
    }

    private void init() {
//        HttpServer server = HttpServer.create()
    }
}
