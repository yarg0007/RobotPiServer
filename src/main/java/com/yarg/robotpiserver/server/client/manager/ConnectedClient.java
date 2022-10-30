package com.yarg.robotpiserver.server.client.manager;

public class ConnectedClient {

	private static ConnectedClient instance;

	// Member fields
	private volatile String clientIpAddress;

	private ConnectedClient() {

	}

	/**
	 * Get the singleton instance.
	 *
	 * @return Singleton instance.
	 */
	public static ConnectedClient getInstance() {

		if (instance == null) {
			synchronized (ConnectedClient.class) {
				if (instance == null) {
					instance = new ConnectedClient();
				}
			}
		}

		return instance;
	}

	/**
	 * Set the client IP address.
	 *
	 * @param clientIpAddress
	 *            Client IP address of the connected client.
	 */
	public void setClientIpAddress(String clientIpAddress) {

		if (clientIpAddress != null && !clientIpAddress.isEmpty()) {
			this.clientIpAddress = clientIpAddress;
		} else {
			this.clientIpAddress = null;
		}

	}

	/**
	 * Check if there is a client connected.
	 *
	 * @return True if client connected, false otherwise.
	 */
	public boolean isClientConnected() {

		return clientIpAddress == null ? false : true;

	}

	/**
	 * Get the client IP address connected to the server.
	 *
	 * @return Client IP address or an empty string if none connected.
	 */
	public String getClientIpAddress() {

		return clientIpAddress == null ? "" : clientIpAddress;

	}

	/**
	 * Disconnect a the registered client.
	 */
	public void disconnectClient() {

		clientIpAddress = null;

	}

	/**
	 * Used when testing to reset the singleton instance.
	 */
	protected void reset() {
		clientIpAddress = null;
	}
}
