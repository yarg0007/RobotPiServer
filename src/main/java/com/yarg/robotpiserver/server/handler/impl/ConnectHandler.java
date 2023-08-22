package com.yarg.robotpiserver.server.handler.impl;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.yarg.gen.models.ConnectErrorResponse;
import com.yarg.gen.models.ConnectResponse;
import com.yarg.robotpiserver.client.ClientConnection;
import com.yarg.robotpiserver.server.handler.HandlerBase;
import com.yarg.robotpiserver.server.handler.HandlerResponse;
import com.yarg.robotpiserver.server.handler.HandlerResponseCode;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Handle the connection request.
 */
public class ConnectHandler extends HandlerBase<ConnectResponse> {

    private Gson gson = new Gson();

    public ConnectHandler() {
        // TODO: support constructor injection for starting robot
    }

    @Override
    public HandlerResponse<ConnectResponse> handleRequest(HttpExchange exchange) {

        ConnectResponse response;
        HandlerResponseCode responseCode;

        if (ClientConnection.getInstance().hasConnection()) {
            response = new ConnectErrorResponse().errorCode(1000);
            response.setMessage("A client connection already exists.");
            responseCode = HandlerResponseCode.INTERNAL_SERVER_ERROR;
        } else {
            InetSocketAddress remoteAddress = exchange.getRemoteAddress();
            ClientConnection.getInstance().connectClient(remoteAddress);

            response = new ConnectResponse();
            response.setMessage("Connection established.");
            responseCode = HandlerResponseCode.SUCCESS;

            // TODO: Start connections back to client.
            InetAddress inetAddress = remoteAddress.getAddress();
            inetAddress.getHostAddress();
        }

        return new HandlerResponse<ConnectResponse>(responseCode, response);
    }
}
