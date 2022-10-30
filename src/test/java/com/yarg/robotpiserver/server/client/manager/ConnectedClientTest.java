package com.yarg.robotpiserver.server.client.manager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ConnectedClientTest {

	@AfterMethod(alwaysRun = true)
	public void cleanup() {
		ConnectedClient.getInstance().reset();
	}

	@Test
	public void noClientConnectedByDefault() {
		assertFalse(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), "");
	}

	@Test
	public void clientConnected() {
		String ipAddress = "127.0.0.1";
		ConnectedClient.getInstance().setClientIpAddress(ipAddress);
		assertTrue(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), ipAddress);
	}

	@Test
	public void clientConnectedWithNullIpAddress() {
		ConnectedClient.getInstance().setClientIpAddress(null);
		assertFalse(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), "");
	}

	@Test
	public void clientConnectedWithEmptyIpAddress() {
		ConnectedClient.getInstance().setClientIpAddress("");
		assertFalse(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), "");
	}

	@Test
	public void twoDifferentClientsConnect() {
		String firstIpAddress = "192.168.0.1";
		String secondIpAddress = "127.0.0.1";
		ConnectedClient.getInstance().setClientIpAddress(firstIpAddress);
		ConnectedClient.getInstance().setClientIpAddress(secondIpAddress);
		assertTrue(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), secondIpAddress);
	}

	@Test
	public void twoDifferentClientsConnectAndSecondClientIsNullIpAddress() {
		String firstIpAddress = "192.168.0.1";
		ConnectedClient.getInstance().setClientIpAddress(firstIpAddress);
		ConnectedClient.getInstance().setClientIpAddress(null);
		assertFalse(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), "");
	}

	@Test
	public void disconnectClient() {
		String ipAddress = "127.0.0.1";
		ConnectedClient.getInstance().setClientIpAddress(ipAddress);
		ConnectedClient.getInstance().disconnectClient();
		assertFalse(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), "");
	}

	@Test
	public void disconnectCalledMultipleTimes() {
		ConnectedClient.getInstance().disconnectClient();
		ConnectedClient.getInstance().disconnectClient();
		assertFalse(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), "");
	}
}
