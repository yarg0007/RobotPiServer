package com.yarg.robotpiserver.server;

import com.sun.net.httpserver.HttpExchange;

public interface ConnectionServiceInterface {

    /**
     * Handle the connection request. Response should be either a ConnectSuccessResponse or a ConnectErrorResponse.
     * @param httpExchange Request received and response to be generated.
     */
    public void handleConnect(HttpExchange httpExchange);

    /**
     * Handle the disconnect request.  Response should be a DisconnectResponse. Request should be a DisconnectRequest.
     * @param httpExchange Request received and response to be generated.
     */
    public void handleDisconnect(HttpExchange httpExchange);
}
