package com.yarg.robotpiserver.server.handler.impl;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.yarg.gen.models.DisconnectRequest;
import com.yarg.gen.models.DisconnectResponse;
import com.yarg.robotpiserver.client.ClientConnection;
import com.yarg.robotpiserver.server.controller.ControllerInterface;
import com.yarg.robotpiserver.server.handler.HandlerBase;
import com.yarg.robotpiserver.server.handler.HandlerResponse;
import com.yarg.robotpiserver.server.handler.HandlerResponseCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/**
 * Handle the disconnect request. Stops all robot operations and disconnects the client.
 * If the request body contains shutdown=true, the Raspberry Pi will be shut down.
 */
public class DisconnectHandler extends HandlerBase<DisconnectResponse> {

    private final ControllerInterface controller;
    private final Gson gson = new Gson();

    public DisconnectHandler(ControllerInterface controller) {
        Objects.requireNonNull(controller, "controller MUST NOT be null.");
        this.controller = controller;
    }

    @Override
    public HandlerResponse<DisconnectResponse> handleRequest(HttpExchange exchange) {

        boolean shutdown = parseShutdownFlag(exchange);

        DisconnectResponse response = new DisconnectResponse();

        if (ClientConnection.getInstance().hasConnection()) {
            controller.stopController();
            ClientConnection.getInstance().disconnectClient();
            response.setMessage("Connection successfully terminated.");
        } else {
            response.setMessage("No connection exists. Connection terminated.");
        }

        if (shutdown) {
            initiateShutdown();
        }

        return new HandlerResponse<DisconnectResponse>(HandlerResponseCode.SUCCESS, response);
    }

    private boolean parseShutdownFlag(HttpExchange exchange) {
        InputStream requestBody = exchange.getRequestBody();
        String body = readRequestBody(requestBody);
        if (body == null || body.trim().isEmpty()) {
            return false;
        }
        DisconnectRequest request = gson.fromJson(body, DisconnectRequest.class);
        return request != null && Boolean.TRUE.equals(request.isShutdown());
    }

    protected void initiateShutdown() {
        try {
            Runtime.getRuntime().exec(new String[]{"sudo", "shutdown", "-h", "now"});
            System.out.println("Shutdown initiated.");
        } catch (IOException e) {
            System.out.println("Failed to initiate shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
