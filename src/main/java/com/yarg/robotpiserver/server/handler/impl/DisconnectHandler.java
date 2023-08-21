package com.yarg.robotpiserver.server.handler.impl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yarg.gen.models.DisconnectResponse;
import com.yarg.robotpiserver.client.ClientConnection;
import com.yarg.robotpiserver.server.handler.HandlerBase;
import com.yarg.robotpiserver.server.handler.HandlerResponse;
import com.yarg.robotpiserver.server.handler.HandlerResponseCode;

import java.io.IOException;

/**
 * Handle the disconnect request. Response should be a DisconnectResponse. Request should be a DisconnectRequest.
 */
public class DisconnectHandler extends HandlerBase<DisconnectResponse> {

    public DisconnectHandler() {
        // TODO: support constructor injection for managing robot
    }

    @Override
    public HandlerResponse<DisconnectResponse> handleRequest(HttpExchange exchange) {

        DisconnectResponse response = new DisconnectResponse();

        if (ClientConnection.getInstance().hasConnection()) {
            ClientConnection.getInstance().disconnectClient();
            response.setMessage("Connection successfully terminated.");
            // TODO: terminate robot operations
        } else {
            response.setMessage("No connection exists. Connection terminated.");
        }

        return new HandlerResponse<DisconnectResponse>(HandlerResponseCode.SUCCESS, response);
    }
}
