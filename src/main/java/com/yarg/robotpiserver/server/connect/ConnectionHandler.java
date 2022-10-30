package com.yarg.robotpiserver.server.connect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yarg.robotpiserver.server.client.manager.ConnectedClient;

public class ConnectionHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange client) throws IOException {
		client.getRequestHeaders();
		InetSocketAddress remoteAddress = client.getRemoteAddress();
		InetAddress address = remoteAddress.getAddress();
		String host = address.getHostAddress();
		ConnectedClient.getInstance().setClientIpAddress(host);

		String response = "Connected";
		client.sendResponseHeaders(200, response.getBytes().length);
		OutputStream os = client.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

}
