package com.yarg.robotpiserver.client;

import java.net.InetSocketAddress;

/**
 * Singleton for tracking the state of the client connection.
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
    public synchronized boolean hasConnection() {
        return (clientAddress != null);
    }

    /**
     * Disconnect the client.
     */
    public synchronized void disconnectClient() {
        clientAddress = null;
    }

    /**
     * Connect to a new client.
     * @param clientAddress Client's InetSocketAddress.
     */
    public synchronized void connectClient(InetSocketAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    /**
     * Get the IP address of the client connection.
     * @return Client connection IP address.
     */
    public synchronized String getClientIpAddress() {
        return clientAddress.getAddress().getHostAddress();
    }
}
