package com.yarg.robotpiserver.client;

import java.net.InetSocketAddress;

/**
 * Singleton for tracking the state of the client conneciton.
 */
public class ClientConnection {

    private static ClientConnection instance = new ClientConnection();
    private InetSocketAddress clientAddress;

    private ClientConnection() {
        clientAddress = null;
    }

    /**
     * Get the singleton instance.
     * @return Singleton instance.
     */
    public static ClientConnection getInstance() {
        return instance;
    }

    /**
     * Check if there is already a connection with the client.
     * @return True if a client connection already exists, false otherwise.
     */
    public boolean hasConnection() {
        return (clientAddress == null);
    }

    /**
     * Disconnect the client.
     */
    public void disconnectClient() {
        clientAddress = null;
    }

    /**
     * Connect to a new client.
     * @param clientAddress Client's InetSocketAddress.
     */
    public void connectClient(InetSocketAddress clientAddress) {
        this.clientAddress = clientAddress;
    }
}
