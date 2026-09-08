package com.yarg.robotpiserver.server.handler.impl;

import com.sun.net.httpserver.HttpExchange;
import com.yarg.gen.models.ConnectResponse;
import com.yarg.robotpiserver.client.ClientConnection;
import com.yarg.robotpiserver.server.controller.ControllerInterface;
import com.yarg.robotpiserver.server.handler.HandlerBase;
import com.yarg.robotpiserver.server.handler.HandlerResponse;
import com.yarg.robotpiserver.server.handler.HandlerResponseCode;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Handle the connection request. Supports reconnection: if a client is already connected,
 * the existing session is stopped before establishing the new connection.
 */
public class ConnectHandler extends HandlerBase<ConnectResponse> {

    private final ControllerInterface controller;

    public ConnectHandler(ControllerInterface controller) {
        Objects.requireNonNull(controller, "controller MUST NOT be null.");
        this.controller = controller;
    }

    @Override
    public HandlerResponse<ConnectResponse> handleRequest(HttpExchange exchange) {

        if (ClientConnection.getInstance().hasConnection()) {
            controller.stopController();
            ClientConnection.getInstance().disconnectClient();
        }

        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        ClientConnection.getInstance().connectClient(remoteAddress);
        controller.startController(remoteAddress);

        ConnectResponse response = new ConnectResponse();
        response.setMessage("Connection established.");

        return new HandlerResponse<ConnectResponse>(HandlerResponseCode.SUCCESS, response);
    }
}
