package com.yarg.robotpiserver.server.disconnect;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yarg.robotpiserver.server.client.manager.ConnectedClient;

public class DisconnectionHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange client) throws IOException {

		ConnectedClient.getInstance().disconnectClient();

		String response = "Disconnected";
		client.sendResponseHeaders(200, response.getBytes().length);
		OutputStream os = client.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

}
