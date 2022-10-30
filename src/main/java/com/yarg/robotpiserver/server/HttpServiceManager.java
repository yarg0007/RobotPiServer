package com.yarg.robotpiserver.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.yarg.robotpiserver.server.connect.ConnectionHandler;
import com.yarg.robotpiserver.server.disconnect.DisconnectionHandler;

public class HttpServiceManager {

	private HttpServer httpServer;

	public HttpServiceManager() {
		try {
			httpServer = HttpServer.create(new InetSocketAddress(8082), 0);
			httpServer.createContext("/connect", new ConnectionHandler());
			httpServer.createContext("/disconnect", new DisconnectionHandler());
			httpServer.setExecutor(null);
			httpServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void terminate() {
		httpServer.stop(5);
	}
}
